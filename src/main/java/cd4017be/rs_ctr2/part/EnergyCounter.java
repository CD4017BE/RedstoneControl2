package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.power_counter;
import static java.lang.Integer.MAX_VALUE;

import cd4017be.api.grid.port.IEnergyAccess;
import net.minecraft.item.Item;


/**
 * @author CD4017BE */
public class EnergyCounter extends ResourceCounter {

	IEnergyAccess inv = IEnergyAccess.NOP;

	@Override
	public Item item() {
		return power_counter;
	}

	@Override
	protected int type() {
		return IEnergyAccess.TYPE_ID;
	}

	@Override
	protected void setSource(Object handler) {
		inv = IEnergyAccess.of(handler);
	}

	@Override
	protected void count() {
		state = Math.abs(inv.transferEnergy(empty ? MAX_VALUE : -MAX_VALUE, true));
	}

	@Override
	protected String message(boolean empty) {
		return empty ? "msg.rs_ctr2.count_cap" : "msg.rs_ctr2.count_energy";
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			empty ? "state.rs_ctr2.cap_counter" : "state.rs_ctr2.energy_counter",
			state, inv != IEnergyAccess.NOP, clk
		};
	}

}
