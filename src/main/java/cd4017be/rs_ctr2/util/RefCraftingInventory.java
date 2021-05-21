package cd4017be.rs_ctr2.util;

import java.util.function.*;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;

/**Crafting Inventory with referenced slot access.
 * @author CD4017BE */
public class RefCraftingInventory extends CraftingInventory {

	private final ItemStack[] inv;
	private final IntUnaryOperator slot;
	private final Runnable update;
	private final int w, h, n;

	/**@param w grid width
	 * @param h grid height
	 * @param inv reference inventory
	 * @param slot mapper */
	public RefCraftingInventory(
		int w, int h, ItemStack[] inv, IntUnaryOperator slot, Runnable update
	) {
		super(null, 0, 0);
		this.inv = inv;
		this.slot = slot;
		this.update = update;
		this.w = w;
		this.h = h;
		this.n = w * h;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getContainerSize() {
		return n;
	}

	@Override
	public ItemStack getItem(int s) {
		s = slot.applyAsInt(s);
		return s > 0 ? inv[s] : ItemStack.EMPTY;
	}

	@Override
	public void setItem(int s, ItemStack stack) {
		s = slot.applyAsInt(s);
		if (s <= 0) return;
		inv[s] = stack;
		if (update != null) update.run();
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack stack = getItem(slot);
		if (stack.isEmpty()) return ItemStack.EMPTY;
		ItemStack ret;
		if (amount >= stack.getCount()) {
			ret = stack;
			stack = ItemStack.EMPTY;
		} else ret = stack.split(amount);
		setItem(slot, stack);
		return ret;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack stack = getItem(slot);
		if (!stack.isEmpty()) setItem(slot, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public boolean canPlaceItem(int s, ItemStack stack) {
		return slot.applyAsInt(s) > 0;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < n; i++)
			if (!getItem(i).isEmpty())
				return false;
		return true;
	}

	@Override
	public void fillStackedContents(RecipeItemHelper recipeHelper) {
		for (int i = 0; i < n; i++)
			recipeHelper.accountSimpleStack(getItem(i));
	}

}
