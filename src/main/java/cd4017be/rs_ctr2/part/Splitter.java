package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.splitter;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class Splitter extends OrientedPart
implements ISignalReceiver, IProbeInfo {

	final ISignalReceiver[] out = new ISignalReceiver[5];
	@Sync public int state;

	public Splitter() {
		super(6);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, TYPE_ID);
		setPort(1, pos, Direction.SOUTH, TYPE_ID);
		setPort(2, pos, Direction.DOWN, TYPE_ID);
		setPort(3, pos, Direction.UP, TYPE_ID);
		setPort(4, pos, Direction.WEST, TYPE_ID);
		setPort(5, pos, Direction.EAST, TYPE_ID);
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (--port < 0) return;
		if (handler instanceof ISignalReceiver)
			(out[port] = (ISignalReceiver)handler).updateInput(state);
		else out[port] = null;
	}

	@Override
	public boolean isMaster(int channel) {
		return channel != 0;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || value == state) return;
		state = value;
		for (ISignalReceiver sr : out)
			if (sr != null)
				sr.updateInput(value, rec);
	}

	@Override
	public Item item() {
		return splitter;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{"state.rs_ctr2.splitter", state};
	}

}
