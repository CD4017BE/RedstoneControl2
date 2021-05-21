package cd4017be.rs_ctr2.api.grid;

import static java.lang.Math.max;

import java.util.Arrays;

import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.api.gate.IPortHolder.Port;
import cd4017be.rs_ctr2.api.gate.Link;
import it.unimi.dsi.fastutil.longs.LongArrays;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

/**Manages grid port connections for a {@link IGridPortHolder}.
 * @author CD4017BE */
public class ExtGridPorts implements INBTSerializable<LongArrayNBT> {

	private final IGridPortHolder host;
	/**bit62 = link valid, bit63 = master <br>
	 * 0x00ii_PPPP_LLLL_LLLL -> unconnected wire end <br>
	 * 0x10ii_PPPP_LLLL_LLLL -> inner endpoint <br>
	 * 0x2DII_PPPP_00ii_pppp -> pass through wire <br>
	 * 0x3DII_PPPP_LLLL_LLLL -> outer endpoint <br>
	 * P = this port, p = linked neighbor port,
	 * I = neighbor index, i = linked index,
	 * D = neighbor direction, L = Link ID */
	private long[] ports = LongArrays.EMPTY_ARRAY;

	public ExtGridPorts(IGridPortHolder host) {
		this.host = host;
	}

	public short getPort(int port) {
		return (short)(ports[port] >> 32);
	}

	public boolean isMaster(int channel) {
		return ports[channel] < 0;
	}

	public void onLoad() {
		for (int i = 0; i < ports.length; i++) {
			long x = ports[i];
			if (x << 1 < 0 && x << 3 < 0)
				Link.load(host, i, (int)x);
		}
	}

	public void onUnload() {
		for (int i = 0; i < ports.length; i++) {
			long x = ports[i];
			if (x << 1 < 0 && x << 3 < 0)
				Link.unload(host, i, (int)x);
		}
	}

	public void onRemove() {
		for (int i = 0; i < ports.length; i++)
			disconnect(i);
	}

	public void clear() {
		for (int i = 0; i < ports.length; i++) {
			disconnect(i);
			ports[i] = 0;
		}
	}

	@Override
	public LongArrayNBT serializeNBT() {
		return new LongArrayNBT(ports);
	}

	@Override
	public void deserializeNBT(LongArrayNBT nbt) {
		ports = LongArrays.EMPTY_ARRAY;
		long[] arr = nbt.getAsLongArray();
		for (int i = arr.length; i > 0; i--)
			if (arr[i - 1] != 0) {
				ports = Arrays.copyOf(arr, i);
				break;
			}
	}

	/**@param port to find
	 * @param i initial guess for the index
	 * @return index of port in extPorts or -1 if not found */
	public int findPort(short port, int i) {
		if (i < ports.length && (short)(ports[i] >> 32) == port)
			return i;
		for (i = 0; i < ports.length; i++)
			if ((short)(ports[i] >> 32) == port)
				return i;
		return -1;
	}

	public boolean remove(short port) {
		int i = findPort(port, 0xff);
		if (i < 0) return false;
		disconnect(i);
		ports[i] = 0;
		//TODO remove connected as well
		return true;
	}

	public int create(short port, int flags) {
		int i = findPort(port, 0xff);
		if (i < 0) {
			for (i = 0; i < ports.length && ports[i] != 0; i++);
			if (i == ports.length)
				ports = Arrays.copyOf(ports, max(i + 3, i << 1));
		}
		int d = Integer.numberOfTrailingZeros(~port & 0x111);
		d = (d + 8 >> 1 | port >> d + 3 & 1) % 6 | flags << 4;
		ports[i] = ports[i] & 0x40ff_0000_ffff_ffffL
			| (long)((port & 0xffff) | d << 24) << 32;
		return i;
	}

	public boolean createPort(short con, boolean master, boolean doLink) {
		int i;
		if (((con | con - 0x111) & 0x888) != 0)
			i = create(con, master ? 11 : 3);
		else if ((i = findPort(con, 0xff)) >= 0)
			ports[i] = ports[i] & 0x40ff_ffff_ffff_ffffL
				| (master ? 9L : 1L) << 60;
		else return false;
		if (doLink) connect(i);
		return true;
	}

