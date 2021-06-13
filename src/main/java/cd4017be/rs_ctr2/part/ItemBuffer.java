package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.lib.network.Sync.Type.I8;
import static cd4017be.rs_ctr2.Content.item_buffer;
import static cd4017be.rs_ctr2.Content.item_cable;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack;
import static net.minecraftforge.items.ItemHandlerHelper.copyStackWithSize;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.function.*;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.IGridItem;
import cd4017be.api.grid.port.IInventoryAccess;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.IPlayerPacketReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.container.ContainerItemBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;


/**
 * @author CD4017BE */
public class ItemBuffer extends MultiBlock<ItemBuffer>
implements IInventoryAccess, IUnnamedContainerProvider,
IPlayerPacketReceiver, IItemHandler {

	ArrayDeque<ItemStack> inv = new ArrayDeque<>(1);
	int slots, max;
	@Sync(to = GUI) public int n;
	@Sync(to = GUI, type = I8) public int scroll;

	public ItemBuffer(int pos) {
		super(pos);
	}

	@Override
	public Item item() {
		return item_buffer;
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int port) {
		return false;
	}

	@Override
	public void getContent(ObjIntConsumer<ItemStack> inspector, int rec) {
		rec = max - n;
		for (ItemStack stack : inv)
			inspector.accept(stack, rec);
		for (int i = slots - inv.size(); i > 0; i--)
			inspector.accept(ItemStack.EMPTY, rec);
	}

	@Override
	public int transfer(
		int amount, Predicate<ItemStack> filter, ToIntFunction<ItemStack> target, int rec
	) {
		for (Iterator<ItemStack> it = inv.descendingIterator(); it.hasNext();) {
			ItemStack stack = it.next();
			if (!filter.test(stack)) continue;
			int n = stack.getCount();
			int l = Math.min(n, amount);
			stack.setCount(l);
			l = target.applyAsInt(stack);
			this.n -= l;
			if ((n -= l) <= 0) it.remove();
			else stack.setCount(n);
			return l;
		}
		return 0;
	}

	@Override
	public int insert(ItemStack stack, int rec) {
		int m = stack.getCount(), l = Math.min(max - n, m);
		if (l <= 0) return 0;
		for (ItemStack stack1 : inv)
			if (canItemStacksStack(stack1, stack)) {
				stack1.grow(l);
				n += l;
				return l;
			}
		if (inv.size() >= slots) return 0;
		inv.addFirst(copyStackWithSize(stack, l));
		n += l;
		return l;
	}

	@Override
	public void setHost(IGridHost host) {
		super.setHost(host);
		if (host == null) return;
		while(inv.size() > slots) {
			ItemStack stack = inv.pollLast();
			n -= stack.getCount();
			ItemFluidUtil.dropStack(stack, host.world(), host.pos());
		}
		while(n > max) {
			ItemStack stack = inv.getLast();
			ItemStack stack1 = stack.split(n - max);
			n -= stack1.getCount();
			if (stack.isEmpty()) inv.pollLast();
			ItemFluidUtil.dropStack(stack1, host.world(), host.pos());
		}
	}

	@Override
	protected void onBoundsChange() {
		slots = Long.bitCount(bounds);
		max = slots * SERVER_CFG.item_buffer_size.get();
		setHost(host);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		if ((mode & SAVE) != 0) {
			n = 0;
			inv.clear();
			ListNBT list = nbt.getList("inv", NBT.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				ItemStack stack = ItemFluidUtil.loadItemHighRes(list.getCompound(i));
				if (stack.isEmpty()) continue;
				inv.addLast(stack);
				n += stack.getCount();
			}
		}
		super.loadState(nbt, mode);
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		if ((mode & SAVE) != 0) {
			ListNBT list = new ListNBT();
			for (ItemStack stack : inv)
				list.add(ItemFluidUtil.saveItemHighRes(stack));
			nbt.put("inv", list);
		}
		super.storeState(nbt, mode);
	}

	@Override
	protected ActionResultType createPort(IGridItem item, short port, boolean client) {
		if (item != item_cable) return ActionResultType.PASS;
		if (client) return ActionResultType.CONSUME;
		IGridHost host = this.host;
		host.removePart(this);
		ports = ArrayUtils.add(ports, (short)(port | TYPE_ID << 12));
		host.addPart(this);
		return ActionResultType.SUCCESS;
	}

	@Override
	protected ActionResultType onInteract(PlayerEntity player, boolean client) {
		if (client) return ActionResultType.CONSUME;
		NetworkHooks.openGui((ServerPlayerEntity)player, this, pkt -> pkt.writeByte(slots).writeInt(max));
		return ActionResultType.SUCCESS;
	}

	@Override
	protected ItemBuffer splitOff(long splitBounds, long thisBounds) {
		ItemBuffer other = new ItemBuffer(-1);
		// TODO split inventory
		return other;
	}

	@Override
	protected short[] merge(ItemBuffer other) {
		max += other.max;
		slots += other.slots;
		for (ItemStack stack : other.inv)
			insert(stack, 0);
		return ArrayUtils.addAll(ports, other.ports);
	}

	public final static ResourceLocation MODEL = Main.rl("part/item_buffer");

	@Override
	protected ResourceLocation model() {
		return MODEL;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.item_buffer",
			n, max, inv.size(), slots
		};
	}

	@Override
	public ContainerItemBuffer createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerItemBuffer(id, inv, this, this);
	}

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		scroll = Math.max(0, Math.min(pkt.readUnsignedByte(), slots - 16));
	}

	@Override
	public boolean dissassemble(World world, BlockPos pos) {
		ItemStack stack;
		while((stack = inv.pollFirst()) != null)
			ItemFluidUtil.dropStack(stack, world, pos);
		return true;
	}

	//IItemHandler implementation: only needed for GUI

	@Override
	public int getSlots() {
		return 16;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		slot += scroll;
		if (slot >= inv.size()) return ItemStack.EMPTY;
		//Maybe I should write my own implementation of ArrayDeque that supports direct indexing for this.
		for (ItemStack stack : inv)
			if (--slot < 0) return stack;
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!simulate) {
			int n = insert(stack, 0);
			return n == 0 ? stack : copyStackWithSize(stack, stack.getCount() - n);
		}
		int m = stack.getCount(), l = Math.min(max - n, m);
		if (l <= 0) return stack;
		m -= l;
		check: {
			if (inv.size() < slots) break check;
			for (ItemStack stack1 : inv)
				if (canItemStacksStack(stack1, stack))
					break check;
			return stack;
		}
		return copyStackWithSize(stack, m);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		slot += scroll;
		if (slot >= inv.size()) return ItemStack.EMPTY;
		for (Iterator<ItemStack> it = inv.iterator(); it.hasNext();) {
			ItemStack stack = it.next();
			if (--slot >= 0) continue;
			amount = Math.min(amount, stack.getCount());
			ItemStack stack1 = copyStackWithSize(stack, amount);
			if (!simulate) {
				n -= amount;
				stack.shrink(amount);
				if (stack.isEmpty()) it.remove();
			}
			return stack1;
		}
		return ItemStack.EMPTY;

	}

	@Override
	public int getSlotLimit(int slot) {
		return max;
	}

	//not used by GlitchSaveSlot
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return false;
	}

}
