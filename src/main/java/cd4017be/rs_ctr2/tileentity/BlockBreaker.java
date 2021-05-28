package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.part.OrientedPart.port;
import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;

import java.util.ArrayList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import cd4017be.api.grid.ExtGridPorts;
import cd4017be.api.grid.port.*;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Orientation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/**@author CD4017BE */
public class BlockBreaker extends Machine
implements ISignalReceiver, IGate, IBlockSupplier {

	private static final int R_SUCCESS = 0, R_UNLOADED = 1,
	R_UNBREAKABLE = 2, R_NO_ENERGY = 4, R_INV_FULL = 8;

	ArrayList<ItemStack> drops = new ArrayList<>();
	IBlockSupplier target = this;
	IEnergyAccess energy = IEnergyAccess.NOP;
	ISignalReceiver out = ISignalReceiver.NOP;
	IInventoryAccess inv = IInventoryAccess.NOP;
	@Sync public int clk, res;
	@Sync public boolean active;

	public BlockBreaker(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public Object getHandler(int port) {
		return port == 0 ? this : null;
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
		return !unloaded;
	}

	@Override
	public void latchOut() {
		IProfiler profiler = level.getProfiler();
		profiler.push("rs_ctr2:BlockBreaker");
		int res = process(profiler);
		profiler.pop();
		if (res != this.res) out.updateInput(this.res = res);
	}

	private int process(IProfiler profiler) {
		if (!drops.isEmpty()) return eject();
		ImmutablePair<BlockPos, ServerWorld> b = target.getBlock();
		if (b == null) return R_UNLOADED;
		BlockPos pos = b.left;
		ServerWorld world = b.right;
		BlockState state = world.getBlockState(pos);
		int e = -(int)(state.getDestroySpeed(world, pos) * SERVER_CFG.hardness_break.get())
			- SERVER_CFG.block_break.get();
		if (energy.transferEnergy(e, true) != e) return R_NO_ENERGY;
		TileEntity te = state.hasTileEntity() ? world.getBlockEntity(pos) : null;
		if (!world.removeBlock(pos, false)) return R_UNBREAKABLE;
		energy.transferEnergy(e, false);
		//Minecraft's loot table code looks potentially performance heavy, let's see how bad it really is ...
		profiler.push("collect_loot");
		for (ItemStack stack : Block.getDrops(state, world, pos, te))
			addDrop(stack);
		if (te != null)
			for (ItemEntity ie : world.getLoadedEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(pos))) {
				addDrop(ie.getItem());
				world.despawn(ie);
			}
		profiler.pop();
		return drops.isEmpty() ? R_SUCCESS : R_INV_FULL;
	}

	private int eject() {
		for (int i = drops.size() - 1; i >= 0; i--) {
			ItemStack stack = inv.apply(drops.get(i));
			if (stack.isEmpty()) drops.remove(i);
			else drops.set(i, stack);
		}
		return drops.isEmpty() ? R_SUCCESS : R_INV_FULL;
	}

	private void addDrop(ItemStack stack) {
		if (!(stack = inv.apply(stack)).isEmpty())
			drops.add(stack);
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return ports.isLinked(4) ? null //prevent breaking connected cables
			: new ImmutablePair<>(worldPosition.relative(orientation().b), (ServerWorld)level);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		for (ItemStack stack : drops)
			ItemFluidUtil.dropStack(stack, level, worldPosition);
	}

}