	public boolean createWire(GridPart part, boolean doLink) {
		if (!(part instanceof IWire)) return false;
		short port0 = part.ports[0], port1 = part.ports[1];
		int f0 = ((port0 | port0 - 0x111) & 0x888) != 0 ? 2 : 0;
		int f1 = ((port1 | port1 - 0x111) & 0x888) != 0 ? 2 : 0;
		if ((f0 | f1) == 0) return false;
		int master1 = 0;
		Port p;
		if (f0 == 0 && (p = part.host.findPort(part, port0)) != null) {
			f0 = 1;
			if (!p.isMaster()) master1 = 8;
		}
		if (f1 == 0 && (p = part.host.findPort(part, port1)) != null) {
			f1 = 1;
			if (p.isMaster()) master1 = 8;
		}
		int i0 = create(port0, f0 | master1 ^ 8);
		int i1 = create(port1, f1 | master1);
		long l0 = ports[i0], l1 = ports[i1];
		ports[i0] = f0 == 2
			? l0 & 0xffff_ffff_0000_0000L | mirroredPort(l1) | i1 << 16
			: l0 & 0xff00_ffff_ffff_ffffL | (long)i1 << 48;
		ports[i1] = f1 == 2
			? l1 & 0xffff_ffff_0000_0000L | mirroredPort(l0) | i0 << 16
			: l1 & 0xff00_ffff_ffff_ffffL | (long)i0 << 48;
		if (doLink) connect(i0);
		return true;
	}

	private static int mirroredPort(long ep) {
		int ax = (int)(ep >> 57) + 1 & 3;
		return (int)(ep >> 32) & 0xffff ^ 8 << (ax == 3 ? 0 : ax << 2);
	}

	public void connect(int extPort) {
		if ((ports[extPort] & 3L << 60) == 0) return;
		Port start = findStartPort(this, extPort, true);
		if (start == null) return;
		Port end = findEndPort(this, extPort, true);
		if (end == null || !(start.isMaster() ^ end.isMaster())) return;
		long[] startPorts = ((IGridPortHolder)start.host).extPorts().ports;
		long[] endPorts = ((IGridPortHolder)end.host).extPorts().ports;
		long se = startPorts[start.channel], ee = endPorts[end.channel];
		int id = (int)se;
		if (id == 0) id = (int)ee;
		if (id == 0) id = Link.newId();
		startPorts[start.channel] = se & 0xffff_ffff_0000_0000L | id & 0xffff_ffffL | 1L << 62;
		  endPorts[  end.channel] = ee & 0xffff_ffff_0000_0000L | id & 0xffff_ffffL | 1L << 62;
		Link.load(start.host, start.channel, id);
		Link.load(  end.host,   end.channel, id);
	}

	public void disconnect(int extPort) {
		if (ports[extPort] << 1 >= 0) return;
		long[] ports;
		Port port;
		if ((port = findStartPort(this, extPort, false)) != null) {
			ports = ((IGridPortHolder)port.host).extPorts().ports;
			Link.unload(port.host, port.channel, (int)ports[port.channel]);
			ports[port.channel] &= 0xbfff_ffff_ffff_ffffL;
		}
		if ((port = findEndPort(this, extPort, false)) != null) {
			ports = ((IGridPortHolder)port.host).extPorts().ports;
			Link.unload(port.host, port.channel, (int)ports[port.channel]);
			ports[port.channel] &= 0xbfff_ffff_ffff_ffffL;
		}
	}

	private static Port findStartPort(ExtGridPorts grid, int i, boolean linked) {
		long ep = grid.ports[i];
		if (ep << 2 >= 0) i = (int)(ep >> 48) & 0xff;
		else if (ep << 3 >= 0) i = (int)ep >>> 16;
		else return new Port(grid.host, i);
		return findEndPort(grid, i, linked);
	}

	private ExtGridPorts adjacent(int dir) {
		BlockPos pos = host.pos().relative(Direction.from3DDataValue(dir));
		TileEntity te = host.world().getBlockEntity(pos);
		return te instanceof IGridPortHolder ?
			((IGridPortHolder)te).extPorts() : null;
	}

	private static Port findEndPort(ExtGridPorts grid, int i, boolean linked) {
		long ep = grid.ports[i], link = linked ? 1L << 62 : 0;
		short port = (short)mirroredPort(ep);
		while(ep << 2 < 0) {
			ExtGridPorts grid1 = grid.adjacent((int)(ep >> 56) & 7);
			if (grid1 == null) return null;
			int p = grid1.findPort(port, (int)(ep >> 48) & 0xff);
			if (p < 0) return null;
			grid.ports[i] = ep & 0xbf00_ffff_ffff_ffffL | (long)p << 48 | link & ~ep << 2;
			ep = (grid = grid1).ports[p];
			if (ep << 3 < 0) return new Port(grid.host, p);
			grid.ports[p] = ep & 0xbf00_ffff_ffff_ffffL | (long)i << 48 | link;
			port = (short)ep;
			i = (int)ep >> 16 & 0xff;
			if (i >= grid.ports.length) {
				Main.LOG.error("link index out of bounds: {x}", ep);
				return null;
			}
			ep = grid.ports[i];
		}
		return ep << 3 < 0 ? new Port(grid.host, i) : null;
	}

}
