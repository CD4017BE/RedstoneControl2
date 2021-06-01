package cd4017be.rs_ctr2.api;

import static net.minecraft.util.Direction.*;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeTileEntity;

/**
 * @author CD4017BE */
public interface IFrame extends IForgeTileEntity {

	void setDist(Direction d, int i);
	int getDist(Direction d);
	void addListener(BlockPos pos);
	void removeListener(BlockPos pos);

	default BlockPos pos() {
		return getTileEntity().getBlockPos();
	}

	default boolean exists() {
		return !getTileEntity().isRemoved();
	}

	default IFrame next(Direction d) {
		int i = getDist(d);
		if (i == 0) return null;
		TileEntity te = getTileEntity();
		te = te.getLevel().getBlockEntity(te.getBlockPos().relative(d, i));
		return te instanceof IFrame ? (IFrame)te : null;
	}

	default int findRegion(int[] box) {
		BlockPos pos = pos();
		box[0] = box[1] = box[2] = pos.getX();
		box[3] = box[4] = box[5] = pos.getY();
		box[6] = box[7] = box[8] = pos.getZ();
		int used = 1 << 13, l;
		if ((l = getDist(WEST )) > 0) { box[0] -= l; used |= used >> 1; }
		if ((l = getDist(EAST )) > 0) { box[2] += l; used |= used << 1; }
		if ((l = getDist(DOWN )) > 0) { box[3] -= l; used |= used >> 3; }
		if ((l = getDist(UP   )) > 0) { box[5] += l; used |= used << 3; }
		if ((l = getDist(NORTH)) > 0) { box[6] -= l; used |= used >> 9; }
		if ((l = getDist(SOUTH)) > 0) { box[8] += l; used |= used << 9; }
		return used;
	}

}
