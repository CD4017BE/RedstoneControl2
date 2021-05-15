package cd4017be.rs_ctr2.api.gate;

import cd4017be.rs_ctr2.api.gate.ports.*;
import cd4017be.rs_ctr2.api.grid.GridPart;

/**API for the port connection system, mostly used by {@link GridPart}s.
 * It lets master ports interact with provider ports via a handler
 * object that is passed from provider to master when connecting.
 * @see IEnergyAccess
 * @see IFluidAccess
 * @see IInventoryAccess
 * @see ISignalReceiver
 * @author CD4017BE */
public interface IPortHolder {

	/**@param port index for identification
	 * @return the handler object of the given provider port.
	 * May be null if not available! */
	Object getHandler(int port);

	/**@param port index for identification
	 * @param handler connecting to the given master port or null to disconnect. */
	void setHandler(int port, Object handler);

	/**@param port index for identification
	 * @return whether the given port is a master that implements via
	 * {@link #setHandler()} rather than {@link #getHandler()} */
	boolean isMaster(int port);


	/**Represents a single port on a {@link IPortHolder}.
	 * @author CD4017BE */
	class Port {
		public final IPortHolder host;
		public final int channel;

		public Port(IPortHolder host, int channel) {
			this.host = host;
			this.channel = channel;
		}

		public boolean isMaster() {
			return host.isMaster(channel);
		}

		public void setHandler(Object handler) {
			host.setHandler(channel, handler);
		}

		public Object getHandler() {
			return host.getHandler(channel);
		}

		public boolean connect(Port other) {
			boolean master = isMaster();
			if (!master ^ other.isMaster()) return false;
			if (master) setHandler(other.getHandler());
			else other.setHandler(getHandler());
			return true;
		}
	}

}