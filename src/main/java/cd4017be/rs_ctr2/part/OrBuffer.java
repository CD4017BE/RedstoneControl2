package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.or_buffer;

import net.minecraft.item.Item;


public class OrBuffer extends NotGate {

	@Override
	public Item item() {
		return or_buffer;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || state == (state = value != 0 ? -1 : 0)) return;
		out.updateInput(state, rec);
	}

}
