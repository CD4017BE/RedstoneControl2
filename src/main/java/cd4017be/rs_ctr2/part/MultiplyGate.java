package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.product_gate;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class MultiplyGate extends MultiInputGate {

	public MultiplyGate() {
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
		return host != null && state != (state = (int)((long)in[0] * (long)in[1] >> in[2]));
	}

	@Override
	public Item item() {
		return product_gate;
	}

	@Override
	protected String info() {
		return "state.rs_ctr2.multiply";
	}

}
