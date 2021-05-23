package cd4017be.rs_ctr2.item;

import cd4017be.lib.item.DocumentedItem;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

/**@author CD4017BE */
public abstract class GridItem extends DocumentedItem implements IGridItem {

	public GridItem(Properties p) {
		super(p);
	}

	public ActionResultType useOn(ItemUseContext context) {
		return placeAndInteract(context);
	}

	@Override
	public boolean
	doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
		return world.getBlockEntity(pos) instanceof IGridHost;
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
