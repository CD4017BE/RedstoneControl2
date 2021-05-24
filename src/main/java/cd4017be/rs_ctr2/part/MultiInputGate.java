package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.text.TooltipUtil;

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
	public String toString() {
		Object[] args = new Object[in.length + 1];
		args[0] = state;
		for (int i = 0; i < in.length; i++)
			args[i + 1] = in[i];
		return TooltipUtil.format(info(), args);
	}

}
