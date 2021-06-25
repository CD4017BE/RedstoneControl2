package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.block_send;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.port.IBlockSupplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;


/**
 * @author CD4017BE */
public class WirelessBlock extends Wireless implements IBlockSupplier {

	IBlockSupplier block = IBlockSupplier.NOP;

	public WirelessBlock() {
		super(block_send);
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port != 0) block = IBlockSupplier.of(handler);
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return --rec < 0 ? null : block.getBlock(rec);
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.block_send",
			IBlockSupplier.toString(block), link
		};
	}

}
