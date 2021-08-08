package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.item_counter;

import java.util.function.ObjIntConsumer;

import cd4017be.api.grid.port.IInventoryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


/**
 * @author CD4017BE */
public class ItemCounter extends ResourceCounter
implements ObjIntConsumer<ItemStack> {

	IInventoryAccess inv = IInventoryAccess.NOP;

	@Override
	public Item item() {
		return item_counter;
	}

	@Override
	protected int type() {
		return IInventoryAccess.TYPE_ID;
	}

	@Override
	protected void setSource(Object handler) {
		inv = IInventoryAccess.of(handler);
	}

	@Override
	protected void count() {
		state = 0;
		inv.getContent(this);
	}

	@Override
	public void accept(ItemStack stack, int value) {
		value = stack.getCount();
		state += empty ? (value == 0 ? 1 : 0) : value;
	}

	@Override
	protected String message(boolean empty) {
		return empty ? "msg.rs_ctr2.count_slots" : "msg.rs_ctr2.count_items";
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			empty ? "state.rs_ctr2.slot_counter" : "state.rs_ctr2.item_counter",
			state, inv != IInventoryAccess.NOP, clk
		};
	}

}
