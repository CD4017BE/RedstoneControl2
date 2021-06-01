package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.block_splitter;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.port.IBlockSupplier;
import cd4017be.lib.part.OrientedPart;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;


/**
 * @author CD4017BE */
public class SplitterB extends OrientedPart implements IBlockSupplier {

	IBlockSupplier src = IBlockSupplier.NOP;

	public SplitterB() {
		super(6);
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

}
