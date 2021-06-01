package cd4017be.rs_ctr2.api;

import java.util.function.Consumer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

/**
 * @author CD4017BE */
public interface IFrameOperator {

	/**
	 * @param frame */
	void onFrameChange(IFrame frame);

	/**@param world
	 * @param origin point to search from
	 * @param range max distance
	 * @return closest frame along any of 6 directions */
	static IFrame findNearest(World world, BlockPos origin, int range) {
		Direction[] dirs = Direction.values();
		Mutable pos = new Mutable();
		for (int i = 1; i <= range; i++)
			for (Direction d : dirs) {
				TileEntity te = world.getBlockEntity(pos.set(origin).move(d, i));
				if (te instanceof IFrame) return (IFrame)te;
			}
		return null;
	}

	/**@param world
	 * @param box [x-, y-, z-, x0, y0, z0, x+, y+, z+]
	 * @param points 3x3x3 bitmap of required frames
	 * @param action to do for each found frame
	 * @return 3x3x3 bitmap of found frames */
	static int findFrames(World world, int[] box, int points, Consumer<IFrame> action) {
		Mutable pos = new Mutable();
		int present = 0;
		for (int l = 0, k = 6; k < 9; k++)
			for (int j = 3; j < 6; j++)
				for (int i = 0; i < 3; i++, l++) {
					if ((points >> l & 1) == 0) continue;
					pos.set(box[i], box[j], box[k]);
					TileEntity te = world.getBlockEntity(pos);
					if (!(te instanceof IFrame)) continue;
					action.accept((IFrame)te);
					present |= 1 << l;
				}
		return present;
	}

}
