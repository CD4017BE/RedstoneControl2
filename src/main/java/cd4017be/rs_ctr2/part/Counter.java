package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.counter;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class Counter extends SignalGate implements ISignalReceiver {

	ISignalReceiver clkOut = ISignalReceiver.NOP;
	@Sync public int max, clk, clkO;

	public Counter() {
		super(4);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, TYPE_ID);
		setPort(1, pos, Direction.WEST, TYPE_ID);
		setPort(2, pos, Direction.SOUTH, TYPE_ID);
		setPort(3, pos, Direction.EAST, TYPE_ID);
	}

	@Override
	public Item item() {
		return counter;
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		return port == 2 ? this::updateLimit
			: port == 3 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 1)
			(clkOut = ISignalReceiver.of(handler)).updateInput(clkO);
		else super.setHandler(port, handler);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel < 2;
	}

	public void updateLimit(int value, int rec) {
		if (max > 0 & (max = value) <= 0) update();
	}

	@Override
	public void updateInput(int value, int rec) {
		if ((value & ~clk) != 0 || value == 0 && clk != 0) update();
		clk = value;
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null) return false;
		if (clk != 0) state++;
		if (state >= max) {
			if (max > 0) clkO = clk;
			state = 0;
		} else clkO = 0;
		return true;
	}

	@Override
	public void latchOut() {
		super.latchOut();
		clkOut.updateInput(clkO);
	}

	@Override
	public String toString() {
		return TooltipUtil.format("state.rs_ctr2.counter", state, clkO, max, clk);
	}

}
