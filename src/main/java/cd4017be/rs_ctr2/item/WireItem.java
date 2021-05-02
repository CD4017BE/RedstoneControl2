package cd4017be.rs_ctr2.item;

import cd4017be.rs_ctr2.api.gate.IPortHolder.Port;
import cd4017be.rs_ctr2.api.gate.ISignalReceiver;
import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.part.JumperWire;
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


public class WireItem extends DocumentedItem implements IGridItem {

	public WireItem(Properties p) {
		super(p);
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
		Direction d = Direction.orderedByNearest(player)[5];
		JumperWire part = new JumperWire(pos, hit.getDirection().getOpposite(), d, ISignalReceiver.TYPE_ID);
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
		return new JumperWire();
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
