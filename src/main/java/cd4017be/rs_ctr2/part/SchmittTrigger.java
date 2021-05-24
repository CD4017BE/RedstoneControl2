package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.schmitt_trigger;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class SchmittTrigger extends MultiInputGate {

	public SchmittTrigger() {
		super(3);
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
		if (host == null) return false;
		int s = ~state >> 31, v = in[0];
		int l = in[1 - s], h = in[2 + s];
		if (h > l ? v > l : v < l) return false;
		state = s;
		return true;
	}

	@Override
	public Item item() {
		return schmitt_trigger;
	}

	@Override
	protected String info() {
		return "state.rs_ctr2.schmitt_trigger";
	}

}
