package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.lib.network.Sync.Type.Enum;
import static cd4017be.rs_ctr2.Content.led;
import static cd4017be.rs_ctr2.util.Utils.heldColor;
import static cd4017be.rs_ctr2.util.Utils.serverAction;

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
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * @author CD4017BE */
public class LED extends OrientedPart implements IDynamicPart, ISignalReceiver {

	@Sync(to = ALL)
	public boolean state;
	@Sync(to = ALL, type = Enum)
	public DyeColor color = DyeColor.RED;

	public LED() {
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
		return led;
	}

	@Override
	public byte getLayer() {
		return L_INNER;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand == null) return super.onInteract(player, hand, hit, pos);
		DyeColor color = heldColor(player, hand);
		return color != null ?
			serverAction(player, ()-> {
				this.color = color;
				host.onPartChange();
			}) : ActionResultType.PASS;
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int port) {
		return false;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (state ^ (state = value != 0) && host != null)
			host.updateDisplay();
	}

	public static final ResourceLocation LED = Main.rl("part/led");

	@Override
	@OnlyIn(Dist.CLIENT)
	protected ResourceLocation model() {
		return Switch.BASE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(
		MatrixStack ms, IRenderTypeBuffer rtb, int light, int overlay, float t, long opaque
	) {
		if ((bounds & ~opaque) == 0) return;
		ms.pushPose();
		transform(ms);
		int color = this.color.getTextColor() | 0xff000000;
		GridModels.draw(
			LED, ms.last(),
			rtb.getBuffer(RenderType.solid()),
			state ? color : color >> 2 & 0xff3f3f3f,
			state ? 0xf0 : light, overlay
		);
		ms.popPose();
	}

	@Override
	public void readSync(PacketBuffer pkt) {
		state = pkt.readBoolean();
	}

	@Override
	public void writeSync(PacketBuffer pkt, boolean init) {
		pkt.writeBoolean(state);
	}

}
