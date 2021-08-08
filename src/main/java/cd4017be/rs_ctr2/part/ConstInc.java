package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.const_inc;

import cd4017be.api.grid.port.ISignalReceiver;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;

/**
 * @author CD4017BE */
public class ConstInc extends AndFilter {

	{value = 1;}

	@Override
	public Item item() {
		return const_inc;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port != 0) (out = ISignalReceiver.of(handler)).updateInput(state + value);
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || state == (state = value)) return;
		out.updateInput(value + this.value, rec);
	}

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		int v = pkt.readInt();
		if (v != value) out.updateInput(state + (value = v));
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{
			"state.rs_ctr2.const_inc",
			value, state, state + value
		};
	}

}
