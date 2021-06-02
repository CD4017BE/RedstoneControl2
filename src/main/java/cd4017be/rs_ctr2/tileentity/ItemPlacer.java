package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.lib.part.OrientedPart.port;
import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.math.Linalg.*;
import static cd4017be.math.Orient.orient;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Hand.MAIN_HAND;
import static net.minecraft.util.Hand.OFF_HAND;
import static net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack;

import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.mojang.authlib.GameProfile;

import cd4017be.api.grid.ExtGridPorts;
import cd4017be.api.grid.port.*;
import cd4017be.lib.block.BlockTE.ITEPlace;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import cd4017be.lib.util.SaferFakePlayer;
import cd4017be.rs_ctr2.api.IProbeInfo;
import cd4017be.rs_ctr2.container.ContainerItemPlacer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;


/**
 * @author CD4017BE */
public class ItemPlacer extends Machine
implements IGate, IBlockSupplier, ISignalReceiver, ITEPlace,
IUnnamedContainerProvider, IProbeInfo {

	private static final int R_SUCCESS = 0, R_UNLOADED = 1,
	R_UNBREAKABLE = 2, R_INV_FULL = 4, R_NO_ENERGY = 8;
	private static final CompoundNBT DEFAULT_DATA = new CompoundNBT();
	static {
		DEFAULT_DATA.putString("name", "FakePlayer");
		DEFAULT_DATA.putUUID("uuid", new UUID(0, 0));
	}

	private CompoundNBT playerData = DEFAULT_DATA;
	private FakePlayer player = null;
	IBlockSupplier target = this;
	ItemStack oldStack;
	IEnergyAccess energy = IEnergyAccess.NOP;
	ISignalReceiver out = ISignalReceiver.NOP;
	IInventoryAccess inv = IInventoryAccess.NOP;
	@Sync public int clk, res;
	@Sync(to = SAVE|GUI)
	public int aim0;
	/**bit[0-5]: hotbarSlot, bit[7]: no restock,
	 * bit[8,9]: yaw, bit[10,11]: pitch, bit[12]: sneak, bit[14]: replaceBlock, bit[15]: airUse,
	 * bit[16-19]: pixelX, bit[20-23]: pixelY */
	public int aim;
	@Sync public boolean active;

	public ItemPlacer(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void onPlace(BlockState state, ItemStack stack, LivingEntity entity) {
		if (level instanceof ServerWorld && entity instanceof PlayerEntity) {
			player = new SaferFakePlayer((ServerWorld)level, ((PlayerEntity)entity).getGameProfile());
			if (playerData != null) {
				player.inventory.load(playerData.getList("inv", NBT.TAG_COMPOUND));
				player.inventory.selected = playerData.getByte("sel") & 0xff;
				playerData = null;
			}
		}
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		return port == 5 ? (v, r) -> aim0 = v
			: port == 0 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		switch(port) {
		case 1: out = ISignalReceiver.of(handler); break;
		case 2: energy = IEnergyAccess.of(handler); break;
		case 3: inv = IInventoryAccess.of(handler); break;
		case 4: target = IBlockSupplier.of(handler, this); break;
		}
	}

	@Override
	protected void init(ExtGridPorts ports, Orientation o) {
		ports.createPort(port(o, 0x05, NORTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x06, NORTH, ISignalReceiver.TYPE_ID), true, true);
		ports.createPort(port(o, 0x09, NORTH, IEnergyAccess.TYPE_ID), true, true);
		ports.createPort(port(o, 0x0a, NORTH, IInventoryAccess.TYPE_ID), true, true);
		ports.createPort(port(o, 0x35, SOUTH, IBlockSupplier.TYPE_ID), true, true);
		ports.createPort(port(o, 0x01, NORTH, ISignalReceiver.TYPE_ID), false, true);
	}

	@Override
	public void updateInput(int value, int rec) {
		if ((~clk & (clk = value)) != 0 && !active) {
			active = true;
			GATE_UPDATER.add(this);
		}
	}

	@Override
	public boolean evaluate() {
		active = false;
		aim = aim0;
		return !unloaded;
	}

	@Override
	public void latchOut() {
		IProfiler profiler = level.getProfiler();
		profiler.push("rs_ctr2:ItemPlacer");
		int res = process(profiler, getPlayer());
		profiler.pop();
		if (res != this.res) out.updateInput(this.res = res);
	}

	private int process(IProfiler profiler, FakePlayer player) {
		if (oldStack != null) {
			if (!restock(player)) return R_INV_FULL;
			oldStack = null;
			return R_SUCCESS;
		}
		ImmutablePair<BlockPos, ServerWorld> b = target.getBlock();
		if (b == null) return R_UNLOADED;
		BlockPos pos = b.left;
		ServerWorld world = b.right;
		int e = -SERVER_CFG.block_break.get();
		if (energy.transferEnergy(e, true) != e) return R_NO_ENERGY;
		BlockRayTraceResult res = setupInteraction(player, world, pos, orientation(), aim);
		Hand hand = player.inventory.selected < 36 ? MAIN_HAND : OFF_HAND;
		ItemStack stack = player.getItemInHand(hand);
		oldStack = stack.copy();
		ActionResultType ar = interact(player, stack, hand, res, (aim & 0x4000) != 0);
		if (ar == null) return R_UNBREAKABLE;
		energy.transferEnergy(e, false);
		if ((aim & 0x80) == 0 && !restock(player)) return R_INV_FULL;
		oldStack = null;
		return ar.consumesAction() ? R_SUCCESS : R_UNBREAKABLE;
	}

	private boolean restock(FakePlayer player) {
		PlayerInventory pinv = player.inventory;
		int i = pinv.selected;
		ItemStack stack = pinv.getItem(i), old = oldStack;
		if (ItemStack.isSame(stack, old)) return true;
		if (!stack.isEmpty()) {
			ItemStack stack1 = inv.apply(stack);
			pinv.setItem(i, stack1);
			if (!stack1.isEmpty()) return false;
		}
		if (old.isEmpty()) return true;
		return inv.transfer(old.getMaxStackSize(), old::sameItem, s -> {
			ItemStack s1 = pinv.getItem(i);
			if (s1.isEmpty()) pinv.setItem(i, s);
			else if (!canItemStacksStack(s1, s)) return s;
			else s1.grow(s.getCount());
			return ItemStack.EMPTY;
		}) > 0;
	}

	private static BlockRayTraceResult setupInteraction(
		FakePlayer player, ServerWorld world, BlockPos pos, Orientation o, int aim
	) {
		int i = aim & 0x3f;
		player.inventory.selected = i < 36 ? i : 40;
		player.setPose((aim & 0x1000) != 0 ? Pose.CROUCHING : Pose.STANDING);
		boolean air = (aim & 0x8000) != 0;
		//create aim vector
		float[] p = {
			.46875F - (aim >> 16 & 15) * .0625F,
			(aim >> 20 & 15) * .0625F - .46875F,
			air ? 0 : -2
		};
		o = o.apply(Orientation.byIndex(aim = aim >> 8 & 15));
		dadd(3, orient(o.o, p), .5F);
		Vector3d vec = new Vector3d(
			(double)p[0] + pos.getX(),
			(double)p[1] + pos.getY(),
			(double)p[2] + pos.getZ()
		);
		//position & orient player
		player.level = world;
		player.moveTo(
			vec.x, vec.y - player.getEyeHeight(), vec.z,
			(aim & 3) * 90, (((aim >> 2) + 1 & 3) - 1) * -90
		);
		player.xRotO = player.xRot;
		player.yHeadRotO = player.yHeadRot =
		player.yBodyRotO = player.yBodyRot =
		player.yRotO = player.yRot;
		//ray trace
		if (air) return null;
		p = orient(o.o, vec(0, 0, -3.5F));
		Vector3d dir = vec.add(p[0], p[1], p[2]);
		BlockState state = world.getBlockState(pos);
		BlockRayTraceResult res = state.getShape(world, pos).clip(vec, dir, pos);
		if (res != null) return res;
		return new BlockRayTraceResult(dir, o.b.getOpposite(), pos, false);
	}

	private static ActionResultType interact(
		FakePlayer player, ItemStack stack, Hand hand,
		BlockRayTraceResult res, boolean replace
	) {
		PlayerInteractionManager pim = player.gameMode;
		if (res == null)
			return stack.isEmpty() ? ActionResultType.PASS
				: pim.useItem(player, player.level, stack, hand);
		if (replace && !new BlockItemUseContext(player, hand, stack, res).replacingClickedOnBlock())
			return null;
		return pim.useItemOn(player, player.level, stack, hand, res);
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return ports.isLinked(4) ? null //prevent interacting connected cables
			: new ImmutablePair<>(worldPosition.relative(orientation().b), (ServerWorld)level);
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		if ((mode & SAVE) != 0) {
			if (oldStack != null) nbt.put("stock", oldStack.serializeNBT());
			if (player != null) {
				CompoundNBT tag = new CompoundNBT();
				tag.put("inv", player.inventory.save(new ListNBT()));
				tag.putUUID("uuid", player.getGameProfile().getId());
				tag.putString("name", player.getGameProfile().getName());
				tag.putByte("sel", (byte)player.inventory.selected);
				nbt.put("FP", tag);
			} else if (playerData != null) nbt.put("FP", playerData);
		}
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		if ((mode & SAVE) != 0) {
			oldStack = !nbt.contains("stock", NBT.TAG_COMPOUND) ? null
				: ItemStack.of(nbt.getCompound("stock"));
			playerData = nbt.getCompound("FP");
			aim = aim0;
		}
	}

	public FakePlayer getPlayer() {
		if (player != null) return player;
		CompoundNBT nbt = playerData;
		if (!nbt.contains("uuid", NBT.TAG_INT_ARRAY))
			nbt.put("uuid", DEFAULT_DATA.get("uuid"));
		GameProfile gp = new GameProfile(nbt.getUUID("uuid"), nbt.getString("name"));
		player = new SaferFakePlayer((ServerWorld)level, gp);
		player.inventory.load(nbt.getList("inv", NBT.TAG_COMPOUND));
		player.inventory.selected = nbt.getByte("sel") & 0xff;
		playerData = null;
		return player;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return getPlayer().getCapability(cap, side);
	}

	@Override
	public ContainerItemPlacer createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerItemPlacer(id, inv, this);
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.item_placer", clk, aim0, res,
			-energy.transferEnergy(-Integer.MAX_VALUE, true),
			inv != IInventoryAccess.NOP,
			IBlockSupplier.toString(target)
		};
	}

}
