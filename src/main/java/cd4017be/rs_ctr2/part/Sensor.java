package cd4017be.rs_ctr2.part;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.port.IBlockSupplier;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;


/**
 * @author CD4017BE */
public abstract class Sensor extends OrientedPart
implements IBlockSupplier, ISignalReceiver, IProbeInfo {

	protected ISignalReceiver out = ISignalReceiver.NOP;
	protected IBlockSupplier block = this;
	@Sync public int clk, state;

	public Sensor() {
		super(3);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, IBlockSupplier.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Object getHandler(int port) {
		return port == 2 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) block = IBlockSupplier.of(handler, this);
		else if (port == 1) (out = ISignalReceiver.of(handler)).updateInput(state);
	}

	@Override
	public boolean isMaster(int port) {
		return port < 2;
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return onEdge() ? new ImmutablePair<>(
			host.pos().relative(orient.b, -1),
			(ServerWorld)host.world()
		) : null;
	}

	@Override
	public void updateInput(int value, int rec) {
		if ((~clk & (clk = value)) == 0) return;
		if (state == (state = measure(block.getBlock()))) return;
		out.updateInput(state);
	}

	protected abstract int measure(ImmutablePair<BlockPos, ServerWorld> block);

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.sensor", IBlockSupplier.toString(block), state, clk
		};
	}

}
