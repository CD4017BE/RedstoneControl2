package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.power_splitter;

import cd4017be.api.grid.port.IEnergyAccess;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class SplitterP extends OrientedPart implements IEnergyAccess {

	IEnergyAccess src = IEnergyAccess.NOP;

	public SplitterP() {
		super(6);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, TYPE_ID);
		setPort(1, pos, Direction.SOUTH, TYPE_ID);
		setPort(2, pos, Direction.DOWN, TYPE_ID);
		setPort(3, pos, Direction.UP, TYPE_ID);
		setPort(4, pos, Direction.WEST, TYPE_ID);
		setPort(5, pos, Direction.EAST, TYPE_ID);
	}

	@Override
	public Object getHandler(int port) {
		return port == 0 ? null : this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) src = IEnergyAccess.of(handler);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel == 0;
	}

	@Override
	public int transferEnergy(int amount, boolean test, int rec) {
		return --rec < 0 ? 0 : src.transferEnergy(amount, test, rec);
	}

	@Override
	public Item item() {
		return power_splitter;
	}

}
