package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.fluid_counter;

import java.util.function.ObjIntConsumer;

import cd4017be.api.grid.port.IFluidAccess;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.FluidStack;


/**
 * @author CD4017BE */
public class FluidCounter extends ResourceCounter
implements ObjIntConsumer<FluidStack> {

	IFluidAccess inv = IFluidAccess.NOP;

	@Override
	public Item item() {
		return fluid_counter;
	}

	@Override
	protected int type() {
		return IFluidAccess.TYPE_ID;
	}

	@Override
	protected void setSource(Object handler) {
		inv = IFluidAccess.of(handler);
	}

	@Override
	protected void count() {
		state = 0;
		inv.getContent(this);
	}

	@Override
	public void accept(FluidStack stack, int value) {
		state += empty ? value - stack.getAmount() : stack.getAmount();
	}

	@Override
	protected String message(boolean empty) {
		return empty ? "msg.rs_ctr2.count_air" : "msg.rs_ctr2.count_fluid";
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			empty ? "state.rs_ctr2.air_counter" : "state.rs_ctr2.fluid_counter",
			state, inv != IFluidAccess.NOP, clk
		};
	}

}
