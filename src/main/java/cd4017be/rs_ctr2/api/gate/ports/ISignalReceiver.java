package cd4017be.rs_ctr2.api.gate.ports;

@FunctionalInterface
public interface ISignalReceiver {

	void updateInput(int value);

	/** does nothing */
	ISignalReceiver NOP = v -> {};

	/** port type id */
	int TYPE_ID = 0;

}
