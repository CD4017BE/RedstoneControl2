package cd4017be.rs_ctr2.api.grid;

import cd4017be.rs_ctr2.api.gate.IPortHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**Lets TileEntities have signal connections with
 * {@link GridPart}s or other {@link IGridPortHolder}s.
 * @author CD4017BE */
public interface IGridPortHolder extends IPortHolder {

	ExtGridPorts extPorts();

	World world();

	BlockPos pos();

	@Override
	default boolean isMaster(int channel) {
		return extPorts().isMaster(channel);
	}

}
