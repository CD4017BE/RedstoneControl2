package cd4017be.rs_ctr2.api.grid;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

import cd4017be.rs_ctr2.api.gate.IPortHolder;
import cd4017be.lib.network.INBTSynchronized;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class GridPart implements IPortHolder, INBTSynchronized {

	public IGridHost host;
	/** filled & 1 << (x + 4*y + 16*z) */
	public long bounds;
	/** x & 0x000f | y & 0x00f0 | z & 0x0f00 | type & 0xf000 */
	public final short[] ports;

	public GridPart(int ports) {
		this.ports = new short[ports];
	}

	/**@return the {@link IGridItem} that creates this part. */
	public abstract Item item();

	/**@return the ItemStack dropped on removal. */
	public ItemStack asItemStack() {
		return new ItemStack(item());
	}

	public void setHost(IGridHost host) {
		this.host = host;
	}

	/**@param b new bounds (must not overlap with other parts) */
	public void setBounds(long b) {
		long old = bounds;
		bounds = b;
		host.updateBounds();
		for (int i = 0; i < 6; i++) {
			long f = FACES[i];
			if ((old & f) != 0 ^ (b & f) == 0) continue;
			Direction d = Direction.from3DDataValue(i^1);
			if (analogOutput(d) > 0)
				host.updateNeighbor(d.getOpposite());
		}
		host.onPartChange();
	}

	public boolean isOpaque() {
		return true;
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		INBTSynchronized.super.storeState(nbt, mode);
		nbt.putString("id", item().getRegistryName().toString());
	}

	/**@param player
	 * @param hand used hand or null if left click
	 * @param hit original ray trace hit
	 * @param pos hit voxel pos
	 * @return action result */
	public ActionResultType onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand != null) return ActionResultType.PASS;
		if (!player.level.isClientSide && player.getMainHandItem().getItem() instanceof IGridItem) {
			IGridHost host = this.host;
			host.removePart(this);
			host.removeIfEmpty();
			if (!player.isCreative())
				ItemFluidUtil.dropStack(asItemStack(), player);
		}
		return ActionResultType.CONSUME;
	}

	/**@param model used to render the grid block
	 * @param opaque voxels for visibility testing */
	@OnlyIn(Dist.CLIENT)
	public abstract void fillModel(JitBakedModel model, long opaque);

	/**@param side the neighbor block is powered from
	 * @return redstone level emitted on touching outer faces */
	public int analogOutput(Direction side) {
		return 0;
	}

	/**@param side relative to neighbor block
	 * @return whether redstone should connect to this */
	public boolean connectRedstone(Direction side) {
		return false;
	}

	/**when an adjacent block changes
	 * @param world
	 * @param pos changed block's postion
	 * @param dir side of this grid */
	public void onBlockChange(World world, BlockPos pos, Direction dir) {
	}

	/**@param pos x & 0x03 | y & 0x0c | z & 0x30
	 * @param dir side of the cell
	 * @param type type & 7 | master & 8
	 * @return x & 0x000f | y & 0x00f0 | z & 0x0f00 | type & 0xf000 */
	public static short port(int pos, Direction dir, int type) {
		return (short)((pos << 1 & 6 | pos << 3 & 0x60 | pos << 5 & 0x600 | type << 12)
		+ 0x111 + (dir.getStepX() | dir.getStepY() << 4 | dir.getStepZ() << 8));
	}

	/**@param p0 first corner index
	 * @param p1 second corner index
	 * @return bounds with the given cuboid of voxels filled */
	public static long bounds(int p0, int p1) {
		long b = 1L << p0;
		if (p1 == p0) return b;
		int i = (p1 & 3) - (p0 & 3);
		for (;i > 0; i--) b |= b << 1;
		for (;i < 0; i++) b |= b >>> 1;
		i = (p1 & 12) - (p0 & 12) >> 2;
		for (;i > 0; i--) b |= b << 4;
		for (;i < 0; i++) b |= b >>> 4;
		i = (p1 & 48) - (p0 & 48) >> 4;
		for (;i > 0; i--) b |= b << 16;
		for (;i < 0; i++) b |= b >>> 16;
		return b;
	}

	/**@param dir BTNSWE index
	 * @return x->1, y->4, z->16 */
	public static int step(int dir) {
		return 0xb424 >>> dir * 3 & 0x15;
	}

	/**@param part optional old part to reuse
	 * @param nbt data
	 * @param mode sync mode
	 * @return loaded part */
	public static GridPart load(GridPart part, CompoundNBT nbt, int mode) {
		Item item = ITEMS.getValue(new ResourceLocation(nbt.getString("id")));
		if (part == null || part.item() != item)
			part = item instanceof IGridItem ? ((IGridItem)item).createPart() : null;
		if (part != null) part.loadState(nbt, mode);
		return part;
	}

	public static BlockState GRID_HOST_BLOCK;

	public static final long[] FACES = {
		0x000f_000f_000f_000fL,
		0xf000_f000_f000_f000L,
		0x0000_0000_0000_ffffL,
		0xffff_0000_0000_0000L,
		0x1111_1111_1111_1111L,
		0x8888_8888_8888_8888L
	};

}