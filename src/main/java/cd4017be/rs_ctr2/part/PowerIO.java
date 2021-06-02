package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.power_io;

import cd4017be.api.grid.port.IBlockSupplier;
import cd4017be.api.grid.port.IEnergyAccess;
import cd4017be.lib.capability.NullEnergyStorage;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**@author CD4017BE */
public class PowerIO extends CapabilityIO<IEnergyStorage>
implements IEnergyAccess, IProbeInfo {

	public PowerIO() {
		super(2);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, IBlockSupplier.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, IEnergyAccess.TYPE_ID);
	}

	@Override
	public Item item() {
		return power_io;
	}

	@Override
	protected Capability<IEnergyStorage> capability() {
		return CapabilityEnergy.ENERGY;
	}

	@Override
	public int transferEnergy(int amount, boolean test, int rec) {
		IEnergyStorage inv = get(NullEnergyStorage.INSTANCE);
		return amount > 0 ? inv.receiveEnergy(amount, test)
			: amount < 0 ? -inv.extractEnergy(-amount, test)
			: 0;
	}

	@Override
	public Object[] stateInfo() {
		IEnergyStorage inv = get(NullEnergyStorage.INSTANCE);
		return new Object[] {
			"state.rs_ctr2.power_io",
			IBlockSupplier.toString(block),
			inv.getEnergyStored(),
			inv.getMaxEnergyStored()
		};
	}

}
