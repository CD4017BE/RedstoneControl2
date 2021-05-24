package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.delay;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class Delay extends SignalGate implements ISignalReceiver {

	@Sync public int in;

	public Delay() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null) return false;
		state = in;
		return true;
	}

	@Override
	public Object getHandler(int port) {
		return port == 1 ? this : null;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (value == in) return;
		in = value;
		update();
	}

	@Override
	public Item item() {
		return delay;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{"state.rs_ctr2.gate1", state, in};
	}

}
