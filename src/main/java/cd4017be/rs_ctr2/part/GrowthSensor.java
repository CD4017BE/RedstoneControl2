package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.growth_sensor;

import org.apache.commons.lang3.tuple.ImmutablePair;

import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;


/**
 * @author CD4017BE */
public class GrowthSensor extends Sensor {

	@Override
	protected int measure(ImmutablePair<BlockPos, ServerWorld> src) {
		BlockState state = src.right.getBlockState(src.left);
		Block block = state.getBlock();
		if (block instanceof IGrowable)
			return ((IGrowable)block).isValidBonemealTarget(
				src.right, src.left, state, false
			) ? 1 : 2;
		if (block instanceof IPlantable)
			return block.isRandomlyTicking(state) ? 1 : 2;
		return 0;
	}

	@Override
	public Item item() {
		return growth_sensor;
	}

}
