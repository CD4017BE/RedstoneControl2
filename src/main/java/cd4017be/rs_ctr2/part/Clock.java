package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.Content;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
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
		if (t == 0) super.latchOut();
		if (dt > 0) update();
	}

	@Override
	public Object getHandler(int port) {
		return port == 1 ? this : null;
	}

	@Override
	public Item item() {
		return Content.clock;
	}

	@Override
	public void updateInput(int value, int rec) {
		dt = value;
		if (value > 0) update();
	}

	@Override
	public String toString() {
		return TooltipUtil.format("state.rs_ctr2.clock", state, dt, t);
	}

}
