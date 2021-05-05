package cd4017be.rs_ctr2.api.gate;

import java.util.function.Consumer;

import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;

public final class GateUpdater implements Consumer<ServerTickEvent> {

	/** The active GateUpdater instance */
	public static GateUpdater GATE_UPDATER;
	/** gate ticks counted since game application started */
	public static int TICK;

	/** looping array used as queue */
	private IGate[] updateQueue;
	private int start, end, mask;
	private boolean evaluating;
	public final MinecraftServer server;

	public GateUpdater(MinecraftServer server, int initialsize) {
		this.server = server;
		initialsize = Integer.highestOneBit(initialsize - 1) << 1;
		updateQueue = new IGate[initialsize];
		mask = initialsize - 1;
		start = end = 0;
	}

	@Override
	public void accept(ServerTickEvent event) {
		if (event.phase != Phase.END || start == end) return;
		IProfiler profiler = server.getProfiler();
		profiler.push("evaluate gates");
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
		TICK++;
		profiler.popPush("latch out gates");
		for (int i = e; i != j; i = i + 1 & m) {
			start = start + 1 & mask;
			IGate g = queue[i];
			queue[i] = null;
			g.latchOut();
		}
		profiler.pop();
	}

	public void add(IGate update) {
		if (evaluating) throw new IllegalStateException(
			"Scheduling gate updates is not allowed during evaluation!");
		updateQueue[end] = update;
		if ((end = end + 1 & mask) == start) grow();
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

	public int count() {
		return end - start & mask;
	}

}
