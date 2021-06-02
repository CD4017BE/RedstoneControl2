package cd4017be.rs_ctr2.part;

import static cd4017be.lib.util.ItemFluidUtil.placeFluid;
import static cd4017be.rs_ctr2.Content.fluid_io;
import static net.minecraftforge.fluids.FluidAttributes.BUCKET_VOLUME;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;
import java.util.function.*;

import cd4017be.api.grid.port.IBlockSupplier;
import cd4017be.api.grid.port.IFluidAccess;
import cd4017be.lib.network.Sync;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.fluid.*;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**@author CD4017BE */
public class FluidIO extends CapabilityIO<IFluidHandler>
implements IFluidAccess, IProbeInfo {

	@Sync public FluidStack remainder = FluidStack.EMPTY;

	public FluidIO() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, IBlockSupplier.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, IFluidAccess.TYPE_ID);
	}

	@Override
	protected Capability<IFluidHandler> capability() {
		return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public Item item() {
		return fluid_io;
	}

	@Override
	public void getContent(ObjIntConsumer<FluidStack> inspector, int rec) {
		IFluidHandler inv = get(null);
		if (inv != null) {
			for (int l = inv.getTanks(), i = 0; i < l; i++)
				inspector.accept(inv.getFluidInTank(i), inv.getTankCapacity(i));
			return;
		}
		if (last == null) return;
		FluidState state = last.right.getFluidState(last.left);
		inspector.accept(
			!state.isSource() ? FluidStack.EMPTY
				: new FluidStack(state.getType(), BUCKET_VOLUME),
			BUCKET_VOLUME
		);
	}

	@Override
	public int transfer(
		int amount, Predicate<FluidStack> filter, ToIntFunction<FluidStack> target, int rec
	) {
		//drain remainder
		FluidStack stack = remainder;
		if (!stack.isEmpty()) {
			if (!filter.test(stack)) return 0;
			int i = target.applyAsInt(
				amount >= stack.getAmount() ? stack
					: new FluidStack(stack, amount)
			);
			stack.shrink(i);
			return i;
		}
		IFluidHandler inv = get(null);
		//drain block
		if (inv == null) {
			if (amount < BUCKET_VOLUME || last == null) return 0;
			stack = ItemFluidUtil.drainFluid(last.right, last.left, filter);
			if (stack.isEmpty()) return 0;
			int i = target.applyAsInt(stack);
			if (i < stack.getAmount())
				(remainder = stack).shrink(i);
			return i;
		}
		//drain inventory
		findFluid: {
			stack = inv.drain(amount, SIMULATE);
			if (stack.isEmpty()) return 0;
			if (filter.test(stack)) break findFluid;
			for (int l = inv.getTanks(), i = 0; i < l; i++) {
				stack = inv.getFluidInTank(i);
				if (stack.isEmpty() || !filter.test(stack)) continue;
				stack = inv.drain(new FluidStack(stack, amount), SIMULATE);
				if (!stack.isEmpty()) break findFluid;
			}
			return 0;
		}
		stack.setAmount(amount = target.applyAsInt(stack));
		if (amount <= 0) return 0;
		inv.drain(stack, EXECUTE);
		return amount;
	}

	@Override
	public int insert(FluidStack stack, int rec) {
		IFluidHandler inv = get(null);
		if (inv != null) return inv.fill(stack, EXECUTE);
		int m = remainder.isFluidEqual(stack) ? remainder.getAmount() : 0;
		(stack = stack.copy()).grow(m);
		if (stack.getAmount() < BUCKET_VOLUME) return 0;
		if (!placeFluid(last.right, last.left, stack)) return 0;
		if (m > 0) remainder = FluidStack.EMPTY;
		return BUCKET_VOLUME - m;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.fluid_io",
			IBlockSupplier.toString(block),
			get(null) != null
		};
	}

}
