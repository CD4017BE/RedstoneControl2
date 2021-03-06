package cd4017be.rs_ctr2.part;

import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.lib.tick.GateUpdater.TICK;
import static cd4017be.rs_ctr2.Content.solarcell;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static java.lang.Math.round;

import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.IGridItem;
import cd4017be.api.grid.port.IEnergyAccess;
import cd4017be.lib.tick.ISlowTickable;
import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.Main;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

/**@author CD4017BE */
public class SolarCell extends MultiBlock<SolarCell> implements ISlowTickable {

	public static float INV_DAY_LENGHT;

	IEnergyAccess energy = IEnergyAccess.NOP;
	int power = -1;
	boolean active, fixTime;

	public SolarCell(int pos) {
		super(pos);
	}

	@Override
	public Item item() {
		return solarcell;
	}

	@Override
	protected void onBoundsChange() {
		power = -1;
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		energy = IEnergyAccess.of(handler);
	}

	@Override
	public boolean isMaster(int port) {
		return true;
	}

	@Override
	public SolarCell setHost(IGridHost host) {
		super.setHost(host);
		if (host == null || active) return this;
		DimensionType dt = host.world().dimensionType();
		if (!dt.hasSkyLight()) return this;
		fixTime = dt.hasFixedTime();
		active = true;
		GATE_UPDATER.add(this);
		return this;
	}

	@Override
	public boolean tick8() {
		if (host == null) return active = false;
		if (power < 0 || (TICK & 0x38) == 0) updatePower();
		int p = power();
		if (p > 0) energy.transferEnergy(p, false);
		return true;
	}

	private int power() {
		return fixTime ? power : sunPower(power,
			(int)((host.world().dayTime() + 6000) % 24000) - 12000
		);
	}

	private void updatePower() {
		World world = host.world();
		long b = host.bounds();
		b = b >> 4 & 0x0fff_0fff_0fff_0fffL
		  | b >> 8 & 0x00ff_00ff_00ff_00ffL
		  | b >> 12 & 0x000f_000f_000f_000fL;
		power = Long.bitCount(bounds & ~b) * SERVER_CFG.solar_power.get()
		* world.getBrightness(LightType.SKY, host.pos().above()) / 15;
		if (fixTime) {
			float t = world.getTimeOfDay(1F);
			power = sunPower(power, (t > 0.5F ? t - 1 : t) * 24000F);
		}
	}

	private static int sunPower(int base, float dt) {
		dt *= INV_DAY_LENGHT;
		return round((1F - dt * dt) * base);
	}

	@Override
	protected ActionResultType createPort(IGridItem item, short port, boolean client) {
		if (item != Content.power_cable) return ActionResultType.PASS;
		if (client) return ActionResultType.CONSUME;
		IGridHost host = this.host;
		host.removePart(this);
		if (ports.length == 0) ports = new short[1];
		ports[0] = (short)(port | IEnergyAccess.TYPE_ID << 12);
		host.addPart(this);
		return ActionResultType.SUCCESS;
	}

	@Override
	protected SolarCell splitOff(long splitBounds, long thisBounds) {
		return new SolarCell(-1);
	}

	@Override
	protected short[] merge(SolarCell other) {
		return ports.length == 0 && other.ports.length != 0
			? other.ports : ports;
	}

	@Override
	protected ResourceLocation model() {
		return MODEL;
	}

	public static final ResourceLocation MODEL = Main.rl("part/solarcell");

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			"state.rs_ctr2.solar", Math.max(power(), 0),
			energy.transferEnergy(Integer.MAX_VALUE, true)
		};
	}

}
