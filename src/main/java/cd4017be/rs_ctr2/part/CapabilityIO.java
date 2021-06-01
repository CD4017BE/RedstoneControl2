package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.IGridHost;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

/**@author CD4017BE */
public abstract class CapabilityIO<T> extends OrientedPart
implements NonNullConsumer<LazyOptional<T>> {

	protected T inv;

	public CapabilityIO(int ports) {
		super(ports);
	}

	protected abstract T fallback();
	protected abstract Capability<T> capability();

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int channel) {
		return false;
	}

	@Override
	public void setHost(IGridHost host) {
		super.setHost(host);
		inv = null;
	}

	@Override
	public void onTEChange(World world, BlockPos pos, Direction dir) {
		if (inv == fallback() && dir == orient.b.getOpposite()) inv = null;
	}

	@Override
	public void accept(LazyOptional<T> t) {
		if (host == null || !onEdge()) {
			inv = fallback();
			return;
		}
		Direction d = orient.b;
		TileEntity te = Utils.getTileAt(host.world(), host.pos().relative(d, -1));
		if (te != null) {
			LazyOptional<T> lo = te.getCapability(capability(), d);
			inv = lo.orElse(null);
			if (inv != null) {
				lo.addListener(this);
				return;
			}
		}
		inv = fallback();
	}

}
