package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.Type.Enum;
import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static cd4017be.math.Linalg.dadd;
import static cd4017be.math.Linalg.sca;
import static cd4017be.rs_ctr2.Content.*;
import static cd4017be.rs_ctr2.util.Utils.heldColor;
import static cd4017be.rs_ctr2.util.Utils.serverAction;
import static java.lang.Math.max;
import static java.lang.Math.min;

import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.api.grid.IDynamicPart;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.IPlayerPacketReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.render.GridModels;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.container.ContainerButton;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * @author CD4017BE */
public class Button extends OrientedPart
implements IDynamicPart, IGate, IUnnamedContainerProvider, IPlayerPacketReceiver {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync(to = ALL|GUI) public byte delay = 5;
	@Sync(to = ALL) public byte state;
	@Sync(to = ALL, type = Enum)
	public DyeColor color = DyeColor.RED;

	public Button() {
		super(1);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return button;
	}

	@Override
	public byte getLayer() {
		return L_INNER;
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		(out = ISignalReceiver.of(handler)).updateInput(state != 0 ? -1 : 0);
	}

	@Override
	public boolean isMaster(int port) {
		return true;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		boolean crouch = player.isCrouching();
		if (hand == null || crouch && !player.getItemInHand(hand).isEmpty())
			return super.onInteract(player, hand, hit, pos);
		DyeColor color = heldColor(player, hand);
		return serverAction(player,
			color != null ? ()-> {
				this.color = color;
				host.onPartChange();
			} :
			crouch ? ()-> player.openMenu(this) :
			()-> {
				if (state == 0) {
					GATE_UPDATER.add(this);
					state = -1;
				} else state = delay;
			}
		);
	}

	@Override
	public boolean evaluate() {
		if (host == null) return false;
		return true;
	}

	@Override
	public void latchOut() {
		if (state == -1) {
			state = delay;
			changeState(-1);
		} else if (--state == 0) {
			changeState(0);
			return;
		}
		GATE_UPDATER.add(this);
	}

	private void changeState(int state) {
		float[] vec = sca(3, dadd(3, vec(pos(pos, orient)), .5F), .25F);
		BlockPos bp = host.pos();
		host.world().playSound(null,
			(double)bp.getX() + vec[0],
			(double)bp.getY() + vec[1],
			(double)bp.getZ() + vec[2],
			state < 0 ? BUTTON_PRESs : BUTTON_RELEASe,
			SoundCategory.BLOCKS, 3F, 2F
		);
		host.updateDisplay();
		out.updateInput(state);
	}

	public static final ResourceLocation
	ON = Main.rl("part/button_on"), OFF = Main.rl("part/button_off");

	@Override
	@OnlyIn(Dist.CLIENT)
	protected ResourceLocation model() {
		return Switch.BASE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(MatrixStack ms, IRenderTypeBuffer rtb, int light, int overlay, float t, long opaque) {
		if ((bounds & ~opaque) == 0) return;
		ms.pushPose();
		transform(ms);
		GridModels.draw(
			state != 0 ? ON : OFF, ms.last(),
			rtb.getBuffer(RenderType.solid()),
			color.getTextColor(),
			state != 0 ? 0xf0 : light, overlay
		);
		ms.popPose();
	}

	@Override
	public void readSync(PacketBuffer pkt) {
		state = pkt.readByte();
	}

	@Override
	public void writeSync(PacketBuffer pkt, boolean init) {
		pkt.writeByte(state);
	}

	@Override
	public ContainerButton createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerButton(id, inv, this);
	}

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		int d = min(max(pkt.readByte() & 0xff, 1), 250);
		delay = (byte)d;
		if ((state + 1 & 0xff) - 1 > d) state = delay;
	}

}
