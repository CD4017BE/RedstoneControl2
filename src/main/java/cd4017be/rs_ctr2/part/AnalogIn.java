package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.analog_in;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**@author CD4017BE */
public class AnalogIn extends SignalGate {

	public AnalogIn() {
		super(1);
	}

	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return analog_in;
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void onBlockChange(World world, BlockPos pos, Direction dir) {
		if (dir == orient.b.getOpposite()) update();
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null) return false;
		Direction dir = orient.b.getOpposite();
		int old = state;
		return (state =
			host.world().getSignal(host.pos().relative(dir), dir)
		) != old;
	}

	@Override
	public boolean connectRedstone(Direction side) {
		return side == orient.b;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{"state.rs_ctr2.in", state};
	}

}
