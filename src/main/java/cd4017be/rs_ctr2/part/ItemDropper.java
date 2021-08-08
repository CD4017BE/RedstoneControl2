package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.item_dropper;
import static net.minecraftforge.items.ItemHandlerHelper.copyStackWithSize;

import java.util.function.*;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.base.Predicates;

import cd4017be.api.grid.port.IBlockSupplier;
import cd4017be.api.grid.port.IInventoryAccess;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;


/**
 * @author CD4017BE */
public class ItemDropper extends OrientedPart implements IInventoryAccess, IBlockSupplier {

	protected IBlockSupplier block = this;
	protected ImmutablePair<BlockPos, ServerWorld> last;
	float y;

	public ItemDropper() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, IBlockSupplier.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, IInventoryAccess.TYPE_ID);
		int d = orient.b.getStepY();
		y = (d != 0 ? d + 2 << 1 : ports[0] >> 4 & 15) * .125F;
	}

	@Override
	public Item item() {
		return item_dropper;
	}

	@Override
	public Object getHandler(int port) {
		return port != 0 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) {
			block = IBlockSupplier.of(handler, this);
			last = null;
		}
	}

	@Override
	public boolean isMaster(int port) {
		return port == 0;
	}

	@Override
	public void getContent(ObjIntConsumer<ItemStack> inspector, int rec) {
		ImmutablePair<BlockPos, ServerWorld> pos = block.getBlock();
		if (pos == null) return;
		for (ItemEntity ei : pos.right.getEntities(
			EntityType.ITEM, new AxisAlignedBB(pos.left), Predicates.alwaysTrue()
		)) inspector.accept(ei.getItem(), 0);
	}

	@Override
	public int transfer(
		int amount, Predicate<ItemStack> filter, ToIntFunction<ItemStack> target, int rec
	) {
		ImmutablePair<BlockPos, ServerWorld> pos = block.getBlock();
		if (pos == null) return 0;
		int am0 = amount;
		for (ItemEntity ei : pos.right.getEntities(
			EntityType.ITEM, new AxisAlignedBB(pos.left), e -> filter.test(e.getItem())
		)) {
			ItemStack stack = ei.getItem();
			int n = target.applyAsInt(
				stack.getCount() <= amount ? stack
				: copyStackWithSize(stack, amount)
			);
			stack.shrink(n);
			if (stack.isEmpty()) pos.right.despawn(ei);
			else ei.setItem(stack);
			if ((amount -= n) <= 0) break;
		}
		return am0 - amount;
	}

	@Override
	public int insert(ItemStack stack, int rec) {
		ImmutablePair<BlockPos, ServerWorld> pos = block.getBlock();
		if (pos == null) return 0;
		BlockPos p = pos.left;
		ItemEntity ei = new ItemEntity(
			pos.right, p.getX() + 0.5, p.getY() + (double)y, p.getZ() + 0.5, stack.copy()
		);
		ei.setDefaultPickUpDelay();
		ei.setDeltaMovement(Vector3d.ZERO);
		pos.right.addFreshEntity(ei);
		return stack.getCount();
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return last != null ? last
		: onEdge() ? new ImmutablePair<>(
			host.pos().relative(orient.b, -1),
			(ServerWorld)host.world()
		) : null;
	}

}
