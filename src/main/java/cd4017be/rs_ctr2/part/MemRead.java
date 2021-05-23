package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.mem_read;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY;

import cd4017be.lib.network.Sync;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class MemRead extends SignalGate implements ISignalReceiver {

	int[] mem = EMPTY_INT_ARRAY;
	@Sync public int addr, bits;

	public MemRead() {
		super(4);
		bits = 32;
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.WEST, TYPE_ID);
		setPort(1, pos, Direction.NORTH, 15);
		setPort(2, pos, Direction.EAST, TYPE_ID);
		setPort(3, pos, Direction.SOUTH, TYPE_ID);
	}

	@Override
	public Item item() {
		return mem_read;
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null || bits == 0) return false;
		int p = addr * bits, i = p >>> 5;
		return state != (
			state = i >= mem.length ? 0
			: mem[i] >>> p & -1 >>> 32 - bits
		);
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		return port == 3 ? this::updateBits
			: port == 2 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 1)
			mem = handler instanceof int[] ? (int[])handler : EMPTY_INT_ARRAY;
		else super.setHandler(port, handler);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel < 2;
	}

	public void updateBits(int value, int rec) {
		if (bits != (bits = value)) update();
	}

	@Override
	public void updateInput(int value, int rec) {
		if (addr != (addr = value)) update();
	}

	@Override
	public String toString() {
		return TooltipUtil.format("state.rs_ctr2.mem_read", state, addr, bits);
	}

}
