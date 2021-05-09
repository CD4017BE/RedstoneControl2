package cd4017be.rs_ctr2.part;


import static cd4017be.rs_ctr2.api.grid.IGridHost.posOfport;
import static cd4017be.math.Linalg.*;

import java.util.List;

import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.api.grid.*;
import cd4017be.rs_ctr2.render.MicroBlockFace;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Cable extends GridPart implements IWire {

	public Cable() {
		super(2);
	}

	public Cable(int pos, Direction d1, Direction d2, int type) {
		this();
		this.bounds = 1L << pos;
		this.ports[0] = port(pos, d1, type);
		this.ports[1] = port(pos, d2, type);
	}

	@Override
	public Item item() {
		return ITEMS[ports[0] >> 12 & 3];
	}

	@Override
	public ItemStack asItemStack() {
		return new ItemStack(item(), Long.bitCount(bounds));
	}

	@Override
	public byte getLayer() {
		return L_INNER;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand != null) return ActionResultType.PASS;
		if (!player.level.isClientSide && player.getMainHandItem().getItem() instanceof IGridItem) {
			remove(pos);
			if (!player.isCreative())
				ItemFluidUtil.dropStack(new ItemStack(item()), player);
		}
		return ActionResultType.CONSUME;
	}

	public void merge(GridPart other) {
		if (!(other instanceof Cable)) return;
		short[] tp = this.ports, op = other.ports;
		if      (tp[0] == op[1]) tp[0] = op[0];
		else if (tp[1] == op[0]) tp[1] = op[1];
		else if (tp[0] == op[0]) tp[0] = op[1];
		else if (tp[1] == op[1]) tp[1] = op[0];
		else return;
		bounds |= other.bounds;
		other.host.removePart(other);
	}

	public void addTo(IGridHost host) {
		Port p;
		if (
			(p = host.findPort(this, ports[0])) != null ? !p.isMaster() :
			(p = host.findPort(this, ports[1])) != null && p.isMaster()
		) {
			short p0 = ports[0];
			ports[0] = ports[1];
			ports[1] = p0;
		}
		host.addPart(this);
	}

	public void remove(int pos) {
		//remove old part:
		IGridHost host = this.host;
		host.removePart(this);
		long b = bounds & ~(1L << pos);
		if (b == 0) {
			host.removeIfEmpty();
			return;
		}
		//split up:
		long f0 = path(b, ports[0]);
		long f1 = path(b, ports[1]);
		if (f0 != 0) {
			Cable part = new Cable();
			if (f1 == f0) {//still in one piece
				f1 = 0;
				part.ports[1] = ports[1];
			} else part.ports[1] = portNear(f0, pos, ports[1] >> 12);
			part.ports[0] = ports[0];
			part.bounds = f0;
			b &= ~f0;
			host.addPart(part);
		}
		if (f1 != 0) {
			Cable part = new Cable();
			part.ports[0] = portNear(f1, pos, ports[0] >> 12);
			part.ports[1] = ports[1];
			part.bounds = f1;
			b &= ~f1;
			host.addPart(part);
		}
		//drop remainder:
		if (b != 0)
			ItemFluidUtil.dropStack(new ItemStack(item(), Long.bitCount(b)), host.world(), host.pos());
		host.removeIfEmpty();
	}

	private static short portNear(long b, int pos, int type) {
		Direction d;
		if      (( 3 &  pos) != 0 && (b >>> pos -  1 & 1) != 0) d = Direction.WEST;
		else if (( 3 & ~pos) != 0 && (b >>> pos +  1 & 1) != 0) d = Direction.EAST;
		else if ((12 &  pos) != 0 && (b >>> pos -  4 & 1) != 0) d = Direction.DOWN;
		else if ((12 & ~pos) != 0 && (b >>> pos +  4 & 1) != 0) d = Direction.UP;
		else if ((48 &  pos) != 0 && (b >>> pos - 16 & 1) != 0) d = Direction.NORTH;
		else if ((48 & ~pos) != 0 && (b >>> pos + 16 & 1) != 0) d = Direction.SOUTH;
		else {pos = Long.numberOfTrailingZeros(b); d = Direction.NORTH;}
		return port(pos, d, type);
	}

	private static long path(long b, short port) {
		int p = posOfport(port);
		if ((p < 0 || (b >> p & 1) == 0) && (p = posOfport(port - 0x111)) < 0)
			return 0L;
		return floodFill(b, 1L << p);
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		nbt.putLong("bounds", bounds);
		nbt.putInt("ports", ports[0] & 0xffff | ports[1] << 16);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		bounds = nbt.getLong("bounds");
		int port = nbt.getInt("ports");
		ports[0] = (short)port;
		ports[1] = (short)(port >>> 16);
	}

	@Override
	public Object getHandler(int port) {
		if (port != 0) Main.LOG.warn("provider access from wrong side");
		Port p = host.findPort(this, ports[port ^ 1]);
		return p == null ? null : p.getHandler();
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) Main.LOG.warn("master access from wrong side");
		Port p = host.findPort(this, ports[port ^ 1]);
		if (p != null) p.setHandler(handler);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel != 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillModel(JitBakedModel model, long opaque) {
		MicroBlockFace[] faces = MicroBlockFace.facesOf(MODELS[ports[0] >> 12 & 3]);
		List<BakedQuad> quads = model.inner();
		long b = bounds, visible = ~opaque;
		drawPort(quads, faces, ports[0], b, visible);
		drawPort(quads, faces, ports[1], b, visible);
		drawStrips(quads, faces, b & b >>> 1 & 0x7777_7777_7777_7777L, visible, 0);
		drawStrips(quads, faces, b & b >>> 4 & 0x0fff_0fff_0fff_0fffL, visible, 1);
		drawStrips(quads, faces, b & b >>> 16 & 0x0000_ffff_ffff_ffffL, visible, 2);
	}

	@OnlyIn(Dist.CLIENT)
	private static void drawStrips(
		List<BakedQuad> quads, MicroBlockFace[] faces,
		long line, long visible, int ax
	) {
		int s = 1 << ax + ax, d = ax == 0 ? 2 : ax - 1;
		while(line != 0) {
			int p = Long.numberOfTrailingZeros(line), n = 1;
			long m = 1L << p;
			for (long l = m << s; (line & l) != 0; l <<= s, n++) m |= l;
			line &= ~m;
			if (((m | m << 1) & visible) == 0) continue;
			float[] vec = sca(3, dadd(3, MicroBlockFace.vec(p), .25F), .25F);
			float[] size = {.125F, .125F, .125F};
			size[ax] += n * .25F;
			for (int i = 0; i < 6; i++)
				if (i >> 1 != d && faces[i] != null)
					quads.add(faces[i].makeRect(vec, size));
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void drawPort(
		List<BakedQuad> quads, MicroBlockFace[] faces,
		int port, long b, long visible
	) {
		int p0 = IGridHost.posOfport(port);
		int p1 = IGridHost.posOfport(port - 0x111);
		boolean visible0 = p0 < 0 || (visible >>> p0 & 1) != 0;
		boolean visible1 = p1 < 0 || (visible >>> p1 & 1) != 0;
		if ((b >>> p0 & 1) == 0) p0 = -1;
		if ((b >>> p1 & 1) == 0) p1 = -1;
		if (p0 < 0 ^ p1 >= 0 || !(visible0 | visible1)) return;
		if (p0 >= 0) {
			boolean v = visible0;
			visible0 = visible1;
			visible1 = v;
		} else p0 = p1;
		int ax = Integer.numberOfTrailingZeros(0x111 & ~port) >> 2;
		if (ax >= 3) return;
		float[] vec = dadd(3, vec(p0 & 3, p0 >> 2 & 3, p0 >> 4 & 3), .25F);
		float[] size = {.5F, .5F, .5F};
		if (p1 < 0) vec[ax] -= .2505F;
		size[ax] = .751F;
		sca(3, vec, .25F);
		sca(3, size, .25F);
		int d = p1 >>> 31 | (ax == 0 ? 4 : ax - 1 << 1);
		for (int i = 0; i < 6; i++)
			if (i != d && faces[i] != null && (i == (d ^ 1) ? visible0 : visible1))
				quads.add(faces[i].makeRect(vec, size));
	}

	private static final Item[] ITEMS = {
		Content.data_cable,
		Content.power_cable,
		Content.item_cable,
		Content.fluid_cable,
	};

	public static final ResourceLocation[] MODELS = {
		Main.rl("part/data_cable"),
		Main.rl("part/power_cable"),
		Main.rl("part/item_cable"),
		Main.rl("part/fluid_cable")
	};

}
