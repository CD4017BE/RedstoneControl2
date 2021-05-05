package cd4017be.rs_ctr2.api.gate.ports;

import net.minecraftforge.energy.IEnergyStorage;

/**Grid port handler for energy transfer.
 * The energy unit is the same as in {@link IEnergyStorage}.
 * @author CD4017BE */
public interface IEnergyAccess {

	/**@param amount to fill (> 0) or drain (< 0)
	 * @return amount actually transferred */
	int transferEnergy(int amount);

	/** does nothing */
	IEnergyAccess NOP = v -> 0;

	/** port type id */
	int TYPE_ID = 1;
}
