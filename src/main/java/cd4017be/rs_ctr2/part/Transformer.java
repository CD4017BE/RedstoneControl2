package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.transformer;

import cd4017be.api.grid.port.IEnergyAccess;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class Transformer extends SignalGate implements ISignalReceiver {

	IEnergyAccess a = IEnergyAccess.NOP;
	IEnergyAccess b = IEnergyAccess.NOP;
	@Sync public int in;

	public Transformer() {
		super(4);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.EAST, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.SOUTH, IEnergyAccess.TYPE_ID);
		setPort(3, pos, Direction.NORTH, IEnergyAccess.TYPE_ID);
	}

	@Override
	public Item item() {
		return transformer;
	}

	@Override
	public void setHandler(int port, Object handler) {
		switch(port) {
		case 0: super.setHandler(port, handler); break;
		case 2: a = IEnergyAccess.of(handler); break;
		case 3: b = IEnergyAccess.of(handler); break;
		}
	}

	@Override
	public Object getHandler(int port) {
		return port == 1 ? this : null;
	}

	@Override
	public boolean isMaster(int channel) {
		return channel != 1;
	}

	@Override
	public boolean evaluate() {
		active = false;
		return host != null && (state | (state = in)) != 0;
	}

	@Override
	public void latchOut() {
		if (state != 0) {
			state = b.transferEnergy(-a.transferEnergy(-state, true), false);
			a.transferEnergy(-state, false);
			update();
		}
		super.latchOut();
	}

	@Override
	public void updateInput(int value, int rec) {
		if (in != (in = value)) update();
	}

	@Override
	public Object[] stateInfo() {
		int stoA = -a.transferEnergy(Integer.MIN_VALUE, true);
		int capA = a.transferEnergy(Integer.MAX_VALUE, true) + stoA;
		int stoB = -b.transferEnergy(Integer.MIN_VALUE, true);
		int capB = b.transferEnergy(Integer.MAX_VALUE, true) + stoB;
		return new Object[]{"state.rs_ctr2.transformer", state, in, stoA, capA, stoB, capB};
	}

}
