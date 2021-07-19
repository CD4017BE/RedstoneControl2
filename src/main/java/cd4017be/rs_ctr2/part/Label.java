package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.Type.Enum;
import static cd4017be.math.Linalg.*;
import static cd4017be.math.MCConv.blockRelVecF;
import static cd4017be.math.Orient.orient;
import static cd4017be.rs_ctr2.Content.label;
import static cd4017be.rs_ctr2.util.Utils.*;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.api.grid.IDynamicPart;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.IPlayerPacketReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.render.MicroBlockFace;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.container.ContainerLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * @author CD4017BE */
public class Label extends ExtendablePart
implements IDynamicPart, IPlayerPacketReceiver, IUnnamedContainerProvider {

	@Sync(to=ALL|GUI)
	public String text = "Label text";
	@Sync(to = ALL, type = Enum)
	public DyeColor lc = DyeColor.WHITE, tc = DyeColor.BLACK;

	public Label() {
		super(0);
	}

	@Override
	public Item item() {
		return label;
	}

	@Override
	public byte getLayer() {
		return L_INNER;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		ActionResultType ret = super.onInteract(player, hand, hit, pos);
		if (ret != ActionResultType.PASS) return ret;
		DyeColor color = heldColor(player, hand);
		return color != null ?
			serverAction(player, ()-> {
				float[] vec = blockRelVecF(hit.getLocation(), hit.getBlockPos());
				vec = orient(orient.inv().o, dadd(3, subsca(3, vec, vec(pos), .25F), -.125F));
				if (Math.abs(vec[1]) < 0.0625F)
					tc = color;
				else lc = color;
				host.onPartChange();
			})
		: player.getItemInHand(hand).isEmpty() ?
			serverAction(player, ()-> player.openMenu(this))
		: ActionResultType.PASS;
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int port) {
		return false;
	}

	@Override
	public void readSync(PacketBuffer pkt) {}

	@Override
	public void writeSync(PacketBuffer pkt, boolean init) {}

	@OnlyIn(Dist.CLIENT)
	private int w, h;

	@OnlyIn(Dist.CLIENT)
	private void updateSize() {
		@SuppressWarnings("resource")
		FontRenderer fr = Minecraft.getInstance().font;
		int w = 0, h = 1;
		for(String line : text.split("\n", 4)) {
			h += fr.lineHeight;
			w = Math.max(fr.width(line), w);
		}
		this.w = w + 1;
		this.h = h;
	}

	public static final ResourceLocation MODEL = Main.rl("part/label");

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillModel(JitBakedModel model, long opaque) {
		if ((bounds & ~opaque) == 0) return;
		updateSize();
		float[] size = {((pos >> 6 & 3) + 1) * 0.25F, 0.25F, 0.005F};
		float scale = Math.min((size[0] - 0.03125F) / w, (size[1] - 0.03125F) / h);
		float[] p = sca(3, vec(pos), 0.25F), q = {w * scale, h * scale, 0.005F};
		int o = orient.o;
		add(3, p, sca(3, sub(3, size, q), .5F));
		p = dadd(3, orient(o, dadd(3, p, -0.5F)), 0.5F);
		q = orient(o, q);
		ArrayList<BakedQuad> quads = model.inner();
		MicroBlockFace[] faces = MicroBlockFace.facesOf(MODEL);
		int f = orient.b.ordinal();
		int c = RGBtoBGR(lc.getColorValue());
		quads.add(colorQuad(faces[f].makeRect(p, q), c));
		quads.add(colorQuad(faces[f^1].makeRect(p, q), c));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(
		MatrixStack ms, IRenderTypeBuffer rtb, int light, int overlay, float t, long opaque
	) {
		ms.pushPose();
		transform(ms);
		float scale = ((pos >> 6 & 3) + 1) * 0.25F;
		ms.translate(scale * 0.5F, 0.125, 0.01F);
		scale = Math.min((scale - 0.03125F) / w, 0.21875F / h);
		ms.scale(scale, -scale, 1F);
		Matrix4f mat = ms.last().pose();
		@SuppressWarnings("resource")
		FontRenderer fr = Minecraft.getInstance().font;
		int c = tc.getTextColor();
		float y = h * -.5F + 1;
		for (String line : text.split("\n", 4)) {
			float x = (1 - fr.width(line)) * .5F;
			fr.drawInBatch(line, x, y, c, false, mat, rtb, false, overlay, light);
			y += fr.lineHeight;
		}
		ms.popPose();
	}

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		text = pkt.readUtf(256);
		host.onPartChange();
	}

	@Override
	public ContainerLabel createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerLabel(id, inv, this);
	}

}
