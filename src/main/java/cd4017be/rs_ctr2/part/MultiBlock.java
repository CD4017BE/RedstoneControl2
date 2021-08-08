package cd4017be.rs_ctr2.part;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.api.grid.GridPart;
import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.IGridItem;
import cd4017be.lib.render.GridModels;
import cd4017be.lib.render.MicroBlockFace;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @param <T> type of extending class
 * @author CD4017BE */
public abstract class MultiBlock<T extends MultiBlock<T>> extends GridPart
implements IProbeInfo {

	public MultiBlock(int pos) {
		super(0);
		if (pos < 0) return;
		bounds = 1L << pos;
		onBoundsChange();
	}

	@Override
	public ItemStack asItemStack() {
		return new ItemStack(item(), Long.bitCount(bounds));
	}

	@Override
	public ActionResultType onInteract(
		PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos
	) {
		boolean client = player.level.isClientSide;
		if (hand == null) {
			if (client || !(player.getMainHandItem().getItem() instanceof IGridItem))
				return ActionResultType.CONSUME;
			removeVoxel(pos);
			if (!player.isCreative())
				ItemFluidUtil.dropStack(super.asItemStack(), player);
			return ActionResultType.CONSUME;
		}
		//right click interact
		Item item = player.getItemInHand(hand).getItem();
		short port = port(pos, hit.getDirection(), 0);
		int p = findPort(port);
		if (item instanceof IGridItem)
			return p >= 0 ? ActionResultType.PASS
				: createPort((IGridItem)item, port, client);
		if (!player.isShiftKeyDown())
			return onInteract(player, client);
		if (item != Items.AIR) return ActionResultType.PASS;
		//remove port
		if (client) return ActionResultType.CONSUME;
		if (p < 0) return ActionResultType.FAIL;
		IGridHost host = this.host;
		host.removePart(this);
		ports = ArrayUtils.remove(ports, p);
		host.addPart(this);
		return ActionResultType.SUCCESS;
	}

	protected ActionResultType onInteract(PlayerEntity player, boolean client) {
		return ActionResultType.PASS;
	}

	private int findPort(int port) {
		port &= 0xfff;
		for (int i = 0; i < ports.length; i++)
			if ((ports[i] & 0xfff) == port) return i;
		return -1;
	}

	private static int portValid(short port, long b) {
		int p = IGridHost.posOfport(port);
		int q = IGridHost.posOfport(port - 0x111);
		return ~p >>> 31 & (int)(b >>> p)
			^ ~q >>> 31 & (int)(b >>> q);
	}

	@Override
	public void setBounds(long b) {
		//check ports
		long valid = 0;
		for (int i = 0; i < ports.length; i++)
			valid |= (long)portValid(ports[i], b) << i;
		int n = Long.bitCount(valid);
		IGridHost host = this.host;
		if (n >= ports.length) {
			//all ports valid, just change bounds
			bounds = b;
			onBoundsChange();
			if (host == null) return;
			host.updateBounds();
			host.onPartChange();
			return;
		}
		//remove invalid ports
		if (host != null) host.removePart(this);
		short[] arr = new short[n];
		for (int i = 0, j = 0; valid != 0; i++, valid >>>= 1)
			if ((valid & 1) != 0) arr[j++] = ports[i];
		ports = arr;
		bounds = b;
		onBoundsChange();
		if (host != null) host.addPart(this);
	}

	public void removeVoxel(int pos) {
		long b = bounds & ~(1L << pos);
		IGridHost host = this.host;
		if (b == 0) {// completely removed
			dissassemble(host.world(), host.pos());
			host.removePart(this);
			host.removeIfEmpty();
			return;
		}
		long b1 = floodFill(b, Long.lowestOneBit(b));
		if (b1 != b) {// not in one piece
			host.removePart(this);
			do {//split off piece
				T part = splitOff(b1, b);
				part.ports = ports;
				part.setBounds(b1);
				host.addPart(part);
				b &= ~b1;
				b1 = floodFill(b, Long.lowestOneBit(b));
			} while(b1 != b);
		}
		setBounds(b);
		host.addPart(this);
	}

	@SuppressWarnings("unchecked")
	public T findAdjacent(IGridHost host, long b) {
		long o = outline(b) & ~b;
		Class<?> c = getClass();
		return (T)host.findPart(p -> (p.bounds & o) != 0 && p.getClass() == c);
	}

	public boolean addVoxel(int pos) {
		if (host.getPart(pos, L_FULL) != null) return false;
		long b = bounds | 1L << pos;
		IGridHost host = this.host;
		for (T part; (part = findAdjacent(host, b)) != null;) {
			//merge with touching other structure
			host.removePart(part);
			b |= part.bounds;
			short[] p = merge(part);
			if (p != ports) {
				host.removePart(this);
				ports = p;
			}
		}
		setBounds(b);
		host.addPart(this);
		return true;
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		bounds = nbt.getLong("b");
		onBoundsChange();
		int[] arr = nbt.getIntArray("p");
		ports = new short[arr.length];
		for (int i = 0; i < arr.length; i++)
			ports[i] = (short)arr[i];
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		nbt.putLong("b", bounds);
		int[] arr = new int[ports.length];
		for (int i = 0; i < arr.length; i++)
			arr[i] = ports[i];
		nbt.putIntArray("p", arr);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillModel(JitBakedModel model, long opaque) {
		MicroBlockFace.drawVoxels(model, model(), bounds, opaque);
		for (int i = 0; i < ports.length; i++) {
			short port = ports[i];
			GridModels.drawPort(model, port, isMaster(i), bounds, opaque);
		}
	}

	protected abstract void onBoundsChange();
	protected abstract ActionResultType createPort(IGridItem item, short port, boolean client);
	protected abstract T splitOff(long splitBounds, long thisBounds);
	protected abstract short[] merge(T other);
	protected abstract ResourceLocation model();

	@Override
	public boolean canRotate() {
		return true;
	}

	@Override
	public void rotate(int steps) {
		rotate(ports, steps);
		super.rotate(steps);
	}

	@Override
	public boolean canMove(Direction d, int n) {
		long m = mask(d.ordinal(), n);
		return (bounds & m) == 0 || (bounds & ~m) == 0;
	}

	@Override
	public GridPart move(Direction d, int n) {
		GridPart part = super.move(d, n);
		move(ports, d, n, part != null);
		return part;
	}

}
