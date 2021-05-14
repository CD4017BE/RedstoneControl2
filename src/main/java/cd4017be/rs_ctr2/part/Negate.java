package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.neg_gate;

import net.minecraft.item.Item;


public class Negate extends NotGate {

	@Override
	public Item item() {
		return neg_gate;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || state == (state = -value)) return;
		out.updateInput(state, rec);
	}

}
