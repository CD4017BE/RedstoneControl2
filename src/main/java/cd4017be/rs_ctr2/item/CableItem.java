package cd4017be.rs_ctr2.item;

import cd4017be.rs_ctr2.api.gate.IPortHolder.Port;
import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.part.Cable;

import static cd4017be.math.Linalg.*;
import static cd4017be.math.MCConv.blockRelVecF;
import static net.minecraft.util.Direction.getNearest;

import cd4017be.lib.item.DocumentedItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;


public class CableItem extends DocumentedItem implements IGridItem {

	final int type;

	public CableItem(Properties p, int type) {
		super(p);
		this.type = type;
	}

	@Override
	public ActionResultType onInteract(
		IGridHost grid, ItemStack stack, PlayerEntity player, Hand hand, BlockRayTraceResult hit
	) {
		if (hand == null) return ActionResultType.PASS;
		int pos = IGridHost.target(hit, true);
		if (pos < 0) return ActionResultType.PASS;
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		//position
		Direction d0 = hit.getDirection(), d;
		if (player.isShiftKeyDown()) {
			float[] vec = sca(3, blockRelVecF(hit.getLocation(), hit.getBlockPos()), 4F);
			sub(3, vec, (pos & 3) + .5F, (pos >> 2 & 3) + .5F, (pos >> 4 & 3) + .5F);
			vec[d0.getAxis().ordinal()] = 0;
			if (lenSq(3, vec) < .0625F) d = d0;
			else d = getNearest(vec[0], vec[1], vec[2]);
		} else d = Direction.orderedByNearest(player)[5];
		Cable part = new Cable(pos, d0.getOpposite(), d, type);
		//merge with existing wires:
		Port p0 = grid.findPort(null, part.ports[0]);
		Port p1 = grid.findPort(null, part.ports[1]);
		if (p0 != null) part.merge((GridPart)p0.host);
		if (p1 != null) part.merge((GridPart)p1.host);
		part.addTo(grid);
		if (!player.isCreative()) stack.shrink(1);
		return ActionResultType.SUCCESS;
	}

	@Override
	public GridPart createPart() {
		Cable c = new Cable();
		c.ports[0] = c.ports[1] = (short)(type << 12);
		return c;
	}

	public ActionResultType useOn(ItemUseContext context) {
		return placeAndInteract(context);
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
