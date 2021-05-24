package cd4017be.rs_ctr2.part;

import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Content.mem_write;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class MemWrite extends OrientedPart implements IGate, ISignalReceiver {

	int[] mem = EMPTY_INT_ARRAY;
	int idx, val;
	@Sync public int addr, bits, state;
	@Sync public boolean active;

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, 15);
		setPort(1, pos, Direction.WEST, TYPE_ID);
		setPort(2, pos, Direction.EAST, TYPE_ID);
		setPort(3, pos, Direction.SOUTH, TYPE_ID);
	}

	public MemWrite() {
		super(4);
	}

	@Override
	public Item item() {
		return mem_write;
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		return port == 1 ? (v, r) -> update(state, state = v)
			: port == 3 ? (v, r) -> update(bits, bits = v)
			: port == 2 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0)
			mem = handler instanceof int[] ? (int[])handler : EMPTY_INT_ARRAY;
	}

	@Override
	public boolean isMaster(int port) {
		return port == 0;
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null || bits == 0) return false;
		int p = addr * bits;
		if ((idx = p >>> 5) >= mem.length) return false;
		int m = -1 >>> 32 - bits << p;
		val = state << p & m | mem[idx] & ~m;
		return true;
	}

	@Override
	public void latchOut() {
		if (idx < mem.length) mem[idx] = val;
	}

	@Override
	public void updateInput(int value, int rec) {
		update(addr, addr = value);
	}

	private void update(int old, int val) {
		if (old == val || active) return;
		active = true;
		GATE_UPDATER.add(this);
	}

	@Override
	public String toString() {
		return TooltipUtil.format("state.rs_ctr2.mem_write", state, addr, bits);
	}

}
