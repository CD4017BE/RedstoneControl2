package cd4017be.rs_ctr2.api.gate;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

/**Updates arbitrary objects in two possible ways:
 * {@link #GATE_UPDATER}.{@link #add(IGate)} for a single two stage update and
 * {@link #GATE_UPDATER}.{@link #add(ISlowTickable)} for continuous updates every 8 ticks.
 * <br>{@link ISlowTickable#tick8()} will run directly after all {@link IGate#latchOut()}.
 * <p> Call {@link MinecraftForge#EVENT_BUS}.register(GateUpdater.class)
 * during game startup to make this available. </p>
 * @author CD4017BE */
public final class GateUpdater implements Consumer<ServerTickEvent> {

	private static final Logger LOG = LogManager.getLogger();
	/** The active GateUpdater instance */
	public static GateUpdater GATE_UPDATER;
	/** ticks counted since last server start */
	public static int TICK;

	public final MinecraftServer server;
	private IGate[] updateQueue;
	private ISlowTickable[] slowTicks;
	private int start, end, mask, slowCount;
	private boolean evaluating;

	public GateUpdater(MinecraftServer server) {
		this.server = server;
		this.updateQueue = new IGate[16];
		this.mask = 15;
		this.slowTicks = new ISlowTickable[8];
		this.slowCount = this.start = this.end = 0;
	}

	/**@param update to run in two stages during the next end of a server tick */
	public void add(IGate update) {
		if (evaluating) throw new IllegalStateException(
			"Scheduling gate updates is not allowed during evaluation!");
		updateQueue[end] = update;
		if ((end = end + 1 & mask) == start) grow();
	}

	/**@param update to run continuously every 8 ticks at the end of a server tick */
	public void add(ISlowTickable update) {
		if (slowCount >= slowTicks.length)
			slowTicks = Arrays.copyOf(slowTicks, slowCount << 1);
		slowTicks[slowCount++] = update;
	}

	@Override
	public void accept(ServerTickEvent event) {
		if (event.phase != Phase.END) return;
		IProfiler profiler = server.getProfiler();
		profiler.push("GateUpdater");
		TICK++;
		if (start != end) tickGates(profiler);
		if ((TICK & 7) < slowCount) tickSlow(profiler);
		profiler.pop();
	}

	private void tickGates(IProfiler profiler) {
		profiler.push("evaluate");
		evaluating = true;
		IGate[] queue = updateQueue;
		int m = mask, e = end, j = e;
		for (int i = start; i != e; i = i + 1 & m) {
			IGate g = queue[i];
			queue[i] = null;
			if (g.evaluate()) {
				queue[j] = g;
				j = j + 1 & m;
			}
		}
		evaluating = false;
		start = e; end = j;
		profiler.popPush("latchOut");
		for (int i = e; i != j; i = i + 1 & m) {
			start = start + 1 & mask;
			IGate g = queue[i];
			queue[i] = null;
			g.latchOut();
		}
		profiler.pop();
	}

	private void tickSlow(IProfiler profiler) {
		profiler.push("tick8");
		for (int i = TICK & 7; i < slowCount; i+=8) {
			if (slowTicks[i].tick8()) continue;
			slowTicks[i] = slowTicks[--slowCount];
			slowTicks[slowCount] = null;
			i -= 8;
		}
		profiler.pop();
	}

	private void grow() {
		int l = updateQueue.length;
		IGate[] arr = new IGate[l << 1];
		System.arraycopy(updateQueue, start, arr, 0, l - start);
		System.arraycopy(updateQueue, 0, arr, l - start, start);
		updateQueue = arr;
		mask = arr.length - 1;
		start = 0;
		end = l;
	}

	private int count() {
		return end - start & mask;
	}

	@SubscribeEvent
	public static void onServerStart(FMLServerAboutToStartEvent event) {
		if (GATE_UPDATER != null) return;
		EVENT_BUS.addListener(GATE_UPDATER = new GateUpdater(event.getServer()));
		TICK = 0;
		LOG.info("GATE_UPDATER started");
	}

	@SubscribeEvent
	public static void onServerStop(FMLServerStoppingEvent event) {
		Link.clear();
		if (GATE_UPDATER == null) return;
		EVENT_BUS.unregister(GATE_UPDATER);
		LOG.info("GATE_UPDATER shut down: had {} active gate updates and {} slow ticks",
			GATE_UPDATER.count(), GATE_UPDATER.slowCount);
		GATE_UPDATER = null;
	}

}
