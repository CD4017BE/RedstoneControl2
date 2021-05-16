package cd4017be.rs_ctr2.item;

import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.part.MultiBlock;

import java.util.function.IntFunction;
import cd4017be.lib.item.DocumentedItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/**
 * @author CD4017BE */
public class MultiblockItem<T extends MultiBlock<T>> extends DocumentedItem
implements IGridItem {

	private final IntFunction<T> factory;

	public MultiblockItem(Properties p, IntFunction<T> factory) {
		super(p);
		this.factory = factory;
	}

	@Override
	public T createPart() {
		return factory.apply(-1);
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
		
		T part = factory.apply(pos);
		T other = part.findAdjacent(grid, part.bounds);
		if (!(other != null ? other.addVoxel(pos) : grid.addPart(part)))
			return ActionResultType.FAIL;
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
