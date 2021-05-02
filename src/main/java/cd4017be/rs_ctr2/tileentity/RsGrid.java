package cd4017be.rs_ctr2.tileentity;

import static cd4017be.lib.network.Sync.SAVE;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.function.Predicate;

import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.api.grid.ExtGridPorts;
import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.util.VoxelShape4x4x4;
import cd4017be.lib.block.BlockTE;
import cd4017be.lib.block.BlockTE.*;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.render.model.TileEntityModel;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.Constants.NBT;


public class RsGrid extends BaseTileEntity
implements IGridHost, ITEInteract, ITEShape, ITERedstone, ITEBlockUpdate, ITEPickItem {

	private final VoxelShape4x4x4 bounds = new VoxelShape4x4x4();
	private final ExtGridPorts extPorts = new ExtGridPorts(this);
	private final ArrayList<GridPart> parts = new ArrayList<>();
	private long opaque;

	public RsGrid(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public VoxelShape getShape(ISelectionContext context) {
		return new VoxelShapeCube(bounds);
	}

	@Override
	public ActionResultType onActivated(
		PlayerEntity player, Hand hand, BlockRayTraceResult hit
	) {
		return onInteract(player, hand, hit);
	}

	@Override
	public void onClicked(PlayerEntity player) {
		Vector3d from = player.getEyePosition(0);
		Vector3d to = from.add(player.getViewVector(0).scale(5));
		BlockRayTraceResult hit = getShape(null).clip(from, to, worldPosition);
		if (hit != null) onInteract(player, null, hit);
	}

	@Override
	public void updateBounds() {
		long b = 0, o = 0;
		for (GridPart part : parts) {
			b |= part.bounds;
			if (part.isOpaque())
				o |= part.bounds;
		}
		bounds.grid = b;
		opaque = o;
	}

	@Override
	public GridPart findPart(Predicate<GridPart> filter) {
		for (GridPart part : parts)
			if (filter.test(part))
				return part;
		return null;
	}

	@Override
	public void removePart(GridPart part) {
		if (!parts.remove(part)) return;
		updateBounds();
		for (int i = 0; i < part.ports.length; i++) {
			short con = part.ports[i];
			if (extPorts.remove(con)) continue;
			if (part.isMaster(i)) part.setHandler(i, null);
			else {
				Port port = findPort(part, con);
				if (port != null) port.setHandler(null);
			}
		}
		updateRedstone(part);
		part.setHost(null);
		clientDirty(true);
	}

	@Override
	public void removeIfEmpty() {
		if (parts.isEmpty()) level.removeBlock(this.worldPosition, false);
	}

	@Override
	public boolean addPart(GridPart part) {
		if (part.host == this) return true;
		if ((bounds.grid & part.bounds) != 0) return false;
		bounds.grid |= part.bounds;
		if (part.isOpaque()) opaque |= part.bounds;
		parts.add(part);
		part.setHost(this);
		connectPart(part);
		updateRedstone(part);
		clientDirty(true);
		return true;
	}

	private void connectPart(GridPart part) {
		if (extPorts.createWire(part, !unloaded)) return;
		for (int i = 0; i < part.ports.length; i++) {
			short con = part.ports[i];
			boolean master = part.isMaster(i);
			if (extPorts.createPort(con, master, !unloaded)) continue;
			Port port = findPort(part, con);
			if (port == null) continue;
			if (master) part.setHandler(i, port.getHandler());
			else port.setHandler(part.getHandler(i));
		}
	}

	private void updateRedstone(GridPart part) {
		for (int i = 0; i < 6; i++)
			if ((part.bounds & GridPart.FACES[i]) != 0) {
				Direction d = Direction.from3DDataValue(i);
				if (part.analogOutput(d.getOpposite()) > 0)
					updateNeighbor(d);
			}
	}

	@Override
	public void onPartChange() {
		clientDirty(true);
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		ListNBT list = new ListNBT();
		for (GridPart part : parts) {
			CompoundNBT tag = new CompoundNBT();
			part.storeState(tag, mode);
			list.add(tag);
		}
		nbt.put("parts", list);
		if ((mode & SAVE) != 0) {
			nbt.putInt("hash", list.hashCode());
			nbt.put("extIO", extPorts.serializeNBT());
		}
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		ListNBT list = nbt.getList("parts", NBT.TAG_COMPOUND);
		boolean empty = (mode & SAVE) != 0 || list.size() != parts.size();
		if (empty) parts.clear();
		for (int i = 0; i < list.size(); i++) {
			GridPart part = empty ? null : parts.get(i);
			part = GridPart.load(part, list.getCompound(i), mode);
			if (part == null) continue;
			if (empty) parts.add(part);
			else parts.set(i, part);
		}
		updateBounds();
		if ((mode & SAVE) != 0) {
			if (nbt.contains("extIO", NBT.TAG_LONG_ARRAY))
				extPorts.deserializeNBT((LongArrayNBT)nbt.get("extIO"));
			//state loaded from item after placement:
			if (!unloaded) onLoad();
		}
	}

	@Override
	public void onLoad() {
		if (!level.isClientSide) {
			for (GridPart part : parts)
				part.setHost(this);
			for (GridPart part : parts)
				connectPart(part);
			extPorts.onLoad();
			clientDirty(true);
		}
		super.onLoad();
		requestModelDataUpdate();
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		if (level.isClientSide) return;
		for (GridPart part : parts) part.setHost(null);
		extPorts.onUnload();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		extPorts.onRemove();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IModelData getModelData() {
		ModelDataMap data = TileEntityModel.MODEL_DATA_BUILDER.build();
		JitBakedModel model = JitBakedModel.make(data);
		long o = opaque;
		boolean open = (WALLS & ~o) != 0;
		if (!open) o |= ~BOUNDARY;
		for (GridPart part : parts)
			if (open || (part.bounds & BOUNDARY) != 0)
				part.fillModel(model, o);
		return data;
	}

	private static final long BOUNDARY = 0xffff_f99f_f99f_ffffL;
	public static final long WALLS = 0x0660_6996_6996_0660L;

	@Override
	public int redstoneSignal(Direction side, boolean strong) {
		if (strong || side == null) return 0;
		int i = 0;
		long b = GridPart.FACES[side.ordinal()^1];
		for (GridPart part : parts)
			if ((part.bounds & b) != 0)
				i = max(i, part.analogOutput(side));
		return i;
	}

	@Override
	public boolean redstoneConnection(Direction side) {
		if (side == null) return false;
		long b = GridPart.FACES[side.ordinal()^1];
		for (GridPart part : parts)
			if ((part.bounds & b) != 0 && part.connectRedstone(side))
				return true;
		return false;
	}

	@Override
	public void onNeighborBlockChange(BlockPos from, Block block, boolean moving) {
		Direction dir = Utils.getSide(from, worldPosition);
		if (dir == null) return;
		long b = GridPart.FACES[dir.ordinal()];
		for (GridPart part : parts)
			if ((part.bounds & b) != 0)
				part.onBlockChange(level, from, dir);
	}

	@Override
	public void updateNeighbor(Direction d) {
		if (unloaded) return;
		BlockPos pos1 = worldPosition.relative(d);
		getBlockState(pos1, true).neighborChanged(
			level, pos1, getBlockState().getBlock(), worldPosition, false
		);
	}

	@Override
	public Port findPort(GridPart except, short port) {
		long m = 0;
		int port1 = port - 0x111;
		if ((port & 0x888) == 0) m |= 1L << (port >> 1 & 3 | port >> 3 & 12 | port >> 5 & 48);
		if ((port1 & 0x888) == 0) m |= 1L << (port1 >> 1 & 3 | port1 >> 3 & 12 | port1 >> 5 & 48);
		for (GridPart part : parts) {
			if ((part.bounds & m) == 0 || part == except) continue;
			int l = part.ports.length;
			for (int i = 0; i < l; i++)
				if (part.ports[i] == port)
					return new Port(part, i);
			if (l > 0) break;
		}
		return null;
	}

	@Override
	public World world() {
		return level;
	}

	@Override
	public BlockPos pos() {
		return worldPosition;
	}

	@Override
	public ExtGridPorts extPorts() {
		return extPorts;
	}

	@Override
	public ItemStack getItem() {
		ItemStack stack = new ItemStack(Content.grid);
		CompoundNBT nbt = stack.getOrCreateTagElement(BlockTE.TE_TAG);
		storeState(nbt, SAVE);
		nbt.remove("extIO");
		return stack;
	}

}
