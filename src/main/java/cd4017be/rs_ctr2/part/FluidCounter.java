package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.fluid_counter;

import java.util.function.ObjIntConsumer;

import cd4017be.api.grid.port.IFluidAccess;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;


/**
 * @author CD4017BE */
public class FluidCounter extends OrientedPart
implements ISignalReceiver, ObjIntConsumer<FluidStack>, IProbeInfo {

	ISignalReceiver out = ISignalReceiver.NOP;
	IFluidAccess inv = IFluidAccess.NOP;
	@Sync public int clk, state;
	@Sync public boolean empty;

	public FluidCounter() {
		super(3);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, IFluidAccess.TYPE_ID);
		setPort(2, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return fluid_counter;
	}

	@Override
	public Object getHandler(int port) {
		return port == 2 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) (out = ISignalReceiver.of(handler)).updateInput(state);
		else if (port == 1) inv = IFluidAccess.of(handler);
	}

	@Override
	public boolean isMaster(int port) {
		return port < 2;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || (~clk & (clk = value)) == 0) return;
		value = state; state = 0;
		inv.getContent(this);
		if (state != value) out.updateInput(state, rec);
	}

	@Override
	public void accept(FluidStack stack, int value) {
		state += empty ? value - stack.getAmount() : stack.getAmount();
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand == null || !player.getItemInHand(hand).isEmpty())
			return super.onInteract(player, hand, hit, pos);
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		player.displayClientMessage(new TranslationTextComponent(
			(empty = !empty) ? "msg.rs_ctr2.count_air" : "msg.rs_ctr2.count_fluid"
		), true);
		return ActionResultType.SUCCESS;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			empty ? "state.rs_ctr2.air_counter" : "state.rs_ctr2.fluid_counter",
			state, inv != IFluidAccess.NOP, clk
		};
	}

}
