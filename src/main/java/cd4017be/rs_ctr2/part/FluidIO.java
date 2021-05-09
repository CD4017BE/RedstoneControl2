package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.fluid_io;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

import java.util.function.*;

import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.gate.ports.IFluidAccess;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

public class FluidIO extends CapabilityIO<IFluidHandler> implements IFluidAccess {

	public FluidIO() {
		super(1);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, IFluidAccess.TYPE_ID);
	}

	@Override
	protected IFluidHandler fallback() {
		return EmptyFluidHandler.INSTANCE;
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
		if (inv == null) accept(null);
		for (int l = inv.getTanks(), i = 0; i < l; i++)
			inspector.accept(inv.getFluidInTank(i), inv.getTankCapacity(i));
	}

	@Override
	public int transfer(
		int amount, Predicate<FluidStack> filter, ToIntFunction<FluidStack> target, int rec
	) {
		if (inv == null) accept(null);
		FluidStack stack; findFluid: {
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
		if (inv == null) accept(null);
		return inv.fill(stack, EXECUTE);
	}

}
