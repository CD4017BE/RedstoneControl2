package cd4017be.rs_ctr2.api.gate.ports;

import java.util.function.*;

import cd4017be.rs_ctr2.api.gate.Link;
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
	default void getContent(ObjIntConsumer<ItemStack> inspector) {
		getContent(inspector, Link.REC_ITEM);
	}

	/**@param inspector function called for each item
	 * alongside its stack limit (don't modify given stack)
	 * @param rec */
	void getContent(ObjIntConsumer<ItemStack> inspector, int rec);

	/**Attempt to transfer items to another inventory
	 * @param amount maximum amount to transfer
	 * @param filter to restrict, what items to transfer
	 * @param target destination inventory, see {@link #apply(ItemStack)}
	 * @return amount actually transfered */
	default int transfer(int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target) {
		return transfer(amount, filter, target, Link.REC_ITEM);
	}

	/**Attempt to transfer items to another inventory
	 * @param amount maximum amount to transfer
	 * @param filter to restrict, what items to transfer
	 * @param target destination inventory, see {@link #apply(ItemStack)}
	 * @param rec
	 * @return amount actually transfered */
	int transfer(int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target, int rec);

	/**Attempt to insert the given stack.
	 * @param stack item to insert (don't modify)
	 * @return remainder that could not be inserted */
	@Override
	default ItemStack apply(ItemStack stack) {
		return insert(stack, Link.REC_ITEM);
	}

	/**Attempt to insert the given stack.
	 * @param stack item to insert (don't modify)
	 * @param rec
	 * @return remainder that could not be inserted */
	ItemStack insert(ItemStack stack, int rec);

	/** does nothing */
	IInventoryAccess NOP = new IInventoryAccess() {
		@Override
		public void getContent(ObjIntConsumer<ItemStack> inspector, int rec) {}
		@Override
		public int transfer(int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target, int rec) {return 0;}
		@Override
		public ItemStack insert(ItemStack stack, int rec) {return stack;}
	};

	/** port type id */
	int TYPE_ID = 2;

	static IInventoryAccess of(Object handler) {
		return handler instanceof IInventoryAccess ? (IInventoryAccess)handler : NOP;
	}

	static Predicate<ItemStack> filter(ItemStack stack) {
		return s -> ItemHandlerHelper.canItemStacksStack(s, stack);
	}

}
