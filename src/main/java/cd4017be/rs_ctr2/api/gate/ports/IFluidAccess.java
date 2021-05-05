package cd4017be.rs_ctr2.api.gate.ports;

import java.util.function.*;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**Grid port handler for fluid storage interaction.
 * It's essentially {@link IFluidHandler} broken down to just inspecting tank
 * contents and moving fluids from one storage to another.
 * @author CD4017BE */
public interface IFluidAccess extends UnaryOperator<FluidStack> {

	/**@param inspector function called for each fluid tank
	 * together with its capacity (don't modify given stack) */
	void getContent(ObjIntConsumer<FluidStack> inspector);

	/**Attempt to transfer fluids to another inventory
	 * @param amount maximum amount to transfer
	 * @param filter to restrict, what fluids to transfer
	 * @param target destination inventory, see {@link #apply(FluidStack)}
	 * @return remaining amount <b>not</b> transfered */
	int transfer(int amount, Predicate<FluidStack> filter, UnaryOperator<FluidStack> target);

	/**Attempt to insert the given stack.
	 * @param stack fluid to insert (don't modify)
	 * @return remainder that could not be inserted */
	@Override
	FluidStack apply(FluidStack stack);

	/** does nothing */
	IFluidAccess NOP = new IFluidAccess() {
		@Override
		public int transfer(int amount, Predicate<FluidStack> filter, UnaryOperator<FluidStack> target) {return 0;}
		@Override
		public void getContent(ObjIntConsumer<FluidStack> inspector) {}
		@Override
		public FluidStack apply(FluidStack stack) {return stack;}
	};

	/** port type id */
	int TYPE_ID = 3;
}
