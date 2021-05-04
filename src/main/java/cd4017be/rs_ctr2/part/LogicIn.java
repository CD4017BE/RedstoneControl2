package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.logic_in;

import net.minecraft.item.Item;
import net.minecraft.util.Direction;

public class LogicIn extends AnalogIn {

	@Override
	public Item item() {
		return logic_in;
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null) return false;
		Direction dir = orient.b.getOpposite();
		int old = state;
		return (state =
			host.world().hasSignal(host.pos().relative(dir), dir) ? -1 : 0
		) != old;
	}

}
