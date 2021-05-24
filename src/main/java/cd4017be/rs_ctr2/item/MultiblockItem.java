package cd4017be.rs_ctr2.item;

import cd4017be.api.grid.IGridHost;
import cd4017be.lib.item.GridItem;
import cd4017be.rs_ctr2.part.MultiBlock;

import java.util.function.IntFunction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;

/**
 * @author CD4017BE */
public class MultiblockItem<T extends MultiBlock<T>> extends GridItem {

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

}
