package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.logic_out;

import net.minecraft.item.Item;

public class LogicOut extends AnalogOut {

	@Override
	public Item item() {
		return logic_out;
	}

	@Override
	public void updateInput(int value, int rec) {
		super.updateInput(value != 0 ? 15 : 0, rec);
	}

}
