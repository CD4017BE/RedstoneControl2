package cd4017be.rs_ctr2.part;

import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Content.division_gate;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class DivisionGate extends OrientedPart implements IGate {

	ISignalReceiver outD = ISignalReceiver.NOP;
	ISignalReceiver outR = ISignalReceiver.NOP;
	@Sync public int a, b, div, rem;
	@Sync public boolean active;

	public DivisionGate() {
		super(4);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
		setPort(3, pos, Direction.EAST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return division_gate;
	}

	private void update(boolean doUpdate) {
		if (doUpdate && !active) {
			active = true;
			GATE_UPDATER.add(this);
		}
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		return port == 2 ? (v, r) -> update(a != (a = v))
		     : port == 3 ? (v, r) -> update(b != (b = v))
		     : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0)
			(outD = ISignalReceiver.of(handler)).updateInput(div);
		else if (port == 1)
			(outR = ISignalReceiver.of(handler)).updateInput(rem);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel < 2;
	}

	@Override
	public boolean evaluate() {
		active = false;
		return host != null && (
			b != 0 ? div != (div = a / b) | rem != (rem = a % b)
			: div != (div = Integer.MAX_VALUE + (a >> 31)) | rem != (rem = a)
		);
	}

	@Override
	public void latchOut() {
		outD.updateInput(div);
		outR.updateInput(rem);
	}

	@Override
	public String toString() {
		return TooltipUtil.format("state.rs_ctr2.division", div, rem, a, b);
	}

}
