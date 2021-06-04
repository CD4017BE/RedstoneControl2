package cd4017be.rs_ctr2.part;

import cd4017be.rs_ctr2.Content;
import net.minecraft.item.Item;


/**
 * @author CD4017BE */
public class BCDConverter extends NotGate {

	@Override
	public Item item() {
		return Content.bcd_converter;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0) return;
		int x = 0;
		for (char c : Integer.toString(value).toCharArray())
			x = x << 4 | c - '0' & 15;
		if (state != x) out.updateInput(state = x, rec);
	}

}
