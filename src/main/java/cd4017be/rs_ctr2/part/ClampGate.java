package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.clamp_gate;
import static java.lang.Math.max;
import static java.lang.Math.min;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class ClampGate extends MultiInputGate {

	public ClampGate() {
		super(3);
		in[1] = Integer.MIN_VALUE;
		in[2] = Integer.MAX_VALUE;
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.EAST, ISignalReceiver.TYPE_ID);
		setPort(3, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public boolean evaluate() {
		active = false;
		return host != null && state != (state = min(max(in[1], in[0]), in[2]));
	}

	@Override
	public Item item() {
		return clamp_gate;
	}

	@Override
	protected String info() {
		return "state.rs_ctr2.clamp";
	}

}
