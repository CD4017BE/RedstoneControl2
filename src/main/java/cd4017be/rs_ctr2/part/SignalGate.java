package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.api.gate.GateUpdater.GATE_UPDATER;

import cd4017be.rs_ctr2.api.gate.IGate;
import cd4017be.rs_ctr2.api.gate.ISignalReceiver;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.lib.network.Sync;


public abstract class SignalGate extends OrientedPart implements IGate {

	protected ISignalReceiver output = ISignalReceiver.NOP;
	@Sync public int state;
	@Sync public boolean active;

	public SignalGate(int ports) {
		super(ports);
	}

	@Override
	public void setHost(IGridHost host) {
		if (active && host != null && this.host == null)
			GATE_UPDATER.add(this);
		super.setHost(host);
	}

	@Override
	public void setHandler(int port, Object handler) {
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
