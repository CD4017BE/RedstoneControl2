package cd4017be.rs_ctr2.part;

import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.rs_ctr2.Content.battery;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static java.lang.Math.min;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.IGridItem;
import cd4017be.api.grid.port.IEnergyAccess;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tick.IGate;
import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.Main;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * @author CD4017BE */
public class Battery extends MultiBlock<Battery> implements IGate, IEnergyAccess {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync public int energy, state;
	@Sync public boolean active;
	int cap;

	public Battery(int pos) {
		super(pos);
	}

	@Override
	public Item item() {
		return battery;
	}

	@Override
	public void setHost(IGridHost host) {
		super.setHost(host);
		if (active && host != null)
			GATE_UPDATER.add(this);
	}

	@Override
	protected void onBoundsChange() {
		cap = Long.bitCount(bounds) * SERVER_CFG.battery_cap.get();
		energy = min(energy, cap);
	}

	@Override
	protected ActionResultType onInteract(PlayerEntity player, boolean client) {
		if (client) return ActionResultType.CONSUME;
		player.displayClientMessage(new TranslationTextComponent(
			"msg.rs_ctr2.battery", energy, cap, Long.bitCount(bounds)
		), true);
		return ActionResultType.SUCCESS;
	}

	@Override
	protected ActionResultType createPort(IGridItem item, short port, boolean client) {
		if (item == Content.power_cable) {
			if (client) return ActionResultType.CONSUME;
			//add power port
			IGridHost host = this.host;
			host.removePart(this);
			ports = ArrayUtils.add(ports, (short)(port | IEnergyAccess.TYPE_ID << 12));
			host.addPart(this);
			return ActionResultType.SUCCESS;
		}
		if (item == Content.data_cable) {
			if (client) return ActionResultType.CONSUME;
			// add or change data port
			IGridHost host = this.host;
			host.removePart(this);
			if (ports.length == 0 || (ports[0] & 0xf000) != 0) {
				short[] arr = ports;
				System.arraycopy(arr, 0, ports = new short[arr.length + 1], 1, arr.length);
			}
			ports[0] = port;
			host.addPart(this);
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	protected Battery splitOff(long b1, long b) {
		Battery part = new Battery(-1);
		part.energy = (int)((long)energy * Long.bitCount(b1) / Long.bitCount(b));
		energy -= part.energy;
		return part;
	}

	@Override
	protected short[] merge(Battery other) {
		energy += other.energy;
		short[] p = other.ports, ports = this.ports;
		int i0 = p.length > 0 && (p[0] & 0xf000) == 0 ? 1 : 0;
		if (p.length <= i0) return ports;
		// add other's ports
		int l = ports.length;
		ports = Arrays.copyOf(ports, l + p.length - i0);
		System.arraycopy(p, i0, ports, l, p.length - i0);
		return ports;
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		(out = ISignalReceiver.of(handler)).updateInput(state);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel == 0 && ports.length > 0
		&& (ports[0] & 0xf000) == 0;
	}

	@Override
	public int transferEnergy(int amount, boolean test, int rec) {
		if (amount == 0) return 0;
		if (amount < -energy) amount = -energy;
		else if (amount > cap - energy) amount = cap - energy;
		if (!test) {
			energy += amount;
			if (!active) {
				active = true;
				GATE_UPDATER.add(this);
			}
		}
		return amount;
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null) return false;
		state = energy;
		return true;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{"state.rs_ctr2.battery", energy, cap};
	}

	@Override
	public void latchOut() {
		out.updateInput(state);
	}

	@Override
	public boolean dissassemble(World world, BlockPos pos) {
		energy = 0;
		return true;
	}

	@Override
	protected ResourceLocation model() {
		return MODEL;
	}

	public static final ResourceLocation MODEL = Main.rl("part/battery");

}
