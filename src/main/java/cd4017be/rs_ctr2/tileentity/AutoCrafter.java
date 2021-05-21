package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.lib.util.ItemFluidUtil.canSlotStack;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static cd4017be.rs_ctr2.api.gate.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.part.OrientedPart.port;
import static net.minecraft.util.Direction.SOUTH;

import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import cd4017be.lib.block.BlockTE.ITEBreak;
import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.IPlayerPacketReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.gate.IGate;
import cd4017be.rs_ctr2.api.gate.ports.IEnergyAccess;
import cd4017be.rs_ctr2.api.gate.ports.IInventoryAccess;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import cd4017be.rs_ctr2.api.grid.ExtGridPorts;
import cd4017be.rs_ctr2.api.grid.IGridPortHolder;
import cd4017be.rs_ctr2.container.ContainerAutoCraft;
import cd4017be.rs_ctr2.util.RefCraftingInventory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;


public class AutoCrafter extends BaseTileEntity
implements IGridPortHolder, ITEBreak, IUnnamedContainerProvider,
IGate, ISignalReceiver, IInventoryAccess, IPlayerPacketReceiver {

	private static final byte R_SUCCESS = 0, R_MISSING_INGRED = 1,
	R_INVALID_RECIPE = 2, R_OUT_FULL = 4, R_NO_ENERGY = 8;

	final ExtGridPorts ports = new ExtGridPorts(this);
	public final BasicInventory inv = new BasicInventory(16);
	public final RefCraftingInventory craftInv;
	ISignalReceiver out = ISignalReceiver.NOP;
	IEnergyAccess energy = IEnergyAccess.NOP;
	ICraftingRecipe last;
	@Sync(to=SAVE|GUI)
	public long pattern;
	@Sync(to=SAVE|GUI)
	public int p2, p1;
	@Sync public int clk, res, e;
	@Sync public byte slotI, slot, active;

	public AutoCrafter(TileEntityType<?> type) {
		super(type);
		this.craftInv = new RefCraftingInventory(3, 3, inv.items, this::index, null);
		p1 = 0x321;
		p2 = 0x00987654;
		e = -SERVER_CFG.craft.get();
		updatePattern();
	}

	public int index(int slot) {
		return (int)(pattern >>> (slot << 2)) & 15;
	}

	private int craft() {
		{//check input slots
			int[] n = new int[16];
			for (int i = 0; i < 9; i++) n[index(i)]++;
			for (int i = 1; i < 16; i++) {
				int ni = n[i], m;
				if (ni > 0 && (m = inv.items[i].getCount()) > 0 && m < ni)
					return R_MISSING_INGRED;
			}
		}
		//check recipe
		if (last == null || !last.matches(craftInv, level)) {
			RecipeManager rm = level.getRecipeManager();
			last = rm.getRecipeFor(IRecipeType.CRAFTING, craftInv, level).orElse(null);
			if (last == null) return R_INVALID_RECIPE;
		}
		//check result
		ItemStack out = last.assemble(craftInv);
		if (!inv.insertItem(index(10), out, true).isEmpty()) return R_OUT_FULL;
		//check remainders
		boolean hasRem = false;
		NonNullList<ItemStack> rem = last.getRemainingItems(craftInv);
		int x = index(9);
		ItemStack stackR = x != 0 ? inv.items[x] : ItemStack.EMPTY;
		for (int i = 0; i < rem.size(); i++) {
			ItemStack stack = rem.get(i);
			int n = stack.getCount();
			if (n <= 0) continue;
			hasRem = true;
			if (craftInv.getItem(i).getCount() <= 1) continue;
			if (x == 0 || !canSlotStack(stackR, stack)) return R_OUT_FULL;
			if (stackR.isEmpty())
				stackR = stack.copy();
			else if ((n += stackR.getCount()) <= stackR.getMaxStackSize())
				stackR.setCount(n);
			else return R_OUT_FULL;
		}
		//craft
		for (int i = 0; i < 9; i++) craftInv.removeItem(i, 1);
		inv.insertItem(index(10), out, false);
		if (hasRem)
			for (int i = 0; i < rem.size(); i++) {
				ItemStack stack = rem.get(i);
				int n = stack.getCount();
				if (n <= 0) continue;
				if (craftInv.getItem(i).isEmpty()) {
					if (x == 0 || !canSlotStack(stackR, stack)
					|| (n += stackR.getCount()) > stack.getMaxStackSize()) {
						craftInv.setItem(i, stack);
						continue;
					}
					stackR.setCount(n);
				}
				inv.insertItem(x, stack, false);
			}
		e = -SERVER_CFG.craft.get();
		return R_SUCCESS;
	}

	@Override
	public Object getHandler(int port) {
		switch(port) {
		case 0: return (ISignalReceiver)this::updateClock;
		case 1: return (ISignalReceiver)(v, r)-> p2 = v;
		case 2: return (ISignalReceiver)(v, r)-> p1 = v;
		case 3: case 4: return this;
		default: return null;
		}
	}

	@Override
	public void setHandler(int port, Object handler) {
		switch(port) {
		case 5: (out = ISignalReceiver.of(handler)).updateInput(res); break;
		case 6: energy = IEnergyAccess.of(handler); break;
		}
	}

	public void updateClock(int value, int rec) {
		if ((~clk & (clk = value)) == 0) return;
		if (e < 0 && (e -= energy.transferEnergy(e, false)) < 0) {
			if (res != R_NO_ENERGY && --rec >= 0)
				out.updateInput(res = R_NO_ENERGY, rec);
			return;
		}
		if (active == 0) GATE_UPDATER.add(this);
		active |= 2;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (slotI == (slotI = (byte)(value & 15))) return;
		if (active == 0) GATE_UPDATER.add(this);
		active |= 1;
	}

	@Override
	public boolean evaluate() {
		if (unloaded) return false;
		if ((active & 1) != 0) slot = slotI;
		if ((active & 2) == 0) {
			active = 0;
			return false;
		}
		active = 0;
		updatePattern();
		return res != (res = craft());
	}

	private void updatePattern() {
		pattern = p1 & 0xfff | (long)p2 << 12;
	}

	@Override
	public void latchOut() {
		out.updateInput(res);
	}

	@Override
	public void getContent(ObjIntConsumer<ItemStack> inspector, int rec) {
		inspector.accept(inv.items[slot & 15], 64);
	}

	@Override
	public int transfer(
		int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target, int rec
	) {
		ItemStack stack = inv.extractItem(slot & 15, amount, true);
		if (stack.isEmpty() || !filter.test(stack)) return 0;
		stack.shrink(target.apply(stack).getCount());
		if ((amount = stack.getCount()) <= 0) return 0;
		inv.extractItem(slot & 15, amount, false);
		return amount;
	}

	@Override
	public ItemStack insert(ItemStack stack, int rec) {
		return inv.insertItem(slot & 15, stack, false);
	}

	@Override
	public ExtGridPorts extPorts() {
		return ports;
	}

	@Override
	public World world() {
		return level;
	}

	@Override
	public BlockPos pos() {
		return worldPosition;
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		if ((mode & SAVE) != 0) {
			nbt.put("inv", inv.write());
			nbt.put("ports", ports.serializeNBT());
		}
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		if ((mode & SAVE) != 0) {
			inv.read(nbt.getList("inv", NBT.TAG_COMPOUND));
			if (nbt.contains("ports", NBT.TAG_LONG_ARRAY))
				ports.deserializeNBT((LongArrayNBT)nbt.get("ports"));
		}
	}

	@Override
	public void clearCache() {
		super.clearCache();
		if (level.isClientSide) return;
		Orientation o = orientation();
		ports.clear();
		ports.createPort(port(o, 0x32, SOUTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x30, SOUTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x31, SOUTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x33, SOUTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x37, SOUTH, IInventoryAccess.TYPE_ID), false, true);
		ports.createPort(port(o, 0x34, SOUTH, ISignalReceiver.TYPE_ID), true, true);
		ports.createPort(port(o, 0x36, SOUTH, IEnergyAccess.TYPE_ID), true, true);
	}

	@Override
	public void onLoad() {
		if (!level.isClientSide) {
			ports.onLoad();
			if (active != 0) GATE_UPDATER.add(this);
		}
		super.onLoad();
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		if (!level.isClientSide) ports.onUnload();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (!level.isClientSide) ports.onRemove();
	}

	@Override
	public void onBreak(BlockState newState, boolean moving) {
		for (ItemStack stack : inv.items)
			ItemFluidUtil.dropStack(stack, level, worldPosition);
	}

	@Override
	public ContainerAutoCraft createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerAutoCraft(id, inv, this);
	}

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		switch(pkt.readByte()) {
		case 0: p2 = pkt.readInt(); break;
		case 1: p1 = pkt.readInt(); break;
		default: return;
		}
		updatePattern();
	}

}
