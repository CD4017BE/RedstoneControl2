package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.rs_ctr2.Content.constant;

import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.IPlayerPacketReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.gate.ISignalReceiver;
import cd4017be.rs_ctr2.container.ContainerConstant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;


public class Constant extends OrientedPart
implements IUnnamedContainerProvider, IPlayerPacketReceiver {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync(to = SAVE|GUI)
	public int value;

	public Constant() {
		super(1);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (handler instanceof ISignalReceiver)
			(out = (ISignalReceiver)handler).updateInput(value);
		else out = ISignalReceiver.NOP;
	}

	@Override
	public boolean isMaster(int channel) {
		return true;
	}

	@Override
	public Item item() {
		return constant;
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
		if (v != value)
			out.updateInput(value = v);
	}

}
