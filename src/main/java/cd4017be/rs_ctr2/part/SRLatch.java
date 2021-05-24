package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.sr_latch;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class SRLatch extends SignalGate {

	protected ISignalReceiver outputR = ISignalReceiver.NOP;
	@Sync public int inS, inR;

	public SRLatch() {
		super(4);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.EAST, ISignalReceiver.TYPE_ID);
		setPort(3, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) super.setHandler(port, handler);
		else if (handler instanceof ISignalReceiver)
			(outputR = (ISignalReceiver)handler).updateInput(~state);
		else outputR = ISignalReceiver.NOP;
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		return port == 2 ? (v, r) -> {
			if (inR != (inR = v)) update();
		} : port == 3 ? (v, r) -> {
			if (inS != (inS = v)) update();
		} : null;
	}

	@Override
	public boolean evaluate() {
		active = false;
		return host != null && state != (state = (state | inS) & ~inR);
	}

	@Override
	public void latchOut() {
		output.updateInput(state);
		outputR.updateInput(~state);
	}

	@Override
	public Item item() {
		return sr_latch;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{"state.rs_ctr2.sr_latch", state, ~state, inR, inS};
	}

}
