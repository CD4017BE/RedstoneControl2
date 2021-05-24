package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.data_mux;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class DataMux extends MultiInputGate {

	public DataMux() {
		super(3);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.EAST, ISignalReceiver.TYPE_ID);
		setPort(3, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public boolean evaluate() {
		active = false;
		int ctr = in[0];
		return host != null && state != (state = in[1] & ~ctr | in[2] & ctr);
	}

	@Override
	public Item item() {
		return data_mux;
	}

	@Override
	protected String info() {
		return "state.rs_ctr2.mux";
	}

}
