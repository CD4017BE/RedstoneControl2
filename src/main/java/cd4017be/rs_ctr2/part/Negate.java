package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.neg_gate;

import net.minecraft.item.Item;

/**@author CD4017BE */
public class Negate extends DirectGate {

	@Override
	public Item item() {
		return neg_gate;
	}

	@Override
	protected int result() {
		return -in;
	}

}
