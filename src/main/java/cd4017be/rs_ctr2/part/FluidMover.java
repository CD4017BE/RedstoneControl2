package cd4017be.rs_ctr2.part;

import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Content.fluid_mover;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static java.lang.Math.abs;

import com.google.common.base.Predicates;

import cd4017be.api.grid.GridPart;
import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.port.IEnergyAccess;
import cd4017be.api.grid.port.IFluidAccess;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;

/**@author CD4017BE */
public class FluidMover extends OrientedPart
implements ISignalReceiver, IGate, IProbeInfo {

	IFluidAccess a = IFluidAccess.NOP;
	IFluidAccess b = IFluidAccess.NOP;
	IEnergyAccess energy = IEnergyAccess.NOP;
	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync public int num = 1000, clk, res;
	@Sync public boolean active;

	public FluidMover() {
		super(6);
	}

	@Override
	public void set(int pos, Orientation orient) {
		if ((pos & 12) == 12) pos -= 4;
		if ((pos & 48) == 48) pos -= 16;
		super.set(pos, orient);
		setBounds(pos, pos + 0x14);
		setPort(0, pos + 0x14, Direction.UP, ISignalReceiver.TYPE_ID);
		setPort(1, pos + 0x04, Direction.UP, ISignalReceiver.TYPE_ID);
		setPort(2, pos + 0x04, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(3, pos + 0x14, Direction.SOUTH, IEnergyAccess.TYPE_ID);
		setPort(4, pos + 0x00, Direction.NORTH, IFluidAccess.TYPE_ID);
		setPort(5, pos + 0x10, Direction.SOUTH, IFluidAccess.TYPE_ID);
	}

	@Override
	public GridPart setHost(IGridHost host) {
		if (host != null && active)
			GATE_UPDATER.add(this);
		return super.setHost(host);
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		return port == 1 ? (v, r) -> num = v
			: port == 0 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		switch(port) {
		case 2: (out = ISignalReceiver.of(handler)).updateInput(res); break;
		case 3: energy = IEnergyAccess.of(handler); break;
		case 4: a = IFluidAccess.of(handler); break;
		case 5: b = IFluidAccess.of(handler); break;
		}
	}

	@Override
	public boolean isMaster(int channel) {
		return channel >= 2;
	}

	@Override
	public Item item() {
		return fluid_mover;
	}

	@Override
	public void updateInput(int value, int rec) {
		if ((~clk & (clk = value)) == 0 || active) return;
		active = true;
		GATE_UPDATER.add(this);
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null) return false;
		return (res | (res = num)) != 0;
	}

	@Override
	public void latchOut() {
		transfer: {
			if (res == 0) break transfer;
			int e = cost(res);
			if (energy.transferEnergy(e, true) != e) {
				res = 0;
				break transfer;
			}
			if (res > 0) res = a.transfer(res, Predicates.alwaysTrue(), b);
			else res = -b.transfer(-res, Predicates.alwaysTrue(), a);
			energy.transferEnergy(cost(res), false);
		}
		out.updateInput(res);
	}

	private static int cost(int am) {
		long e = (-100L - abs((long)am)) * SERVER_CFG.move_fluid.get() >> 10;
		return e < 0xc0000000 ? 0xc0000000 : (int)e;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{
			"state.rs_ctr2.fluid_mover", clk, num, res,
			-energy.transferEnergy(-Integer.MAX_VALUE, true),
			a != IFluidAccess.NOP, b != IFluidAccess.NOP
		};
	}

}
