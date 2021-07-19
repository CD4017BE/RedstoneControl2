package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.lib.network.Sync.Type.Enum;
import static cd4017be.rs_ctr2.Content._7segment;
import static cd4017be.rs_ctr2.util.Utils.heldColor;
import static cd4017be.rs_ctr2.util.Utils.serverAction;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import cd4017be.api.grid.IDynamicPart;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.render.GridModels;
import cd4017be.lib.util.Orientation;
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
public class _7Segment extends ExtendablePart implements IDynamicPart, ISignalReceiver {

	@Sync(to = ALL)
	public int state;
	@Sync(to = ALL, type = Enum)
	public DyeColor color = DyeColor.RED;

	public _7Segment() {
		super(1);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setPort(0, pos + (pos >> 6 & 3), Direction.NORTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return _7segment;
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
		if (state != (state = value) && host != null)
			host.updateDisplay();
	}

	public static final ResourceLocation[] MODELS = new ResourceLocation[16];

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(
		MatrixStack ms, IRenderTypeBuffer rtb, int light, int overlay, float t, long opaque
	) {
		ms.pushPose();
		transform(ms);
		int color = this.color.getTextColor() | 0xff000000;
		IVertexBuilder vb = rtb.getBuffer(RenderType.cutout());
		for (int i = pos >> 5 & 6 | 1; i >= 0; i--) {
			int seg = state >> (i << 2) & 15;
			GridModels.draw(MODELS[seg], ms.last(), vb, color, 0xf0, overlay);
			ms.translate(0.125, 0, 0);
		}
		ms.popPose();
	}

	@Override
	public void readSync(PacketBuffer pkt) {
		state = pkt.readInt();
	}

	@Override
	public void writeSync(PacketBuffer pkt, boolean init) {
		pkt.writeInt(state);
	}

}
