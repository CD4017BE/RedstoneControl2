package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.not_gate;

import cd4017be.lib.network.Sync;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.gate.GateUpdater;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;


public class NotGate extends OrientedPart implements ISignalReceiver {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync public int state;
	int lastTick;

	public NotGate() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (handler instanceof ISignalReceiver)
			(out = (ISignalReceiver)handler).updateInput(~state);
		else out = ISignalReceiver.NOP;
	}

	@Override
	public boolean isMaster(int channel) {
		return channel != 0;
	}

	@Override
	public Item item() {
		return not_gate;
	}

	@Override
	public void updateInput(int value) {
		if (value == state || lastTick == GateUpdater.TICK) return;
		state = value;
		lastTick = GateUpdater.TICK; //prevent infinite recursion
		out.updateInput(~value);
	}

}
