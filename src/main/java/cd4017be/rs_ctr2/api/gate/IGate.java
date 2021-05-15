package cd4017be.rs_ctr2.api.gate;

/**To implement by objects that want to run two stage updates via {@link GateUpdater}.
 * @author CD4017BE */
public interface IGate {

	/**evaluate the gate's new state, but don't update outputs yet.
	 * @return whether later call to {@link #latchOut()} is needed */
	boolean evaluate();

	/**notify other devices of changed outputs. */
	default void latchOut() {}

}
