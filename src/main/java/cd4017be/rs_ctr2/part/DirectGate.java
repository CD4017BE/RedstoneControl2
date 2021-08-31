package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public abstract class DirectGate extends OrientedPart
implements ISignalReceiver, IProbeInfo {

	protected ISignalReceiver out = ISignalReceiver.NOP;
	@Sync public int in;

	public DirectGate() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
	}

	protected abstract int result();

	protected int change(int old, int in) {
		return old ^ in;
	}

	@Override
	public Object getHandler(int port) {
		return port == 1 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) (out = ISignalReceiver.of(handler)).updateInput(result());
	}

	@Override
	public boolean isMaster(int channel) {
		return channel == 0;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || change(in, in = value) == 0) return;
		out.updateInput(result(), rec);
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{
			"state.rs_ctr2.gate1",
			result(), in
		};
	}

}
