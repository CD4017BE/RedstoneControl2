package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.fluid_splitter;

import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import cd4017be.api.grid.port.IFluidAccess;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

/**@author CD4017BE */
public class SplitterF extends OrientedPart
implements IFluidAccess, IProbeInfo {

	IFluidAccess src = IFluidAccess.NOP;

	public SplitterF() {
		super(6);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, TYPE_ID);
		setPort(1, pos, Direction.SOUTH, TYPE_ID);
		setPort(2, pos, Direction.DOWN, TYPE_ID);
		setPort(3, pos, Direction.UP, TYPE_ID);
		setPort(4, pos, Direction.WEST, TYPE_ID);
		setPort(5, pos, Direction.EAST, TYPE_ID);
	}

	@Override
	public Item item() {
		return fluid_splitter;
	}

	@Override
	public Object getHandler(int port) {
		return port == 0 ? null : this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) src = IFluidAccess.of(handler);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel == 0;
	}

	@Override
	public void getContent(ObjIntConsumer<FluidStack> inspector, int rec) {
		if (--rec < 0) return;
		src.getContent(inspector, rec);
	}

	@Override
	public int transfer(
		int amount, Predicate<FluidStack> filter, ToIntFunction<FluidStack> target, int rec
	) {
		return --rec < 0 ? 0 : src.transfer(amount, filter, target, rec);
	}

	@Override
	public int insert(FluidStack stack, int rec) {
		return --rec < 0 ? 0 : src.insert(stack, rec);
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.res_splitter",
			src != IFluidAccess.NOP
		};
	}

}
