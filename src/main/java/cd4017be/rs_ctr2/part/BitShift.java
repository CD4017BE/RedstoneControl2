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
		return host != null && state != (state = shift(in[0], in[1]));
	}

	private static int shift(int v, int s) {
		if (s < 0)
			return s > -32 ? v >>> -s : 0;
		else return s < 32 ? v << s : 0;
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
