package cd4017be.rs_ctr2.api.gate.ports;

import java.util.function.*;

import cd4017be.rs_ctr2.api.gate.Link;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**Grid port handler for fluid storage interaction.
 * It's essentially {@link IFluidHandler} broken down to just inspecting tank
 * contents and moving fluids from one storage to another.
 * @author CD4017BE */
public interface IFluidAccess extends ToIntFunction<FluidStack> {

	/**@param inspector function called for each fluid tank
	 * together with its capacity (don't modify given stack) */
	default void getContent(ObjIntConsumer<FluidStack> inspector) {
		getContent(inspector, Link.REC_FLUID);
	}

	/**@param inspector function called for each fluid tank
	 * together with its capacity (don't modify given stack)
	 * @param rec */
	void getContent(ObjIntConsumer<FluidStack> inspector, int rec);

	/**Attempt to transfer fluids to another inventory
	 * @param amount maximum amount to transfer
	 * @param filter to restrict, what fluids to transfer
	 * @param target destination inventory, see {@link #applyAsInt(FluidStack)}
	 * @return amount actually transfered */
	default int transfer(int amount, Predicate<FluidStack> filter, ToIntFunction<FluidStack> target) {
		return transfer(amount, filter, target, Link.REC_FLUID);
	}

	/**Attempt to transfer fluids to another inventory
	 * @param amount maximum amount to transfer
	 * @param filter to restrict, what fluids to transfer
	 * @param target destination inventory, see {@link #applyAsInt(FluidStack)}
	 * @param rec
	 * @return amount actually transfered */
	int transfer(int amount, Predicate<FluidStack> filter, ToIntFunction<FluidStack> target, int rec);

	/**Attempt to insert the given stack.
	 * @param stack fluid to insert (don't modify)
	 * @return amount actually filled */
	@Override
	default int applyAsInt(FluidStack stack) {
		return insert(stack, Link.REC_FLUID);
	}

	/**Attempt to insert the given stack.
	 * @param stack fluid to insert (don't modify)
	 * @param rec
	 * @return amount actually filled */
	int insert(FluidStack stack, int rec);

	/** does nothing */
	IFluidAccess NOP = new IFluidAccess() {
		@Override
		public int transfer(int amount, Predicate<FluidStack> filter, ToIntFunction<FluidStack> target, int rec) {return 0;}
		@Override
		public void getContent(ObjIntConsumer<FluidStack> inspector, int rec) {}
		@Override
		public int insert(FluidStack stack, int rec) {return 0;}
	};

	/** port type id */
	int TYPE_ID = 3;

	static IFluidAccess of(Object handler) {
		return handler instanceof IFluidAccess ? (IFluidAccess)handler : null;
	}
}
