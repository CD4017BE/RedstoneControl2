package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.lib.network.Sync.Type.Enum;
import static cd4017be.rs_ctr2.Content.led_array;
import static cd4017be.rs_ctr2.util.Utils.heldColor;
import static cd4017be.rs_ctr2.util.Utils.serverAction;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import cd4017be.api.grid.IDynamicPart;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
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
public class LEDArray extends ExtendablePart implements IDynamicPart, ISignalReceiver {

	@Sync(to = ALL)
	public byte state;
	@Sync(to = ALL, type = Enum)
	public DyeColor color = DyeColor.RED;

	public LEDArray() {
		super(1);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setPort(0, pos + (pos >> 6 & 3), Direction.NORTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return led_array;
	}

	@Override
	public byte getLayer() {
		return L_INNER;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		DyeColor color = heldColor(player, hand);
		return color != null ? serverAction(player, ()-> {
			this.color = color;
			host.onPartChange();
		}) : super.onInteract(player, hand, hit, pos);
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
		if (state != (state = (byte)value) && host != null)
			host.updateDisplay();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected ResourceLocation model() {
		return Main.rl("part/switch_array" + (pos >> 6 & 3));
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
		ms.translate(-0.0625, 0, 0);
		IVertexBuilder vb = rtb.getBuffer(RenderType.solid());
		for (int i = pos >> 5 & 6 | 1; i >= 0; i--) {
			boolean active = (state >> i & 1) != 0;
			GridModels.draw(
				LED.LED, ms.last(), vb,
				active ? color : color >> 2 & 0xff3f3f3f,
				active ? 0xf0 : light, overlay
			);
			ms.translate(0.125, 0, 0);
		}
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
