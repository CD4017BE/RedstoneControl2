package cd4017be.rs_ctr2.render;

import static cd4017be.api.grid.IGridHost.posOfport;
import static cd4017be.lib.text.TooltipUtil.format;
import static cd4017be.lib.text.TooltipUtil.translate;
import static java.lang.Math.max;
import static net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.*;

import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.api.grid.GridPart;
import cd4017be.api.grid.IGridHost;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.util.VoxelShape4x4x4;
import cd4017be.rs_ctr2.item.SignalProbeItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Constants.NBT;

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
		mat.pushPose();
		mat.translate(-0.1875, 0.4375, 0.032);
		mat.scale(1F/256F, -1F/256F, 1F/256F);
		Printer p = new Printer(vb, mat, 0xf0).color(0xffffff00);
		mat.popPose();
		drawDisplay(stack.getOrCreateTag(), p, mat, vb, 0xf0, overlay);
	}

	private void drawDisplay(
		CompoundNBT nbt, Printer pr, MatrixStack mat,
		IRenderTypeBuffer rtb, int light, int overlay
	) {
		int[] ports = SignalProbeItem.readPorts(nbt);
		if (ports == null) {
			pr.row(5).col(2).print(translate("state.rs_ctr2.no_connection"));
			return;
		}
		mat.pushPose();
		mat.scale(1, 1, 0.5F);
		mat.translate(-0.0625, 0.3125, 0.125);
		Minecraft mc = Minecraft.getInstance();
		mat.mulPose(new Quaternion(mc.player.xRot, mc.player.yRot + 180, 0, true));
		pr.row(5).col(64).print(drawBlock(
			mat, mc, BlockPos.of(nbt.getLong("bp")),
			nbt.getByte("gp"), rtb, light, overlay
		).getHoverName(), 96);
		long b = bounds.grid;
		mat.scale(1F/32F, 1F/32F, 1F/32F);
		FontRenderer fr = mc.font;
		int n = 0;
		for (int i = 0; i < ports.length; i++) {
			int port = ports[i];
			if (port == 0) continue;
			n++;
			mat.pushPose();
			mat.translate(port << 2 & 60, port >> 2 & 60, port >> 6 & 60);
			int ax = (Integer.numberOfTrailingZeros(~port & 0x111) >> 1);
			int p = posOfport(port - 0x111);
			if (p >= 0 && ((b >> p & 1) != 0 || posOfport(port) < 0)) ax |= 1;
			mat.mulPose(ROTATIONS[ax % 6]);
			mat.translate(0, 0, -.5);
			String s = Integer.toString(i);
			fr.drawInBatch(s, (fr.width(s)-1) * -.5F, -3.5F, port << 1 < 0 ? 0xffffff80 : -1, false, mat.last().pose(), rtb, false, 0, light);
			mat.popPose();
		}
		mat.popPose();
		pr.row(48).col(64).color(-1).print(format("state.rs_ctr2.ports", n));
		pr.row(64);
		for (String s : SignalProbeItem.readInfo(nbt).split("\n"))
			pr.col(2).print(s).nl();
		if (!mc.options.renderDebug || !nbt.contains("ports", NBT.TAG_LONG_ARRAY)) return;
		long[] arr = nbt.getLongArray("ports");
		pr.nl().col(2).print(translate("state.rs_ctr2.links"));
		long used = 0;
		for (int i = 0; i < arr.length; i++) {
			long x = arr[i];
			if (x == 0) continue;
			String s;
			boolean nl = true;
			switch((int)(x >> 60) & 3) {
			case 0:
				s = format("state.rs_ctr2.link0", i, x >> 48 & 0xff);
				used |= 1L << i;
				break;
			case 1:
				s = format("state.rs_ctr2.link1", i, (int)x, x >> 48 & 0xff);
				used |= 1L << i;
				break;
			case 2:
				int j = (int)x >> 16 & 0xff;
				if (j > i || (used >> j & 1) != 0) continue;
				s = format("state.rs_ctr2.link2", i, j);
				break;
			default:
				s = format("state.rs_ctr2.link3", i, (int)x);
				if (pr.x < 75) {
					pr.col(80);
					nl = false;
				}
			}
			if (nl) pr.nl().col(2);
			pr.color(x << 1 < 0 ? 0xffffff80 : 0xffc0c0c0).print(s);
		}
	}

	private ItemStack drawBlock(
		MatrixStack mat, Minecraft mc, BlockPos pos, byte gp,
		IRenderTypeBuffer rtb, int light, int overlay
	) {
		if (gp < 0) {
			mat.scale(.125F, .125F, .125F);
			mat.translate(-.5, -.5, -.5);
			BlockState state = mc.level.getBlockState(pos);
			mat.pushPose();
			IModelData data = ModelDataManager.getModelData(mc.level, pos);
			if (data == null) data = EmptyModelData.INSTANCE;
			mc.getBlockRenderer().renderBlock(state, mat, rtb, light, overlay, data);
			mat.popPose();
			bounds.grid = System.currentTimeMillis() << 54 >> 63;
			return state.getPickBlock(mc.hitResult, mc.level, pos, mc.player);
		}
		TileEntity te = mc.level.getBlockEntity(pos);
		if (!(te instanceof IGridHost)) return ItemStack.EMPTY;
		long m = 1L << gp;
		GridPart part = ((IGridHost)te).findPart(p -> (p.bounds & m) != 0 && p.ports.length != 0);
		if (part == null) return ItemStack.EMPTY;
		bounds.grid = part.bounds;
		int x0 = bounds.firstFull(Axis.X), y0 = bounds.firstFull(Axis.Y), z0 = bounds.firstFull(Axis.Z);
		int x1 = bounds.lastFull(Axis.X), y1 = bounds.lastFull(Axis.Y), z1 = bounds.lastFull(Axis.Z);
		float scale = .5F / max(max(x1-x0, y1-y0), z1-z0);
		mat.scale(scale, scale, scale);
		mat.translate((x0+x1) * -.125, (y0+y1) * -.125, (z0+z1) * -.125);
		part.fillModel(model.clear(), part.getLayer() >= 0 ? part.bounds : 0L);
		model.render(rtb.getBuffer(RenderType.solid()), mat.last(), light, overlay);
		return part.asItemStack();
	}

}
