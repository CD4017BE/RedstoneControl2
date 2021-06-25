package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.block_splitter;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.port.IBlockSupplier;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;


/**
 * @author CD4017BE */
public class SplitterB extends OrientedPart
implements IBlockSupplier, IProbeInfo {

	IBlockSupplier src = IBlockSupplier.NOP;

	public SplitterB() {
		super(6);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, TYPE_ID);
		setPort(1, pos, Direction.SOUTH, TYPE_ID);
		setPort(2, pos, Direction.DOWN, TYPE_ID);
		setPort(3, pos, Direction.UP, TYPE_ID);
		setPort(4, pos, Direction.WEST, TYPE_ID);
		setPort(5, pos, Direction.EAST, TYPE_ID);
	}

	@Override
	public Item item() {
		return block_splitter;
	}

	@Override
	public Object getHandler(int port) {
		return port == 0 ? null : this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) src = IBlockSupplier.of(handler);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel == 0;
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return --rec < 0 ? null : src.getBlock(rec);
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.block_splitter",
			IBlockSupplier.toString(src)
		};
	}

}
