package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.rs_ctr2.Content.SWITCH_FLIp;
import static cd4017be.rs_ctr2.Content.switcH;

import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.api.grid.IDynamicPart;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.render.GridModels;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.Main;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * @author CD4017BE */
public class Switch extends OrientedPart implements IDynamicPart {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync(to = ALL) public byte state;

	public Switch() {
		super(1);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand == null || player.isCrouching())
			return super.onInteract(player, hand, hit, pos);
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		out.updateInput(state = (byte)~state);
		host.updateDisplay();
		Vector3d vec = hit.getLocation();
		player.level.playSound(null, vec.x, vec.y, vec.z, SWITCH_FLIp, SoundCategory.BLOCKS, 1F, 0.5F);
		return ActionResultType.SUCCESS;
	}

	@Override
	public Item item() {
		return switcH;
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		(out = ISignalReceiver.of(handler)).updateInput(state);
	}

	@Override
	public boolean isMaster(int port) {
		return true;
	}

	@Override
	public byte getLayer() {
		return L_INNER;
	}

	public static final ResourceLocation BASE = Main.rl("part/switch"),
	ON = Main.rl("part/switch_on"), OFF = Main.rl("part/switch_off");

	@Override
	@OnlyIn(Dist.CLIENT)
	protected ResourceLocation model() {
		return BASE;
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
			-1, light, overlay
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

}
