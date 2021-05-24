package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.not_gate;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class NotGate extends OrientedPart implements ISignalReceiver {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync public int state;

	public NotGate() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Object getHandler(int port) {
		return port == 0 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) return;
		if (handler instanceof ISignalReceiver)
			(out = (ISignalReceiver)handler).updateInput(state);
		else out = ISignalReceiver.NOP;
	}

	@Override
	public boolean isMaster(int channel) {
		return channel != 0;
	}

	@Override
	public Item item() {
		return not_gate;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || state == (state = ~value)) return;
		out.updateInput(state, rec);
	}

	@Override
	public String toString() {
		return TooltipUtil.format("state.rs_ctr2.direct", state);
	}

}
