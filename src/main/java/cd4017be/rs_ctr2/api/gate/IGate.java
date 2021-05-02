package cd4017be.rs_ctr2.api.gate;


public interface IGate {

	/**evaluate the gate's new state, but don't update outputs yet.
	 * @return whether later call to {@link #latchOut()} is needed */
	boolean evaluate();

	/**notify other devices of changed outputs. */
	void latchOut();

}
