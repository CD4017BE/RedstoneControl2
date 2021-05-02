package cd4017be.rs_ctr2.util;

import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.shapes.VoxelShapePart;

/**I was working with the bounding boxes as 4x4x4 bit packed long anyway
 * so this saves unnecessary and inefficient conversion steps.
 * @author CD4017BE */
public final class VoxelShape4x4x4 extends VoxelShapePart {

	public long grid;

	public VoxelShape4x4x4() {
		super(4, 4, 4);
	}

	private int index(int x, int y, int z) {
		return x | y << 2 | z << 4;
	}

	@Override
	public boolean isEmpty() {
		return grid == 0L;
	}

	@Override
	public boolean isFull(int x, int y, int z) {
		return (grid >>> index(x, y, z) & 1) != 0;
	}

	@Override
	public void setFull(int x, int y, int z, boolean expandBounds, boolean filled) {
		long mask = 1L << index(x, y, z);
		if (filled) grid |= mask;
		else grid &= ~mask;
	}

	@Override
	public int firstFull(Axis axis) {
		long g = grid;
		if (g == 0) return 4;
		int a = axis.ordinal() << 2;
		if ((g & LAYERS[a  ]) != 0) return 0;
		if ((g & LAYERS[a+1]) != 0) return 1;
		if ((g & LAYERS[a+2]) != 0) return 2;
		return 3;
	}

	@Override
	public int lastFull(Axis axis) {
		long g = grid;
		if (g == 0) return 0;
		int a = axis.ordinal() << 2;
		if ((g & LAYERS[a+3]) != 0) return 4;
		if ((g & LAYERS[a+2]) != 0) return 3;
		if ((g & LAYERS[a+1]) != 0) return 2;
		return 1;
	}

	private static final long[] LAYERS = {
		0x1111_1111_1111_1111L,
		0x2222_2222_2222_2222L,
		0x4444_4444_4444_4444L,
		0x8888_8888_8888_8888L,
		0x000f_000f_000f_000fL,
		0x00f0_00f0_00f0_00f0L,
		0x0f00_0f00_0f00_0f00L,
		0xf000_f000_f000_f000L,
		0x0000_0000_0000_ffffL,
		0x0000_0000_ffff_0000L,
		0x0000_ffff_0000_0000L,
		0xffff_0000_0000_0000L
	};

}
