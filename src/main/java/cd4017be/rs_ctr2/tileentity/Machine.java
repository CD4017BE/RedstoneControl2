package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.network.Sync.SAVE;
import cd4017be.api.grid.ExtGridPorts;
import cd4017be.api.grid.IGridPortHolder;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.Orientation;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**@author CD4017BE */
public abstract class Machine extends BaseTileEntity implements IGridPortHolder {

	protected final ExtGridPorts ports = new ExtGridPorts(this);

	public Machine(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public ExtGridPorts extPorts() {
		return ports;
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		if ((mode & SAVE) != 0)
			nbt.put("ports", ports.serializeNBT());
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		if ((mode & SAVE) != 0 && nbt.contains("ports", NBT.TAG_LONG_ARRAY))
			ports.deserializeNBT((LongArrayNBT)nbt.get("ports"));
	}

	@Override
	public void clearCache() {
		super.clearCache();
		if (level.isClientSide) return;
		ports.clear();
		init(ports, orientation());
	}

	protected abstract void init(ExtGridPorts ports, Orientation o);

	@Override
	public void onLoad() {
		if (!level.isClientSide) ports.onLoad();
		super.onLoad();
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		if (!level.isClientSide) ports.onUnload();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (!level.isClientSide) ports.onRemove();
	}

	@Override
	public World world() {
		return level;
	}

	@Override
	public BlockPos pos() {
		return worldPosition;
	}

}
