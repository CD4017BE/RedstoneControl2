package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.block_io;
import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.port.IBlockSupplier;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/**@author CD4017BE */
public class BlockIO extends OrientedPart implements IBlockSupplier {

	private ImmutablePair<BlockPos, ServerWorld> block;

	public BlockIO() {
		super(1);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, IBlockSupplier.TYPE_ID);
	}

	@Override
	public Item item() {
		return block_io;
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int port) {
		return false;
	}

	@Override
	public void setHost(IGridHost host) {
		super.setHost(host);
		block = host != null && onEdge() ? ImmutablePair.of(
			host.pos().relative(orient.b, -1),
			(ServerWorld)host.world()
		) : null;
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return block;
	}

}
