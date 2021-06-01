package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.lib.part.OrientedPart.port;
import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static net.minecraft.util.Direction.NORTH;
import org.apache.commons.lang3.tuple.ImmutablePair;

import cd4017be.api.grid.ExtGridPorts;
import cd4017be.api.grid.port.*;
import cd4017be.lib.block.BlockTE.ITEInteract;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IFrame;
import cd4017be.rs_ctr2.api.IFrameOperator;
import cd4017be.rs_ctr2.api.IProbeInfo;
import cd4017be.rs_ctr2.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;


/**
 * @author CD4017BE */
public class FrameController extends Machine
implements IFrameOperator, IBlockSupplier, IGate, IProbeInfo, ITEInteract {

	public final int[] region = new int[9];
	public ImmutablePair<BlockPos, ServerWorld> block;
	@Sync(to = ALL)
	public int frames, missing;
	@Sync public int dx, dy, dz;
	@Sync public boolean active;
	@Sync(to = ALL) public boolean visible = true;

	public FrameController(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public Object getHandler(int port) {
		switch(port) {
		case 0: return (ISignalReceiver)(v, r) -> update(dx, dx = v);
		case 1: return (ISignalReceiver)(v, r) -> update(dy, dy = v);
		case 2: return (ISignalReceiver)(v, r) -> update(dz, dz = v);
		case 3: return this;
		default: return null;
		}
	}

	private void update(int old, int val) {
		if (old == val || active) return;
		active = true;
		GATE_UPDATER.add(this);
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (unloaded) return false;
		if (missing < 0) updateFrame();
		block: {
			block = null;
			if (missing != 0) break block;
			int by = region[4] + dy;
			if (by < region[3] || by > region[5]) break block;
			int bx = region[1], bz = region[7];
			switch(orientation()) {
			default:  bx += dx; bz += dz; break;
			case N12: bx -= dx; bz -= dz; break;
			case E12: bx += dz; bz -= dx; break;
			case W12: bx -= dz; bz += dx; break;
			}
			if (bx < region[0] || bx > region[2]) break block;
			if (bz < region[6] || bz > region[8]) break block;
			block = ImmutablePair.of(new BlockPos(bx, by, bz), (ServerWorld)level);
		}
		clientDirty(false);
		return false;
	}

	@Override
	public ImmutablePair<BlockPos, ServerWorld> getBlock(int rec) {
		return block;
	}

	@Override
	protected void init(ExtGridPorts ports, Orientation o) {
		ports.createPort(port(o, 0x03, NORTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x02, NORTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x01, NORTH, ISignalReceiver.TYPE_ID), false, true);
		ports.createPort(port(o, 0x00, NORTH, IBlockSupplier.TYPE_ID), false, true);
		missing |= Integer.MIN_VALUE;
		update(0, 1);
	}

	private void updateFrame() {
		IFrame frame = originFrame();
		if (frame == null) {
			setNullRegion();
			return;
		}
		frames = frame.findRegion(region);
		BlockPos pos = worldPosition;
		missing = frames & ~IFrameOperator.findFrames(level, region, frames, f -> f.addListener(pos));
	}

	private IFrame originFrame() {
		boolean valid = region[2] > region[0] && region[5] > region[3] && region[8] > region[6];
		BlockPos pos = worldPosition;
		if (valid) {
			IFrameOperator.findFrames(level, region, frames & ~missing, f -> f.removeListener(pos));
			TileEntity te = level.getBlockEntity(new BlockPos(region[1], region[4], region[7]));
			if (te instanceof IFrame) {
				return (IFrame)te;
			}
		}
		return IFrameOperator.findNearest(level, pos, SERVER_CFG.device_range.get());
	}

	private void setNullRegion() {
		region[0] = region[1] = region[2] = worldPosition.getX();
		region[3] = region[4] = region[5] = worldPosition.getY();
		region[6] = region[7] = region[8] = worldPosition.getZ();
		frames = 0;
		missing = -1;
	}

	@Override
	public void onFrameChange(IFrame frame) {
		if (frame.exists() ^ missing != 0) return;
		missing |= Integer.MIN_VALUE;
		update(0, 1);
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		nbt.putIntArray("region", region);
		if (block != null)
			nbt.putLong("block", block.left.asLong());
		super.storeState(nbt, mode);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		int[] arr = nbt.getIntArray("region");
		System.arraycopy(arr, 0, region, 0, Math.min(9, arr.length));
		block = nbt.contains("block", NBT.TAG_LONG)
			? ImmutablePair.of(BlockPos.of(nbt.getLong("block")), null) : null;
		super.loadState(nbt, mode);
	}

	@Override
	public void onLoad() {
		if (level instanceof ServerWorld && block != null)
			block = ImmutablePair.of(block.left, (ServerWorld)level);
		super.onLoad();
	}

	@Override
	public Object[] stateInfo() {
		int x0, x1, z0, z1;
		switch(orientation()) {
		default:  x0 = 0; x1 = 2; z0 = 6; z1 = 8; break;
		case N12: x0 = 2; x1 = 0; z0 = 8; z1 = 6; break;
		case E12: x0 = 8; x1 = 6; z0 = 0; z1 = 2; break;
		case W12: x0 = 6; x1 = 8; z0 = 2; z1 = 0; break;
		}
		int x = region[x0 + x1 >> 1], z = region[z0 + z1 >> 1];
		x0 = region[x0] - x; x1 = region[x1] - x;
		if (x0 > x1) {x0 = -x0; x1 = -x1;}
		z0 = region[z0] - z; z1 = region[z1] - z;
		if (z0 > z1) {z0 = -z0; z1 = -z1;}
		int y = region[4];
		return new Object[] {
			"state.rs_ctr2.frame_controller",
			dx, x0, x1,
			dy, region[3] - y, region[5] - y,
			dz, z0, z1,
			IBlockSupplier.toString(this)
		};
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(
			region[0], region[3], region[6],
			region[2]+1, region[5]+1, region[8]+1
		);
	}

	@Override
	public ActionResultType
	onActivated(PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		return player.getItemInHand(hand).isEmpty() ?
			Utils.serverAction(player, ()-> {
				visible = !visible;
				clientDirty(false);
				player.displayClientMessage(new TranslationTextComponent(
					visible ? "msg.rs_ctr2.visible" : "msg.rs_ctr2.invisible"
				), true);
			}) : ActionResultType.PASS;
	}

	@Override
	public void onClicked(PlayerEntity player) {}

}
