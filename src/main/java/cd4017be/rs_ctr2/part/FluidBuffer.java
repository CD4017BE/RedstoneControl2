package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.fluid_buffer;
import static cd4017be.rs_ctr2.Content.fluid_cable;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import java.util.function.*;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.IGridItem;
import cd4017be.api.grid.port.IFluidAccess;
import cd4017be.lib.network.Sync;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;


/**
 * @author CD4017BE */
public class FluidBuffer extends MultiBlock<FluidBuffer>
implements IFluidAccess, IFluidHandler {

	@Sync public FluidStack content = FluidStack.EMPTY;
	int max;

	public FluidBuffer(int pos) {
		super(pos);
	}

	@Override
	public Item item() {
		return fluid_buffer;
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int port) {
		return false;
	}

	@Override
	public void getContent(ObjIntConsumer<FluidStack> inspector, int rec) {
		inspector.accept(content, max);
	}

	@Override
	public int transfer(
		int amount, Predicate<FluidStack> filter, ToIntFunction<FluidStack> target, int rec
	) {
		int n = content.getAmount();
		amount = Math.min(amount, n);
		if (amount <= 0 || !filter.test(content)) return 0;
		content.setAmount(amount);
		amount = target.applyAsInt(content);
		content.setAmount(n - amount);
		return amount;
	}

	@Override
	public int insert(FluidStack stack, int rec) {
		int n = content.getAmount();
		int m = Math.min(max - n, stack.getAmount());
		if (m == 0) return 0;
		if (n == 0) content = new FluidStack(stack, m);
		else if (content.isFluidEqual(stack)) content.grow(m);
		else return 0;
		return m;
	}

	@Override
	public FluidBuffer setHost(IGridHost host) {
		super.setHost(host);
		if (host == null) return this;
		if (content.getAmount() > max)
			content.setAmount(max);
		return this;
	}

	@Override
	protected void onBoundsChange() {
		int n = Long.bitCount(bounds);
		max = n * SERVER_CFG.fluid_buffer_size.get();
		setHost(host);
	}

	@Override
	protected ActionResultType createPort(IGridItem item, short port, boolean client) {
		if (item != fluid_cable) return ActionResultType.PASS;
		if (client) return ActionResultType.CONSUME;
		IGridHost host = this.host;
		host.removePart(this);
		ports = ArrayUtils.add(ports, (short)(port | TYPE_ID << 12));
		host.addPart(this);
		return ActionResultType.SUCCESS;
	}

	@Override
	protected ActionResultType onInteract(PlayerEntity player, boolean client) {
		ItemStack stack = player.getMainHandItem();
		return Utils.serverAction(client, stack.isEmpty() ? ()-> {
			player.displayClientMessage(new TranslationTextComponent(
				"msg.rs_ctr2.fluid_buffer",
				content.getAmount(), max,
				content.getDisplayName(),
				Long.bitCount(bounds)
			), true);
		} : ()-> FluidUtil.interactWithFluidHandler(player, Hand.MAIN_HAND, this));
	}

	@Override
	protected FluidBuffer splitOff(long splitBounds, long thisBounds) {
		FluidBuffer other = new FluidBuffer(-1);
		if (!content.isEmpty()) {
			int n = (int)((long)content.getAmount()
				* (long)Long.bitCount(splitBounds)
				/ (long)Long.bitCount(thisBounds)
			);
			(other.content = content.copy()).setAmount(n);
			content.shrink(n);
		}
		return other;
	}

	@Override
	public FluidBuffer findAdjacent(IGridHost host, long b) {
		long o = outline(b) & ~b;
		FluidStack stack = content;
		return (FluidBuffer)host.findPart(stack.isEmpty()
			? p -> (p.bounds & o) != 0 && p instanceof FluidBuffer
			: p -> {
				if (!((p.bounds & o) != 0 && p instanceof FluidBuffer))
					return false;
				FluidStack stack1 = ((FluidBuffer)p).content;
				return stack1.isEmpty() || stack1.isFluidEqual(stack);
			}
		);
	}

	@Override
	protected short[] merge(FluidBuffer other) {
		max += other.max;
		if (content.isEmpty()) content = other.content;
		else content.grow(other.content.getAmount());
		return ArrayUtils.addAll(ports, other.ports);
	}

	public final static ResourceLocation MODEL = Main.rl("part/fluid_buffer");

	@Override
	protected ResourceLocation model() {
		return MODEL;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.fluid_buffer",
			'\\' + content.getDisplayName().getString(),
			content.getAmount(), max
		};
	}

	@Override
	public boolean dissassemble(World world, BlockPos pos) {
		content = FluidStack.EMPTY;
		return true;
	}

	//IItemHandler implementation: only needed for bucket interaction

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return content;
	}

	@Override
	public int getTankCapacity(int tank) {
		return max;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return true;
	}

	@Override
	public int fill(FluidStack stack, FluidAction action) {
		if (action.execute()) return insert(stack, 0);
		int n = content.getAmount();
		return n == 0 || content.isFluidEqual(stack)
			? Math.min(max - n, stack.getAmount()) : 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return content.isFluidEqual(resource)
			? drain(resource.getAmount(), action)
			: FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		maxDrain = Math.min(maxDrain, content.getAmount());
		if (maxDrain <= 0) return FluidStack.EMPTY;
		FluidStack stack = new FluidStack(content, maxDrain);
		if (action.execute()) content.shrink(maxDrain);
		return stack;
	}

}
