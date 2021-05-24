package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.bit_shift;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class BitShift extends MultiInputGate {

	public BitShift() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public boolean evaluate() {
		active = false;
		int a = in[0], b = in[1];
		return host != null && state != (state = b < 0 ? a >>> -b : a << b);
	}

	@Override
	public Item item() {
		return bit_shift;
	}

	@Override
	protected String info() {
		return "state.rs_ctr2.shift";
	}

}
