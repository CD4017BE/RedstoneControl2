package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.item_mover;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static cd4017be.rs_ctr2.api.gate.GateUpdater.GATE_UPDATER;
import static java.lang.Math.abs;

import com.google.common.base.Predicates;

import cd4017be.lib.network.Sync;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.gate.IGate;
import cd4017be.rs_ctr2.api.gate.ports.IEnergyAccess;
import cd4017be.rs_ctr2.api.gate.ports.IInventoryAccess;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;


public class ItemMover extends OrientedPart implements ISignalReceiver, IGate {

	IInventoryAccess a = IInventoryAccess.NOP;
	IInventoryAccess b = IInventoryAccess.NOP;
	IEnergyAccess energy = IEnergyAccess.NOP;
	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync public int num = 64, clk, res;
	@Sync public boolean active;

	public ItemMover() {
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
		setPort(4, pos + 0x00, Direction.NORTH, IInventoryAccess.TYPE_ID);
		setPort(5, pos + 0x10, Direction.SOUTH, IInventoryAccess.TYPE_ID);
	}

	@Override
	public void setHost(IGridHost host) {
		super.setHost(host);
		if (host != null && active)
			GATE_UPDATER.add(this);
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
		case 4: a = IInventoryAccess.of(handler); break;
		case 5: b = IInventoryAccess.of(handler); break;
		}
	}

	@Override
	public boolean isMaster(int channel) {
		return channel >= 2;
	}

	@Override
	public Item item() {
		return item_mover;
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
		long e = (-1L - abs((long)am)) * SERVER_CFG.move_item.get() >> 6;
		return e < 0xc0000000 ? 0xc0000000 : (int)e;
	}

}