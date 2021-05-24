package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;

/**@author CD4017BE */
public abstract class MultiInputGate extends SignalGate {

	@Sync
	public final int[] in;

	public MultiInputGate(int inputs) {
		super(inputs + 1);
		this.in = new int[inputs];
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		if (port == 0 || port > in.length) return null;
		int i = port - 1;
		return (v, r) -> {
			if (v == in[i]) return;
			in[i] = v;
			update();
		};
	}

	protected String info() {
		return "state.rs_ctr2.gate" + in.length;
	}

	@Override
	public Object[] stateInfo() {
		Object[] args = new Object[in.length + 2];
		args[0] = info();
		args[1] = state;
		for (int i = 0; i < in.length; i++)
			args[i + 2] = in[i];
		return args;
	}

}
