package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.hardness_sensor;

import org.apache.commons.lang3.tuple.ImmutablePair;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;


/**
 * @author CD4017BE */
public class HardnessSensor extends Sensor {

	@Override
	public Item item() {
		return hardness_sensor;
	}

	@Override
	protected int measure(ImmutablePair<BlockPos, ServerWorld> block) {
		if (block == null) return -1;
		BlockPos pos = block.left;
		ServerWorld world = block.right;
		float h = world.getBlockState(pos).getDestroySpeed(world, pos);
		return h < 0 ? Integer.MAX_VALUE : (int)(100F * h);
	}

}
