package cd4017be.rs_ctr2.block;

import java.util.Random;

import cd4017be.rs_ctr2.Content;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;


/**
 * @author CD4017BE */
public class AccessPipe extends Block {

	public static final EnumProperty<Direction> BACK = EnumProperty.create("back", Direction.class);

	public AccessPipe(Properties p) {
		super(p);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> b) {
		b.add(BACK);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext cont) {
		return defaultBlockState().setValue(BACK, cont.getClickedFace().getOpposite());
	}

	@Override
	public VoxelShape getShape(
		BlockState state, IBlockReader world, BlockPos pos, ISelectionContext cont
	) {
		double x0 = .25, y0 = .25, z0 = .25, x1 = .75, y1 = .75, z1 = .75;
		switch(state.getValue(BACK)) {
		case DOWN: y0 = -.25; break;
		case UP: y1 = 1.25; break;
		case NORTH: z0 = -.25; break;
		case SOUTH: z1 = 1.25; break;
		case WEST: x0 = -.25; break;
		default: x1 = 1.25; break;
		}
		return VoxelShapes.box(x0, y0, z0, x1, y1, z1);
	}

	public boolean validSource(BlockState state) {
		Block block = state.getBlock();
		return block == this || block == Content.PIPE_CONTROLLER;
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
		return validSource(world.getBlockState(pos.relative(state.getValue(BACK))));
	}

	@Override
	public BlockState updateShape(
		BlockState state, Direction dir, BlockState adj_state,
		IWorld world, BlockPos pos, BlockPos adj_pos
	) {
		if (dir == state.getValue(BACK) && !validSource(adj_state))
			world.getBlockTicks().scheduleTick(pos, this, 1);
		return state;
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		if (!canSurvive(state, world, pos)) world.destroyBlock(pos, true);
	}

}
