package cd4017be.rs_ctr2;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;
import static cd4017be.rs_ctr2.api.gate.GateUpdater.GATE_UPDATER;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cd4017be.rs_ctr2.api.gate.GateUpdater;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@Mod(Main.ID)
public class Main {

	public static final String ID = "rs_ctr2";
	public static final Logger LOG = LogManager.getLogger(ID);

	public Main() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onServerStart(FMLServerStartedEvent event) {
		if (GATE_UPDATER != null) return;
		EVENT_BUS.addListener(GATE_UPDATER = new GateUpdater(16));
		GATE_UPDATER.profiler = event.getServer().getProfiler();
		LOG.info("gate updater started");
	}

	@SubscribeEvent
	public void onServerStop(FMLServerStoppingEvent event) {
		if (GATE_UPDATER == null) return;
		EVENT_BUS.unregister(GATE_UPDATER);
		LOG.info("gate updater shut down: had {} active updates", GATE_UPDATER.count());
		GATE_UPDATER = null;
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}

}
