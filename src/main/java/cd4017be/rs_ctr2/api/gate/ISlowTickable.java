package cd4017be.rs_ctr2.api.gate;

/**To implement by objects that want to run continuous updates
 * every 8 ticks by {@link GateUpdater}.
 * @author CD4017BE */
@FunctionalInterface
public interface ISlowTickable {

	/**Called by GateUpdater every 8 ticks
	 * @return true to continue ticking, false to unregister */
	boolean tick8();

}
