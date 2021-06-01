package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.lib.network.Sync;
import cd4017be.lib.render.model.PartModel;
import cd4017be.lib.render.model.TileEntityModel;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IFrame;
import cd4017be.rs_ctr2.api.IFrameOperator;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;


/**
 * @author CD4017BE */
public class Frame extends BaseTileEntity implements IFrame {

	/** bit[0...47 6*8]: distances, bit[48...59 3*4]: full rectangles */
	@Sync(to = ALL) public long dist;
	private long[] listeners = ArrayUtils.EMPTY_LONG_ARRAY;

	public Frame(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void clearCache() {
		super.clearCache();
		if (level.isClientSide || dist != 0) return;
		int lim = SERVER_CFG.frame_range.get();
		Mutable pos = new Mutable();
		sides: for (Direction d : Direction.values()) {
			pos.set(worldPosition);
			for (int i = 1; i <= lim; i++) {
				TileEntity te = level.getBlockEntity(pos.move(d));
				if (te instanceof Frame) {
					((Frame)te).setDist(d.getOpposite(), i);
					this.setDist(d, i);
					continue sides;
				}
			}
			setDist(d, 0);
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		BlockPos pos = worldPosition;
		for (Direction d : Direction.values()) {
			int i = getDist(d);
			if (i == 0) continue;
			TileEntity te = level.getBlockEntity(pos.relative(d, i));
			if (!(te instanceof Frame)) continue;
			int j = getDist(d.getOpposite());
			if (j != 0) j += i;
			((Frame)te).setDist(d.getOpposite(), j);
		}
		updateListeners();
	}

	private BlockPos pos(long p) {
		return worldPosition.offset(
			(short)p, (short)(p >> 16), (short)(p >> 32)
		);
	}

	private long pos(BlockPos pos) {
		long dx = pos.getX() - worldPosition.getX() & 0xffff;
		long dy = pos.getY() - worldPosition.getY() & 0xffff;
		long dz = pos.getZ() - worldPosition.getZ() & 0xffff;
		return dx | dy << 16 | dz << 32;
	}

	private void updateListeners() {
		for (int i = 0; i < listeners.length; i++) {
			long l = listeners[i];
			if (l == 0) continue;
			TileEntity te = level.getBlockEntity(pos(l));
			if (!(te instanceof IFrameOperator)) {
				listeners[i] = 0;
				continue;
			}
			((IFrameOperator)te).onFrameChange(this);
		}
	}

	@Override
	public void setDist(Direction d, int i) {
		if (i > 0xff) i = 0;
		int s = d.ordinal() * 8;
		dist = dist & ~(0xffL << s) | (long)i << s;
		updateListeners();
		clientDirty(true);
	}

	@Override
	public int getDist(Direction d) {
		return (int)(dist >> d.ordinal() * 8) & 0xff;
	}

	@Override
	public void addListener(BlockPos pos) {
		long p = pos(pos);
		int l = listeners.length, j = l;
		for (int i = 0; i < l; i++) {
			long p1 = listeners[i];
			if (p1 == p) return;
			if (p1 == 0 && i < j) j = i;
		}
		if (j >= l) listeners = Arrays.copyOf(listeners, Math.max(l + 1, l << 1));
		listeners[j] = p;
	}

	@Override
	public void removeListener(BlockPos pos) {
		long p = pos(pos);
		for (int i = 0; i < listeners.length; i++)
			if (listeners[i] == p) {
				listeners[i] = 0;
				return;
			}
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		if ((mode & SAVE) != 0)
			nbt.putLongArray("refs", listeners);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		if ((mode & SAVE) != 0) {
			long[] arr = nbt.getLongArray("refs");
			int j = 0;
			for (int i = 0; i < arr.length; i++) {
				long x = arr[i];
				if (x == 0) continue;
				if (i != j) arr[j] = x;
				j++;
			}
			listeners = Arrays.copyOf(arr, j);
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		requestModelDataUpdate();
	}

	@Override
	public IModelData getModelData() {
		ModelDataMap data = TileEntityModel.MODEL_DATA_BUILDER.build();
		ArrayList<PartModel> list = new ArrayList<>(6);
		data.setData(PartModel.PART_MODELS, list);
		for (Direction d : Direction.values())
			list.add(
				new PartModel(getDist(d) > 0 ? "1" : "0")
				.orient(Orientation.byBack(d).o, .5F, .5F, .5F)
			);
		return data;
	}

}
