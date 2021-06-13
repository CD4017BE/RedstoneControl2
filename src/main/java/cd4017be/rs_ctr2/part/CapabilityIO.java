package cd4017be.rs_ctr2.part;

import static cd4017be.lib.tick.GateUpdater.TICK;

import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.port.IBlockSupplier;
import cd4017be.lib.part.OrientedPart;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**@author CD4017BE */
public abstract class CapabilityIO<T> extends OrientedPart implements IBlockSupplier {

	protected IBlockSupplier block = this;
	protected ImmutablePair<BlockPos, ServerWorld> last;
	LazyOptional<T> cap = LazyOptional.empty();
	int t, exp;

	public CapabilityIO(int ports) {
		super(ports);
	}

	protected abstract Capability<T> capability();

	@Override
	public Object getHandler(int port) {
		return port != 0 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) {
			block = IBlockSupplier.of(handler, this);
			last = null;
		}
	}

	@Override
	public boolean isMaster(int port) {
		return port == 0;
	}

	@Override
	public void setHost(IGridHost host) {
		super.setHost(host);
		cap = LazyOptional.empty();
	}

	protected T get(T fallback) {
		if (t != TICK && !(
			Objects.equals(last, last = block.getBlock())
			&& cap.isPresent() && TICK < exp
		)) cap = getCap(last);
		return cap.orElse(fallback);
	}

	protected LazyOptional<T> getCap(ImmutablePair<BlockPos, ServerWorld> block) {
		t = TICK; //To not request block multiple times per tick
		if (block == null) return LazyOptional.empty();
		TileEntity te = last.right.getBlockEntity(last.left);
		if (te != null) {
			exp = Integer.MAX_VALUE;
			return te.getCapability(capability(), orient.b);
		}
		for (Entity e : last.right.getEntities(null, new AxisAlignedBB(last.left))) {
			LazyOptional<T> lo = e.getCapability(capability(), orient.b);
			if (lo.isPresent()) {
				exp = t + 10; //refresh regularly as entities may move around.
				return lo;
			}
		}
		return LazyOptional.empty();
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return last != null ? last
		: onEdge() ? new ImmutablePair<>(
			host.pos().relative(orient.b, -1),
			(ServerWorld)host.world()
		) : null;
	}

}
