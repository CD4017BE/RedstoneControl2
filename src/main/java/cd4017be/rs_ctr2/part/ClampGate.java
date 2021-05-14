package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.clamp_gate;
import static java.lang.Math.max;
import static java.lang.Math.min;

import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;


public class ClampGate extends MultiInputGate {

	public ClampGate() {
		super(3);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
		setPort(3, pos, Direction.EAST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public boolean evaluate() {
		active = false;
		int l0 = in[1], l1 = in[2];
		return host != null && state != (state = min(max(l0, l1), max(min(l0, l1), in[0])));
	}

	@Override
	public Item item() {
		return clamp_gate;
	}

}
