package cd4017be.rs_ctr2.part;

import cd4017be.lib.network.Sync;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;


public class Clock extends SignalGate implements ISignalReceiver {

	@Sync
	public int dt, t;

	public Clock() {
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
		if (host == null || dt < 0) return false;
		if (++t >= dt) {
			t = 0;
			state = dt == 0 ? 0 : ~state;
		}
		return true;
	}

	@Override
	public void latchOut() {
		super.latchOut();
		if (dt > 0) update();
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public Item item() {
		return Content.clock;
	}

	@Override
	public void updateInput(int value) {
		dt = value;
		if (value > 0) update();
	}

}
