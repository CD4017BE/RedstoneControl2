package cd4017be.rs_ctr2.api.gate;

public interface IPortHolder {

	Object getHandler(int port);

	void setHandler(int port, Object handler);

	boolean isMaster(int channel);

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