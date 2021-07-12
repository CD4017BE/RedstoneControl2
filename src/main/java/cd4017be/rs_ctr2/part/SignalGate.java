package cd4017be.rs_ctr2.part;

import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;

import cd4017be.api.grid.GridPart;
import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.tick.IGate;
import cd4017be.rs_ctr2.api.IProbeInfo;

/**@author CD4017BE */
public abstract class SignalGate extends OrientedPart implements IGate, IProbeInfo {

	protected ISignalReceiver output = ISignalReceiver.NOP;
	@Sync public int state;
	@Sync public boolean active;

	public SignalGate(int ports) {
		super(ports);
	}

	@Override
	public GridPart setHost(IGridHost host) {
		if (active && host != null && this.host == null)
			GATE_UPDATER.add(this);
		return super.setHost(host);
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port != 0) return;
		if (handler instanceof ISignalReceiver)
			(output = (ISignalReceiver)handler).updateInput(state);
		else output = ISignalReceiver.NOP;
	}

	@Override
	public boolean isMaster(int channel) {
		return channel == 0;
	}

	protected void update() {
		if (!active) {
			active = true;
			GATE_UPDATER.add(this);
		}
	}

	@Override
	public void latchOut() {
		output.updateInput(state);
	}

}
