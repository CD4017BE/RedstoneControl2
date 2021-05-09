package cd4017be.rs_ctr2.item;

import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.part.Battery;
import cd4017be.lib.item.DocumentedItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;


public class BatteryItem extends DocumentedItem implements IGridItem {

	public BatteryItem(Properties p) {
		super(p);
	}

	@Override
	public GridPart createPart() {
		return new Battery();
	}

	@Override
	public ActionResultType onInteract(
		IGridHost grid, ItemStack stack, PlayerEntity player,
		Hand hand, BlockRayTraceResult hit
	) {
		if (hand == null) return ActionResultType.PASS;
		int pos = IGridHost.target(hit, true);
		if (pos < 0) return ActionResultType.PASS;
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		
		int pos1 = IGridHost.target(hit, false);
		Battery part = pos1 < 0 ? null : (Battery)grid.findPart(
			p -> p instanceof Battery && (p.bounds >> pos1 & 1) != 0
		);
		if (!(
			part != null ? part.addVoxel(pos)
			: grid.addPart(new Battery(pos))
		)) return ActionResultType.FAIL;
		if (!player.isCreative()) stack.shrink(1);
		return ActionResultType.SUCCESS;
	}

	public ActionResultType useOn(ItemUseContext context) {
		return placeAndInteract(context);
	}

	@Override
	public boolean canAttackBlock(
		BlockState state, World world, BlockPos pos, PlayerEntity player
	) {
		if (player.isCreative())
			world.getBlockState(pos).attack(world, pos, player);
		return false;
	}

}
