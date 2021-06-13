package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.item_splitter;

import java.util.function.*;

import cd4017be.api.grid.port.IInventoryAccess;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class SplitterI extends OrientedPart implements IInventoryAccess {

	IInventoryAccess src = IInventoryAccess.NOP;

	public SplitterI() {
		super(6);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, TYPE_ID);
		setPort(1, pos, Direction.SOUTH, TYPE_ID);
		setPort(2, pos, Direction.DOWN, TYPE_ID);
		setPort(3, pos, Direction.UP, TYPE_ID);
		setPort(4, pos, Direction.WEST, TYPE_ID);
		setPort(5, pos, Direction.EAST, TYPE_ID);
	}

	@Override
	public Item item() {
		return item_splitter;
	}

	@Override
	public Object getHandler(int port) {
		return port == 0 ? null : this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) src = IInventoryAccess.of(handler);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel == 0;
	}

	@Override
	public void getContent(ObjIntConsumer<ItemStack> inspector, int rec) {
		if (--rec < 0) return;
		src.getContent(inspector, rec);
	}

	@Override
	public int transfer(
		int amount, Predicate<ItemStack> filter, ToIntFunction<ItemStack> target, int rec
	) {
		return --rec < 0 ? 0 : src.transfer(amount, filter, target, rec);
	}

	@Override
	public int insert(ItemStack stack, int rec) {
		return --rec < 0 ? 0 : src.insert(stack, rec);
	}

}
