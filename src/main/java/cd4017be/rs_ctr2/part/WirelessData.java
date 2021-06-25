package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.data_send;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;


/**
 * @author CD4017BE */
public class WirelessData extends Wireless implements ISignalReceiver {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync public int state;

	public WirelessData() {
		super(data_send);
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port != 0) (out = ISignalReceiver.of(handler)).updateInput(state);
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec >= 0 && state != (state = value))
			out.updateInput(value, rec);
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] { "state.rs_ctr2.data_send", state, link };
	}

}
