package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.lib.network.Sync.Type.*;
import static cd4017be.lib.util.ItemFluidUtil.*;
import static java.lang.Math.min;
import static net.minecraftforge.items.ItemHandlerHelper.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.container.ContainerAssembler;
import cd4017be.lib.block.BlockTE;
import cd4017be.lib.block.BlockTE.ITEDropItems;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;

public class Assembler extends BaseTileEntity
implements ITickableServerOnly, IItemHandler, IUnnamedContainerProvider, ITEDropItems {

	final ArrayList<ItemStack> disassembly = new ArrayList<>();
	final ItemStack[] inventory = new ItemStack[23];
	ItemStack[] assembly;
	short[] counts;
	@Sync
	public long hasSlot;
	@Sync(type = Fix)
	public final byte[] refs = new byte[23];
	@Sync(type = I16)
	public int maxDA, remDA, maxASS, countASS;
	@Sync(type = I8)
	public int idxDA, t;
	boolean canDA = true, updateASS = true;

	public Assembler(TileEntityType<?> type) {
		super(type);
		Arrays.fill(inventory, ItemStack.EMPTY);
	}

	@Override
	public void tick() {
		if (--t > 0) return;
		t = 5;
		ItemStack stack;
		if (!disassembly.isEmpty()) {
			if (--idxDA < 0) idxDA = disassembly.size() - 1;
			stack = disassembly.get(idxDA);
			remDA -= stack.getCount();
			stack = insertItemStacked(this, stack, false);
			if (stack.isEmpty()) disassembly.remove(idxDA);
			else {
				remDA += stack.getCount();
				disassembly.set(idxDA, stack);
			}
		} else if (canDA && disassemble(stack = inventory[0])) {
			stack.shrink(1);
			maxDA = 0;
			for (ItemStack stack1 : disassembly)
				maxDA += stack1.getCount();
			remDA = maxDA;
		} else {
			canDA = false;
			remDA = 0;
		}
		if (!updateASS || counts == null) return;
		updateASS = false;
		int n = 0, j = 2;
		for (int i = 0; i < counts.length; i++) {
			int m = counts[i];
			if (m >= 0) continue;
			n += m;
			if ((hasSlot >> i & 1) != 0) continue;
			for (; j < 23; j++) {
				if (refs[j] >= 0) continue;
				refs[j] = (byte)(i + 1);
				(inventory[j] = assembly[i].copy()).setCount(1);
				hasSlot |= 1L << i;
				break;
			}
		}
		countASS = maxASS + n;
		if (n < 0) return;
		stack = inventory[1];
		if (stack.getCount() > stack.getMaxStackSize()) return;
		stack.grow(1);
		for (int i = 0; i < counts.length; i++)
			counts[i] -= assembly[i].getCount();
		hasSlot = 0;
		updateASS = true;
	}

	private boolean disassemble(ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.AIR) return false;
		if (item != Content.grid) {
			stack = Content.microblock.convert(stack, level, worldPosition);
			if (stack.isEmpty()) return false;
			disassembly.add(stack);
			return true;
		}
		ListNBT list = stack.getOrCreateTagElement(BlockTE.TE_TAG)
			.getList("parts", NBT.TAG_COMPOUND);
		int j0 = disassembly.size();
		parts: for (int i = 0; i < list.size(); i++) {
			GridPart part = GridPart.load(null, list.getCompound(i), SAVE);
			if (part == null || (stack = part.asItemStack()).isEmpty())
				continue parts;
			for (int j = j0, j1 = disassembly.size(); j < j1; j++) {
				ItemStack stack1 = disassembly.get(j);
				if (canItemStacksStack(stack, stack1)) {
					stack1.grow(stack.getCount());
					continue parts;
				}
			}
			disassembly.add(stack);
		}
		return true;
	}

	private void cancelAssemble() {
		if (assembly == null) return;
		for (int i = 0; i < counts.length; i++) {
			ItemStack stack = assembly[i];
			stack.grow(counts[i]);
			int n = stack.getCount();
			if (n <= 0) continue;
			disassembly.add(stack);
			remDA += n;
			maxDA += n;
		}
		for (int i = 2; i < 23; i++) {
			if (refs[i] == 0) continue;
			inventory[i] = ItemStack.EMPTY;
			refs[i] = 0;
		}
		assembly = null;
		counts = null;
		hasSlot = 0;
		updateASS = false;
		countASS = 0;
	}

	private void startAssemble(ItemStack stack) {
		int l = disassembly.size();
		if (!disassemble(stack)) return;
		List<ItemStack> list = disassembly.subList(l, disassembly.size());
		assembly = list.toArray(new ItemStack[l = list.size()]);
		list.clear();
		counts = new short[l];
		for (int j = 2; j < 23; j++) {
			stack = inventory[j];
			int n = stack.getCount();
			for (int i = 0; n > 0 && i < l; i++) {
				if (!canItemStacksStack(stack, assembly[i])) continue;
				counts[i] += n;
				inventory[j] = ItemStack.EMPTY;
				n = 0;
			}
			if (n > 0) continue;
			refs[j] = -1;
		}
		hasSlot = 0;
		maxASS = countASS = 0;
		for (int i = 0; i < l; i++) {
			int m = assembly[i].getCount();
			maxASS += m;
			countASS += m;
			int n = counts[i] -= m;
			if (n < 0) countASS += n;
		}
		updateASS = true;
	}

	private ItemStack insertAssemble(int slot, ItemStack stack, boolean simulate) {
		int ref = refs[slot] - 1;
		if (ref < 0) return stack;
		ItemStack stack1 = assembly[ref];
		if (!canItemStacksStack(stack, stack1)) return stack;
		if (simulate) return ItemStack.EMPTY;
		int n = stack.getCount();
		countASS += n;
		n = counts[ref] += n;
		if (n >= 0) {
			countASS -= n;
			inventory[slot] = ItemStack.EMPTY;
			refs[slot] = -1;
			updateASS = true;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlots() {
		return 23;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory[slot];
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!isItemValid(slot, stack)) return stack;
		if (refs[slot] != 0)
			return insertAssemble(slot, stack, simulate);
		ItemStack stack0 = inventory[slot];
		int n = stack0.getCount();
		int m = min(stack.getMaxStackSize() - n, stack.getCount());
		if (m <= 0 || n > 0 && !canItemStacksStack(stack0, stack)) return stack;
		if (!simulate) {
			if (n > 0) stack0.grow(m);
			else {
				(inventory[slot] = stack.copy()).setCount(m);
				canDA |= slot == 0;
				if (slot == 1) startAssemble(stack);
			}
		}
		return copyStackWithSize(stack, stack.getCount() - m);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (refs[slot] != 0) return ItemStack.EMPTY;
		ItemStack stack = inventory[slot];
		amount = min(amount, stack.getCount());
		if (amount <= 0) return ItemStack.EMPTY;
		if (simulate) return copyStackWithSize(stack, amount);
		if (amount >= stack.getCount()) {
			inventory[slot] = ItemStack.EMPTY;
			if (slot == 1)
				cancelAssemble();
			else if (slot != 0 && assembly != null) {
				refs[slot] = -1;
				updateASS = true;
			}
			return stack;
		}
		updateASS |= slot == 1;
		return stack.split(amount);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return slot < 2 ^ stack.getItem() instanceof IGridItem;
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		if ((mode & SAVE) != 0) {
			nbt.put("inv", saveInventory(inventory));
			ListNBT list = new ListNBT();
			for (ItemStack stack : disassembly)
				list.add(saveItemHighRes(stack));
			nbt.put("disass", list);
			if (assembly != null) {
				list = new ListNBT();
				for (int i = 0; i < assembly.length; i++) {
					CompoundNBT tag = saveItemHighRes(assembly[i]);
					tag.putShort("fill", counts[i]);
					list.add(tag);
				}
				nbt.put("assembly", list);
			}
		}
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		if ((mode & SAVE) != 0) {
			loadInventory(nbt.getList("inv", NBT.TAG_COMPOUND), inventory);
			ListNBT list = nbt.getList("disass", NBT.TAG_COMPOUND);
			disassembly.clear();
			for (int i = 0; i < list.size(); i++)
				disassembly.add(loadItemHighRes(list.getCompound(i)));
			if (nbt.contains("assembly", NBT.TAG_LIST)) {
				list = nbt.getList("assembly", NBT.TAG_COMPOUND);
				int l = list.size();
				assembly = new ItemStack[l];
				counts = new short[l];
				for (int i = 0; i < l; i++) {
					CompoundNBT tag = list.getCompound(i);
					counts[i] = tag.getShort("fill");
					assembly[i] = loadItemHighRes(tag);
				}
			}
		}
	}

	@Override
	public ContainerAssembler createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerAssembler(id, inv, this, this);
	}

	@Sync(to = GUI)
	public float progressDA() {
		return -(float)remDA / (float)maxDA;
	}

	@Sync(to = GUI)
	public float progressASS() {
		return -(float)countASS / (float)maxASS;
	}

	public short count(int slot) {
		slot = refs[slot + 2] - 1;
		return slot < 0 ? (short)(slot + 2) : counts[slot];
	}

	@Override
	public void spawnExtraDrops(ItemStack tool) {
		cancelAssemble();
		for (ItemStack stack : inventory)
			ItemFluidUtil.dropStack(stack, level, worldPosition);
		for (ItemStack stack : disassembly)
			ItemFluidUtil.dropStack(stack, level, worldPosition);
	}

}
