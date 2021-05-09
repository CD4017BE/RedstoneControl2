package cd4017be.rs_ctr2.api.gate.ports;

import static cd4017be.rs_ctr2.api.gate.GateUpdater.REC_DATA;

@FunctionalInterface
public interface ISignalReceiver {

	/**@param value updated signal state */
	default void updateInput(int value) {
		updateInput(value, REC_DATA);
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
