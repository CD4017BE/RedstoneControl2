package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.rs_ctr2.Content.and_filter;
import static cd4017be.rs_ctr2.container.ContainerBitShuffle.*;

import cd4017be.lib.network.Sync;
import cd4017be.rs_ctr2.container.ContainerBitShuffle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;

/**
 * @author CD4017BE */
public class BitShuffle extends ConstInc {

	{value = -1;};
	@Sync(to = SAVE|GUI)
	public int xor;
	@Sync(to = SAVE|GUI)
	public byte shift;

	@Override
	public Item item() {
		return and_filter;
	}

	@Override
	protected int result() {
		return Integer.rotateLeft(in & value, shift) ^ xor;
	}

	@Override
	protected int change(int old, int in) {
		return (old ^ in) & value;
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerBitShuffle(id, inv, this);
	}

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		switch(pkt.readByte()) {
		case A_AND:
			if (value != (value = pkt.readInt())) break;
			else return;
		case A_XOR:
			if (xor != (xor = pkt.readInt())) break;
			else return;
		case A_SHIFT:
			if (shift != (shift = pkt.readByte())) break;
		default: return;
		}
		out.updateInput(result());
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{
			"state.rs_ctr2.bit_shuffle",
			result(), in, value, shift, xor
		};
	}

}
