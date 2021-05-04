package cd4017be.rs_ctr2.part;

import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.api.gate.ISignalReceiver;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;


public class Comparator extends MultiInputGate {

	public Comparator() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.EAST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public boolean evaluate() {
		active = false;
		return host != null && state != (state = in[1] > in[0] ? -1 : 0);
	}

	@Override
	public Item item() {
		return Content.comparator;
	}

}
