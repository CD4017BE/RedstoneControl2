package cd4017be.rs_ctr2.api.gate.ports;

import cd4017be.rs_ctr2.api.gate.Link;
import net.minecraftforge.energy.IEnergyStorage;

/**Grid port handler for energy transfer.
 * The energy unit is the same as in {@link IEnergyStorage}.
 * @author CD4017BE */
public interface IEnergyAccess {

	/**@param amount to fill (> 0) or drain (< 0)
	 * @param test whether this is a test and no power is actually transfered
	 * @param rec number of remaining recursions before "giving up"
	 * @return amount actually transferred */
	int transferEnergy(int amount, boolean test, int rec);

	/**@param amount to fill (> 0) or drain (< 0)
	 * @param test whether this is a test and no power is actually transfered
	 * @return amount actually transferred */
	default int transferEnergy(int amount, boolean test) {
		return transferEnergy(amount, test, Link.REC_POWER);
	}

	/** does nothing */
	IEnergyAccess NOP = (v, t, r) -> 0;

	/** port type id */
	int TYPE_ID = 1;

	static IEnergyAccess of(Object handler) {
		return handler instanceof IEnergyAccess ? (IEnergyAccess)handler : NOP;
	}
}
