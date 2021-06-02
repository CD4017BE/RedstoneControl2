package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.rs_ctr2.Content.and_filter;

import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.IPlayerPacketReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.rs_ctr2.container.ContainerConstant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

/**
 * @author CD4017BE */
public class AndFilter extends NotGate
implements IUnnamedContainerProvider, IPlayerPacketReceiver {

	@Sync(to = SAVE|GUI)
	public int value = -1;

	@Override
	public Item item() {
		return and_filter;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port != 0) (out = ISignalReceiver.of(handler)).updateInput(state & value);
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || (state ^ (state = value) & this.value) == 0) return;
		out.updateInput(value & this.value, rec);
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand == null || player.isCrouching())
			return super.onInteract(player, hand, hit, pos);
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		player.openMenu(this);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ContainerConstant createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerConstant(id, inv, this);
	}

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		int v = pkt.readInt();
		if (v != value) out.updateInput(state & (value = v));
	}

	@Override
	public Object[] stateInfo() {
		return new Object[]{"state.rs_ctr2.and_filter", value, state, state & value};
	}

}
