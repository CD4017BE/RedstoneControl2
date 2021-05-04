package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.analog_out;

import cd4017be.rs_ctr2.api.gate.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;


public class AnalogOut extends OrientedPart implements ISignalReceiver {

	@Sync public int state;

	public AnalogOut() {
		super(1);
	}

	@Override
	public Item item() {
		return analog_out;
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int channel) {
		return false;
	}

	@Override
	public void updateInput(int value) {
		if (value != state && host != null) {
			state = value;
			host.updateNeighbor(orient.b.getOpposite());
		}
	}

	@Override
	public int analogOutput(Direction side) {
		return side == orient.b ? state : 0;
	}

	@Override
	public boolean connectRedstone(Direction side) {
		return side == orient.b;
	}

}
