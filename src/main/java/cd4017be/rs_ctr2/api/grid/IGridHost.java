package cd4017be.rs_ctr2.api.grid;

import static cd4017be.math.Linalg.*;
import static cd4017be.math.MCConv.blockRelVecF;
import static cd4017be.math.MCConv.dirVecF;
import static cd4017be.rs_ctr2.api.grid.GridPart.L_INNER;
import static cd4017be.rs_ctr2.api.grid.GridPart.L_OUTER;

import java.util.function.Predicate;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

/**Implemented by the TileEntity that is hosting {@link GridPart}s.
 * @author CD4017BE */
public interface IGridHost extends IGridPortHolder {

	GridPart findPart(Predicate<GridPart> filter);
	Port findPort(GridPart except, short port);
	void removePart(GridPart part);
	boolean addPart(GridPart part);
	void onPartChange();
	void updateBounds();
	void removeIfEmpty();
	void updateNeighbor(Direction d);

	@Override
	default Object getHandler(int port) {
		Port p = findPort(null, extPorts().getPort(port));
		return p != null ? p.getHandler() : null;
	}

	@Override
	default void setHandler(int port, Object handler) {
		Port p = findPort(null, extPorts().getPort(port));
		if (p != null) p.setHandler(handler);
	}

	default GridPart getPart(int pos, byte layer) {
		long m = 1L << pos;
		return findPart(p ->
			(p.bounds & m) != 0 && (layer == 0 || layer + p.getLayer() != 0)
		);
	}

	default ActionResultType onInteract(
		PlayerEntity player, Hand hand, BlockRayTraceResult hit
	) {
		partInteract: {
			int pos = target(hit, false);
			if (pos < 0) break partInteract;
			GridPart part = getPart(pos, L_OUTER);
			if (part == null) part = getPart(pos, L_INNER);
			if (part == null) break partInteract;
			ActionResultType res = part.onInteract(player, hand, hit, pos);
			if (res.consumesAction()) return res;
		}
		ItemStack stack = player.getItemInHand(hand == null ? Hand.MAIN_HAND : hand);
		return stack.getItem() instanceof IGridItem
			? ((IGridItem)stack.getItem()).onInteract(this, stack, player, hand, hit)
			: ActionResultType.PASS;
	}

	static int target(BlockRayTraceResult hit, boolean adjacent) {
		float[] vec = sca(3, blockRelVecF(hit.getLocation(), hit.getBlockPos()), 4F);
		add(3, vec, dirVecF(hit.getDirection(), adjacent ? .5F : -.5F));
		if (!allInRange(3, vec, 0F, 4F)) return -1;
		return (int)vec[0] | (int)vec[1] << 2 | (int)vec[2] << 4;
	}

	/**@param port description of the port (port - 0x111 for other side)
	 * @return adjacent voxel index or -1 if outside grid */
	static int posOfport(int port) {
		if ((port & 0x888) != 0) return -1;
		return port >> 1 & 3 | port >> 3 & 12 | port >> 5 & 48;
	}

}