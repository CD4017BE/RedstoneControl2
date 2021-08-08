package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;


/**
 * @author CD4017BE */
public abstract class ResourceCounter extends OrientedPart
implements ISignalReceiver, IProbeInfo {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync public int clk, state;
	@Sync public boolean empty;

	public ResourceCounter() {
		super(3);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, TYPE_ID);
		setPort(1, pos, Direction.NORTH, type());
		setPort(2, pos, Direction.WEST, TYPE_ID);
	}


	@Override
	public Object getHandler(int port) {
		return port == 2 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) (out = ISignalReceiver.of(handler)).updateInput(state);
		else if (port == 1) setSource(handler);
	}


	@Override
	public boolean isMaster(int port) {
		return port < 2;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || (~clk & (clk = value)) == 0) return;
		value = state;
		count();
		if (state != value) out.updateInput(state, rec);
	}


	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand == null || !player.getItemInHand(hand).isEmpty())
			return super.onInteract(player, hand, hit, pos);
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		player.displayClientMessage(new TranslationTextComponent(message(empty = !empty)), true);
		return ActionResultType.SUCCESS;
	}

	protected abstract int type();
	protected abstract void setSource(Object handler);
	protected abstract void count();
	protected abstract String message(boolean empty);

}
