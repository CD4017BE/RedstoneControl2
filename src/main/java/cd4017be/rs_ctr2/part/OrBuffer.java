package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.or_buffer;

import net.minecraft.item.Item;

/**@author CD4017BE */
public class OrBuffer extends DirectGate {

	@Override
	public Item item() {
		return or_buffer;
	}

	@Override
	protected int result() {
		return in != 0 ? -1 : 0;
	}

	@Override
	protected int change(int old, int in) {
		return (old != 0 ? 1 : 0) ^ (in != 0 ? 1 : 0);
	}

}
