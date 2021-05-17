package cd4017be.rs_ctr2.render;

import static cd4017be.lib.text.TooltipUtil.format;
import static cd4017be.lib.text.TooltipUtil.translate;
import static java.lang.Math.max;
import static net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.*;

import com.mojang.blaze3d.matrix.MatrixStack;
import cd4017be.lib.network.Sync;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.util.VoxelShape4x4x4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** @author CD4017BE */
@OnlyIn(Dist.CLIENT)
public class SignalProbeRenderer extends ItemStackTileEntityRenderer {

	private static final Quaternion[] ROTATIONS = new Quaternion[6];
	static {
		Quaternion q = new Quaternion(90, 0, 0, true);
		for (Direction d : Direction.values())
			(ROTATIONS[d.ordinal() & 1 | d.getAxis().ordinal() << 1] = d.getRotation()).mul(q);
	}
	private final VoxelShape4x4x4 bounds;
	private final JitBakedModel model;

	public SignalProbeRenderer() {
		this.bounds = new VoxelShape4x4x4();
		this.model = new JitBakedModel(0);
	}

	public static ResourceLocation baseModel(Item item) {
		ResourceLocation model = item.getRegistryName();
		return new ResourceLocation(model.getNamespace(), "item/" + model.getPath() + "_base");
	}

	@Override
	public void renderByItem(
		ItemStack stack, TransformType transform, MatrixStack mat,
		IRenderTypeBuffer vb, int light, int overlay
	) {
		Minecraft mc = Minecraft.getInstance();
		mat.translate(0.5, 0.5, 0.5);
		IBakedModel model = mc.getModelManager().getModel(baseModel(stack.getItem()));
		mc.getItemRenderer().render(stack, NONE, false, mat, vb, light, overlay, model);
		if (transform != FIRST_PERSON_LEFT_HAND
			&& transform != FIRST_PERSON_RIGHT_HAND
			&& transform != THIRD_PERSON_LEFT_HAND
			&& transform != THIRD_PERSON_RIGHT_HAND
		) return;
		CompoundNBT nbt = stack.getTagElement("part");
		GridPart part = nbt == null ? null : GridPart.load(null, nbt, Sync.SAVE);
		drawDisplay(part, mat, vb, 0xf0, overlay);
	}

	private void drawDisplay(
		GridPart part, MatrixStack mat,
		IRenderTypeBuffer rtb, int light, int overlay
	) {
		mat.pushPose();
		mat.translate(-0.1875, 0.4375, 0.032);
		mat.scale(1F/256F, -1F/256F, 1F/256F);
		Printer p = new Printer(rtb, mat, light).color(0xffffff00);
		mat.popPose();
		if (part == null) {
			p.row(5).col(2).print(translate("state.rs_ctr2.no_connection"));
			return;
		}
		drawPart(part, mat, rtb, light, overlay);
		p.row(5).col(64).print(part.asItemStack().getHoverName(), 96);
		p.row(48).col(64).color(-1).print(format("state.rs_ctr2.ports", part.ports.length));
		p.row(64);
		String info = part.toString();
		for (String s : info.split("\n"))
			p.col(2).print(s).nl();
	}

	private void drawPart(GridPart part, MatrixStack mat, IRenderTypeBuffer rtb, int light, int overlay) {
		mat.pushPose();
		mat.translate(-0.0625, 0.3125, 0.0625);
		Minecraft mc = Minecraft.getInstance();
		mat.mulPose(new Quaternion(mc.player.xRot, mc.player.yRot + 180, 0, true));
		bounds.grid = part.bounds;
		int x0 = bounds.firstFull(Axis.X), y0 = bounds.firstFull(Axis.Y), z0 = bounds.firstFull(Axis.Z);
		int x1 = bounds.lastFull(Axis.X), y1 = bounds.lastFull(Axis.Y), z1 = bounds.lastFull(Axis.Z);
		float scale = .5F / max(max(x1-x0, y1-y0), z1-z0);
		mat.scale(scale, scale, scale);
		mat.translate((x0+x1) * -.125, (y0+y1) * -.125, (z0+z1) * -.125);
		part.fillModel(model.clear(), part.getLayer() >= 0 ? part.bounds : 0L);
		model.render(rtb.getBuffer(RenderType.solid()), mat.last(), light, overlay);
		mat.scale(scale = 1F / 32F, scale, scale);
		FontRenderer fr = mc.font;
		for (int i = 0; i < part.ports.length; i++) {
			short port = part.ports[i];
			mat.pushPose();
			mat.translate(port << 2 & 60, port >> 2 & 60, port >> 6 & 60);
			int ax = (Integer.numberOfTrailingZeros(~port & 0x111) >> 1);
			int p = IGridHost.posOfport(port - 0x111);
			if (p >= 0) ax |= part.bounds >> p & 1;
			mat.mulPose(ROTATIONS[ax % 6]);
			mat.translate(0, 0, -.5);
			String s = Integer.toString(i);
			fr.drawInBatch(s, (fr.width(s)-1) * -.5F, -3.5F, -1, false, mat.last().pose(), rtb, false, 0, light);
			mat.popPose();
		}
		mat.popPose();
	}

}
