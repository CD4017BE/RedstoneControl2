package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.not_gate;

import net.minecraft.item.Item;

/**@author CD4017BE */
public class NotGate extends DirectGate {

	@Override
	public Item item() {
		return not_gate;
	}

	protected int result() {
		return ~in;
	}

}
