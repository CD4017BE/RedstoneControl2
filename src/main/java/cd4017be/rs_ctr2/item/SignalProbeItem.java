package cd4017be.rs_ctr2.item;

import cd4017be.api.grid.GridPart;
import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.IGridItem;
import cd4017be.lib.item.DocumentedItem;
import cd4017be.lib.network.Sync;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/** @author CD4017BE */
public class SignalProbeItem extends DocumentedItem implements IGridItem {

	public SignalProbeItem(Properties p) {
		super(p);
	}

	@Override
	public GridPart createPart() {
		return null;
	}

	@Override
	public ActionResultType onInteract(
		IGridHost grid, ItemStack stack, PlayerEntity player, Hand hand, BlockRayTraceResult hit
	) {
		if (hand == null) return ActionResultType.PASS;
		int pos = IGridHost.target(hit, false);
		if (pos < 0) return ActionResultType.PASS;
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		nbt.putByte("gp", (byte)pos);
		nbt.putLong("bp", hit.getBlockPos().asLong());
		return ActionResultType.SUCCESS;
	}

	@Override
	public void inventoryTick(
		ItemStack stack, World world, Entity entity, int slot, boolean sel
	) {
		if(!sel || world.isClientSide) return;
		CompoundNBT nbt = stack.getOrCreateTag();
		nbt.remove("part");
		BlockPos pos = BlockPos.of(nbt.getLong("bp"));
		if (!pos.closerThan(entity.position(), 8)) return;
		TileEntity te = world.getBlockEntity(pos);
		if (!(te instanceof IGridHost)) return;
		IGridHost host = (IGridHost)te;
		long m = 1L << nbt.getByte("gp");
		GridPart part = host.findPart(p -> (p.bounds & m) != 0 && p.ports.length > 0);
		if (part == null) return;
		CompoundNBT tag = new CompoundNBT();
		part.storeState(tag, Sync.SAVE);
		nbt.put("part", tag);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || oldStack.getItem() != newStack.getItem();
	}

	@Override
	public boolean canAttackBlock(
		BlockState state, World world, BlockPos pos, PlayerEntity player
	) {
		if (!world.isClientSide && player.isCreative())
			world.getBlockState(pos).attack(world, pos, player);
		return false;
	}

}
