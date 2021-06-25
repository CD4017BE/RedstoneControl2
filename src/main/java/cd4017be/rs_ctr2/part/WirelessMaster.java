package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.IGridHost;
import cd4017be.rs_ctr2.item.WirelessItem;
import net.minecraft.util.math.BlockPos;


/**
 * @author CD4017BE */
public class WirelessMaster extends Wireless {

	Wireless other;
	Object out;

	public WirelessMaster(WirelessItem item) {
		super(item);
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) out = handler;
		else if (handler instanceof Wireless) other = (Wireless)handler;
		else if (other != null) other.setHandler(1, other = null);
		if (other != null) other.setHandler(1, out);
	}

	@Override
	public boolean isMaster(int port) {
		return true;
	}

	@Override
	public Object[] stateInfo() {
		String s;
		IGridHost host;
		if (other != null && (host = other.host) != null) {
			int p = Long.numberOfTrailingZeros(other.bounds);
			BlockPos pos = host.pos();
			s = String.format("\\(%d:%d,%d:%d,%d:%d)%s",
				pos.getX(), p & 3,
				pos.getY(), p >> 2 & 3,
				pos.getZ(), p >> 4 & 3,
				host.world().dimension().location().getPath()
			);
		} else s = "cd4017be.unloaded";
		return new Object[] {"state.rs_ctr2.wireless", link, s};
	}

}
