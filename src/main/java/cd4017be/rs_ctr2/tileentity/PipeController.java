package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.network.Sync.Type.Enum;
import static cd4017be.lib.part.OrientedPart.port;
import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Content.ACCESS_PIPE;
import static cd4017be.rs_ctr2.Content.access_pipe;
import static cd4017be.rs_ctr2.block.AccessPipe.BACK;
import static net.minecraft.util.Direction.NORTH;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.ExtGridPorts;
import cd4017be.api.grid.port.*;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tick.GateUpdater;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemHandlerHelper;


/**
 * @author CD4017BE */
public class PipeController extends Machine
implements IInventoryAccess, IBlockSupplier, ISignalReceiver, IGate, IProbeInfo {

	public static int MAX_LEN = 256;

	ImmutablePair<BlockPos, ServerWorld> block;
	@Sync public BlockPos end;
	@Sync public int len, dir;
	@Sync(type = Enum) public Direction back = Direction.DOWN;
	@Sync public boolean update;
	private int tick;

	public PipeController(TileEntityType<?> type) {
		super(type);
	}

	@Override
	protected void init(ExtGridPorts ports, Orientation o) {
		ports.createPort(port(o, 0x05, NORTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x06, NORTH, IInventoryAccess.TYPE_ID), false, true);
		ports.createPort(port(o, 0x09, NORTH, IBlockSupplier.TYPE_ID), false, true);
		ports.createPort(port(o, 0x0a, NORTH, IBlockSupplier.TYPE_ID), false, true);
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public void getContent(ObjIntConsumer<ItemStack> inspector, int rec) {
		inspector.accept(new ItemStack(access_pipe, len), MAX_LEN);
	}

	@Override
	public int transfer(
		int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target, int rec
	) {
		ItemStack stack = new ItemStack(access_pipe);
		if (!filter.test(stack)) return 0;
		BlockPos pos = end;
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() != ACCESS_PIPE) {
			len = 0;
			if (!pos.equals(worldPosition)) {
				pos = worldPosition;
				block = null;
			}
			return 0;
		}
		if (!target.apply(stack).isEmpty()) return 0;
		level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		updateBlock(pos.relative(state.getValue(BACK)));
		len--;
		return 1;
	}

	@Override
	@SuppressWarnings("deprecation")
	public ItemStack insert(ItemStack stack, int rec) {
		if (len >= MAX_LEN || stack.getItem() != access_pipe) return stack;
		if (!ACCESS_PIPE.validSource(level.getBlockState(end))) {
			updateBlock(worldPosition);
			len = 0;
		}
		BlockPos pos = end.relative(back, -1);
		BlockState state = level.getBlockState(pos);
		BlockState place = ACCESS_PIPE.defaultBlockState().setValue(BACK, back);
		if (state != place) {
			if (!state.isAir(level, pos) && !state.getMaterial().isLiquid()) return stack;
			if (!level.setBlockAndUpdate(pos, place)) return stack;
			stack = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
		}
		updateBlock(pos);
		len++;
		return stack;
	}

	private void updateBlock(BlockPos pos) {
		end = pos;
		block = null;
		tick = -1;
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		if (tick != GateUpdater.TICK) {
			tick = GateUpdater.TICK;
			if (!level.isLoaded(end) || !ACCESS_PIPE.validSource(level.getBlockState(end))) block = null;
			else if (block == null) block = ImmutablePair.of(end.relative(back, -1), (ServerWorld)level);
		}
		return block;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (dir == (dir = value & 7) || update) return;
		update = true;
		GATE_UPDATER.add(this);
	}

	@Override
	public boolean evaluate() {
		update = false;
		if (unloaded) return false;
		back = orientation().apply(Direction.from3DDataValue(dir));
		updateBlock(end);
		return true;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.pipe_controller",
			dir, len, IBlockSupplier.toString(this)
		};
	}

	@Override
	public void setLevelAndPosition(World world, BlockPos pos) {
		super.setLevelAndPosition(world, pos);
		if (end == null) end = pos;
	}

}
