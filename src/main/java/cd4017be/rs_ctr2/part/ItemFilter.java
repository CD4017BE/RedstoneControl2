package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Content.item_filter;

import java.util.Arrays;
import java.util.function.*;

import cd4017be.api.grid.port.IInventoryAccess;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import cd4017be.rs_ctr2.container.ContainerItemFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.common.util.Constants.NBT;


/**
 * @author CD4017BE */
public class ItemFilter extends OrientedPart
implements IInventoryAccess, ISignalReceiver, IGate,
Predicate<ItemStack>, IUnnamedContainerProvider, IProbeInfo {

	private final ItemStack[] items = new ItemStack[8];
	Item[] filter = new Item[0];
	IInventoryAccess main = IInventoryAccess.NOP, rem = IInventoryAccess.NOP;
	@Sync public byte val;
	/** i<-1: none from main, -1: any from main, 0<=i<8: item[i] from main, i>=8: all from rem */
	@Sync(to = SAVE|GUI) public byte idx;
	@Sync public boolean active;

	public ItemFilter() {
		super(4);
		Arrays.fill(items, ItemStack.EMPTY);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, IInventoryAccess.TYPE_ID);
		setPort(1, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.EAST, IInventoryAccess.TYPE_ID);
		setPort(3, pos, Direction.SOUTH, IInventoryAccess.TYPE_ID);
	}

	@Override
	public Item item() {
		return item_filter;
	}

	@Override
	public Object getHandler(int port) {
		return isMaster(port) ? null : this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 2) main = IInventoryAccess.of(handler);
		else if (port == 3) rem = IInventoryAccess.of(handler);
	}

	@Override
	public boolean isMaster(int port) {
		return port >= 2;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (val == (val = (byte)value) || active) return;
		active = true;
		GATE_UPDATER.add(this);
	}

	@Override
	public boolean evaluate() {
		active = false;
		idx = val;
		return false;
	}

	private Predicate<ItemStack> filter() {
		if (idx >= 0) {
			if (idx >= items.length) return null;
			Item item = items[idx].getItem();
			return stack -> stack.getItem() == item;
		}
		return idx == -1 ? this : this.negate();
	}

	@Override
	public void getContent(ObjIntConsumer<ItemStack> inspector, int rec) {
		if (--rec < 0) return;
		Predicate<ItemStack> filter = filter();
		if (filter == null)
			rem.getContent(inspector, rec);
		else main.getContent((stack, n) -> {
			if (filter.test(stack)) inspector.accept(stack, n);
		}, rec);
	}

	@Override
	public int transfer(
		int amount, Predicate<ItemStack> filter, ToIntFunction<ItemStack> target, int rec
	) {
		if (--rec < 0) return 0;
		Predicate<ItemStack> filter0 = filter();
		return filter0 == null ? rem.transfer(amount, filter, target, rec)
			: main.transfer(amount, filter0.and(filter), target, rec);
	}

	@Override
	public int insert(ItemStack stack, int rec) {
		if (--rec < 0) return 0;
		return (test(stack) ? main : rem).insert(stack, rec);
	}

	@Override
	public boolean test(ItemStack stack) {
		Item item = stack.getItem();
		for (Item i : filter)
			if (item == i)
				return true;
		return false;
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		if ((mode & SAVE) != 0) {
			ItemFluidUtil.loadInventory(nbt.getList("inv", NBT.TAG_COMPOUND), items);
			updateFilter(null, -1);
		}
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		if ((mode & SAVE) != 0)
			nbt.put("inv", ItemFluidUtil.saveInventory(items));
	}

	private void updateFilter(ItemStack stack, int slot) {
		if (slot >= 0) items[slot] = stack;
		int m = 0;
		for (int i = 0; i < items.length; i++)
			if (!items[i].isEmpty()) m |= 1 << i;
		int n = Integer.bitCount(m);
		if (filter.length != n) filter = new Item[n];
		n = 0;
		for (int i = 0; m != 0; i++, m >>>= 1)
			if ((m & 1) != 0)
				filter[n++] = items[i].getItem();
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand == null || player.isCrouching())
			return super.onInteract(player, hand, hit, pos);
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		player.openMenu(this);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ContainerItemFilter createMenu(int id, PlayerInventory pinv, PlayerEntity player) {
		BasicInventory inv = new BasicInventory(items);
		inv.onModify = this::updateFilter;
		return new ContainerItemFilter(id, pinv, inv, this);
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.item_filter", val,
			main != IInventoryAccess.NOP,
			rem != IInventoryAccess.NOP
		};
	}

}
