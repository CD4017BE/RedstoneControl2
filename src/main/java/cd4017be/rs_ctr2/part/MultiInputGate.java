package cd4017be.rs_ctr2.part;

import cd4017be.lib.network.Sync;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;


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

}
