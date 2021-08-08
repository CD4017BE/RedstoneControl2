package cd4017be.rs_ctr2.part;

import static cd4017be.lib.tick.GateUpdater.TICK;

import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.GridPart;
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
	protected int t, exp;

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
	public GridPart setHost(IGridHost host) {
		cap = LazyOptional.empty();
		return super.setHost(host);
	}

	protected T get(T fallback) {
		if (t != TICK && !(
			Objects.equals(last, last = block.getBlock())
			&& TICK < exp && cap.isPresent()
		)) cap = getCap(last);
		return cap.orElse(fallback);
	}

	protected LazyOptional<T> getCap(ImmutablePair<BlockPos, ServerWorld> block) {
		t = TICK; //To not request block multiple times per tick
		if (block == null) return LazyOptional.empty();
		TileEntity te = block.right.getBlockEntity(block.left);
		if (te != null) {
			exp = Integer.MAX_VALUE;
			return te.getCapability(capability(), orient.b);
		}
		for (Entity e : block.right.getEntities(null, new AxisAlignedBB(block.left))) {
			LazyOptional<T> lo = e.getCapability(capability(), orient.b);
			if (lo.isPresent()) {
				exp = t + 10; //refresh regularly as entities may move around.
				return lo;
			}
		}
		exp = 0;
		return alternative(block);
	}

	protected LazyOptional<T> alternative(ImmutablePair<BlockPos, ServerWorld> block) {
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
