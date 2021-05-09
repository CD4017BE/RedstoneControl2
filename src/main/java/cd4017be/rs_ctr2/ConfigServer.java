package cd4017be.rs_ctr2;

import static cd4017be.rs_ctr2.api.gate.GateUpdater.*;

import java.util.function.Consumer;

import cd4017be.lib.config.Config;
import cd4017be.rs_ctr2.api.gate.GateUpdater;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class ConfigServer extends Config implements Consumer<FMLServerStartingEvent> {

	public final IntValue battery_cap, move_item, move_fluid;
	public final IntValue rec_data, rec_power, rec_item, rec_fluid;

	protected ConfigServer() {
		super(Type.SERVER);
		Builder b = new Builder();
		b.push("energy");
		battery_cap = b.defineInRange("battery_cap", 16000, 1, Integer.MAX_VALUE / 64);
		move_item = b.defineInRange("move_item", 64, 0, Integer.MAX_VALUE);
		move_fluid = b.defineInRange("move_fluid", 64, 0, Integer.MAX_VALUE);
		b.pop();
		b.push("recursion_limit");
		rec_data = b.defineInRange("data", REC_DATA, 0, 64);
		rec_power = b.defineInRange("power", REC_POWER, 0, 64);
		rec_item = b.defineInRange("power", REC_ITEM, 0, 64);
		rec_fluid = b.defineInRange("power", REC_FLUID, 0, 64);
		b.pop();
		finish(b);
		MinecraftForge.EVENT_BUS.addListener(this);
	}

	@Override
	public void accept(FMLServerStartingEvent t) {
		GateUpdater.TICK = 0;
		GateUpdater.REC_DATA = rec_data.get();
		GateUpdater.REC_POWER = rec_power.get();
		GateUpdater.REC_ITEM = rec_item.get();
		GateUpdater.REC_FLUID = rec_fluid.get();
	}

}
