package cd4017be.rs_ctr2.item;

import static cd4017be.math.Linalg.sca;
import static cd4017be.math.Linalg.sub;
import static cd4017be.math.MCConv.blockRelVecF;
import static net.minecraft.util.Direction.getNearest;

import java.util.function.Supplier;

import cd4017be.rs_ctr2.Main;
import cd4017be.api.grid.IGridHost;
import cd4017be.lib.item.GridItem;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

/** @author CD4017BE */
public class OrientedPartItem<T extends OrientedPart> extends GridItem {

	protected final Supplier<T> factory;

	public OrientedPartItem(Properties p, Supplier<T> factory) {
		super(p);
		tab(Main.CREATIVE_TAB);
		this.factory = factory;
	}

	@Override
	public T createPart() {
		return factory.get();
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
		
		T part = createPart();
		position(part, pos, hit, player);
		if (!grid.addPart(part)) return ActionResultType.FAIL;
		onPlace(part, stack, player);
		return ActionResultType.SUCCESS;
	}

	protected void position(T part, int pos, BlockRayTraceResult hit, PlayerEntity player) {
		Direction d = hit.getDirection(), d1 = null;
		Orientation o;
		if (!player.isShiftKeyDown()) {
			d1 = d;
			d = Direction.orderedByNearest(player)[0];
			if (d == d1.getOpposite()) d1 = null;
		}
		if(d1 == null) {
			float[] vec = sca(3, blockRelVecF(hit.getLocation(), hit.getBlockPos()), 4F);
			sub(3, vec, (pos & 3) + .5F, (pos >> 2 & 3) + .5F, (pos >> 4 & 3) + .5F);
			vec[d.getAxis().ordinal()] = 0;
			d1 = getNearest(vec[0], vec[1], vec[2]);
		}
		o = Orientation.byBackUp(d, d1);
		if (o == null) o = Orientation.byBack(d);
		part.set(OrientedPart.pos(pos, o.inv()), o);
	}

	protected void onPlace(T part, ItemStack stack, PlayerEntity player) {
		if (!player.isCreative()) stack.shrink(1);
	}

}
