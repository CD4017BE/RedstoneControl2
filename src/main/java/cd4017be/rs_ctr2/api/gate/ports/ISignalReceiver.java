package cd4017be.rs_ctr2.api.gate.ports;

import cd4017be.rs_ctr2.api.gate.Link;

@FunctionalInterface
public interface ISignalReceiver {

	/**@param value updated signal state */
	default void updateInput(int value) {
		updateInput(value, Link.REC_DATA);
	}

	/**@param value updated signal state
	 * @param rec number of remaining recursions before "giving up" */
	void updateInput(int value, int rec);

	/** does nothing */
	ISignalReceiver NOP = (v, r) -> {};

	/** port type id */
	int TYPE_ID = 0;

	static ISignalReceiver of(Object handler) {
		return handler instanceof ISignalReceiver ? (ISignalReceiver)handler : NOP;
	}

}
