package cd4017be.rs_ctr2.api.gate.ports;

import java.util.function.*;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**Grid port handler for item inventory interaction.
 * It's essentially {@link IItemHandler} broken down to just inspecting inventory
 * contents and moving items from one inventory to another without a concept of slots.
 * @author CD4017BE */
public interface IInventoryAccess extends UnaryOperator<ItemStack> {

	/**@param inspector function called for each item
	 * alongside its stack limit (don't modify given stack) */
	void getContent(ObjIntConsumer<ItemStack> inspector);

	/**Attempt to transfer items to another inventory
	 * @param amount maximum amount to transfer
	 * @param filter to restrict, what items to transfer
	 * @param target destination inventory, see {@link #apply(ItemStack)}
	 * @return remaining amount <b>not</b> transfered */
	int transfer(int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target);

	/**Attempt to insert the given stack.
	 * @param stack item to insert (don't modify)
	 * @return remainder that could not be inserted */
	@Override
	ItemStack apply(ItemStack stack);

	/** does nothing */
	IInventoryAccess NOP = new IInventoryAccess() {
		@Override
		public void getContent(ObjIntConsumer<ItemStack> inspector) {}
		@Override
		public int transfer(int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target) {return 0;}
		@Override
		public ItemStack apply(ItemStack stack) {return stack;}
	};

	/** port type id */
	int TYPE_ID = 2;

	static Predicate<ItemStack> filter(ItemStack stack) {
		return s -> ItemHandlerHelper.canItemStacksStack(s, stack);
	}

	class ItemHandlerAccess implements IInventoryAccess {

		public final IItemHandler inv;

		public ItemHandlerAccess(IItemHandler inv) {
			this.inv = inv;
		}

		@Override
		public void getContent(ObjIntConsumer<ItemStack> inspector) {
			for (int l = inv.getSlots(), i = 0; i < l; i++)
				inspector.accept(inv.getStackInSlot(i), inv.getSlotLimit(i));
		}

		@Override
		public ItemStack apply(ItemStack stack) {
			return ItemHandlerHelper.insertItemStacked(inv, stack, false);
		}

		@Override
		public int transfer(
			int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target
		) {
			for (int l = inv.getSlots(), i = 0; i < l && amount > 0; i++) {
				ItemStack stack = inv.extractItem(i, amount, true);
				int n = stack.getCount();
				if (n <= 0 || !filter.test(stack)) continue;
				n -= target.apply(stack).getCount();
				if (n <= 0) continue;
				inv.extractItem(l, n, false);
				amount -= n;
			}
			return amount;
		}

	}

}
