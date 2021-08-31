package cd4017be.rs_ctr2.part;

import cd4017be.rs_ctr2.Content;
import net.minecraft.item.Item;


/**
 * @author CD4017BE */
public class BCDConverter extends DirectGate {

	@Override
	public Item item() {
		return Content.bcd_converter;
	}

	@Override
	protected int result() {
		int x = 0;
		for (char c : Integer.toString(in).toCharArray())
			x = x << 4 | c - '0' & 15;
		return x;
	}

}
