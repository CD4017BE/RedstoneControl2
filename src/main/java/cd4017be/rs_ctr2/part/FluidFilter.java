package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Content.fluid_filter;
import static net.minecraftforge.registries.ForgeRegistries.FLUIDS;

import java.util.function.*;

import cd4017be.api.grid.port.IFluidAccess;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import cd4017be.rs_ctr2.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;


/**
 * @author CD4017BE */
public class FluidFilter extends OrientedPart
implements IFluidAccess, ISignalReceiver, IGate, Predicate<FluidStack>, IProbeInfo {

	private Fluid filter = Fluids.EMPTY;
	IFluidAccess main = IFluidAccess.NOP, rem = IFluidAccess.NOP;
	@Sync public byte val;
	/** i<-1: none from main, -1: any from main, 0<=i<8: item[i] from main, i>=8: all from rem */
	@Sync(to = SAVE|GUI) public byte idx;
	@Sync public boolean active;

	public FluidFilter() {
		super(4);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, IFluidAccess.TYPE_ID);
		setPort(1, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.EAST, IFluidAccess.TYPE_ID);
		setPort(3, pos, Direction.SOUTH, IFluidAccess.TYPE_ID);
	}

	@Override
	public Item item() {
		return fluid_filter;
	}

	@Override
	public Object getHandler(int port) {
		return isMaster(port) ? null : this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 2) main = IFluidAccess.of(handler);
		else if (port == 3) rem = IFluidAccess.of(handler);
	}

	@Override
	public boolean isMaster(int port) {
		return port >= 2;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (val == (val = (byte)value) || active) return;
		active = true;
		GATE_UPDATER.add(this);
	}

	@Override
	public boolean evaluate() {
		active = false;
		idx = val;
		return false;
	}

	private Predicate<FluidStack> filter() {
		return idx == 0 ? this : idx < 0 ? this.negate() : null;
	}

	@Override
	public boolean test(FluidStack stack) {
		return stack.getFluid() == filter;
	}

	@Override
	public void getContent(ObjIntConsumer<FluidStack> inspector, int rec) {
		if (--rec < 0) return;
		Predicate<FluidStack> filter = filter();
		if (filter == null)
			rem.getContent(inspector, rec);
		else main.getContent((stack, n) -> {
			if (filter.test(stack)) inspector.accept(stack, n);
		}, rec);
	}

	@Override
	public int transfer(
		int amount, Predicate<FluidStack> filter, ToIntFunction<FluidStack> target, int rec
	) {
		if (--rec < 0) return 0;
		Predicate<FluidStack> filter0 = filter();
		return filter0 == null ? rem.transfer(amount, filter, target, rec)
			: main.transfer(amount, filter0.and(filter), target, rec);
	}

	@Override
	public int insert(FluidStack stack, int rec) {
		if (--rec < 0) return 0;
		return (test(stack) ? main : rem).insert(stack, rec);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		if ((mode & SAVE) != 0) {
			filter = FLUIDS.getValue(new ResourceLocation(nbt.getString("fluid")));
			if (filter == null) filter = Fluids.EMPTY;
		}
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		if ((mode & SAVE) != 0)
			nbt.putString("fluid", filter.getRegistryName().toString());
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand == null || player.isCrouching())
			return super.onInteract(player, hand, hit, pos);
		ItemStack stack = player.getMainHandItem();
		return Utils.serverAction(player, stack.isEmpty() ? ()-> {
			player.displayClientMessage(new TranslationTextComponent(
				filter.getAttributes().getTranslationKey()
			), true);
		} : ()-> FluidUtil.getFluidHandler(stack).ifPresent(
			h -> filter = h.drain(Integer.MAX_VALUE, FluidAction.SIMULATE).getFluid()
		));
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.fluid_filter", val,
			main != IFluidAccess.NOP,
			rem != IFluidAccess.NOP,
			filter.getAttributes().getTranslationKey()
		};
	}

}
