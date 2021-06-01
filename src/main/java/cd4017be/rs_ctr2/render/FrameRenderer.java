package cd4017be.rs_ctr2.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import cd4017be.rs_ctr2.tileentity.FrameController;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * @author CD4017BE */
@OnlyIn(Dist.CLIENT)
public class FrameRenderer extends TileEntityRenderer<FrameController> {

	private static final byte[] EDGES = {
		0x01, 0x23, 0x45, 0x67,
		0x02, 0x13, 0x46, 0x57,
		0x04, 0x15, 0x26, 0x37
	};

	public FrameRenderer(TileEntityRendererDispatcher disp) {
		super(disp);
	}

	@Override
	public void render(
		FrameController te, float t, MatrixStack ms,
		IRenderTypeBuffer rtb, int overlay, int light
	) {
		IVertexBuilder vb = rtb.getBuffer(RenderType.lines());
		Matrix4f mat = ms.last().pose();
		BlockPos pos = te.pos();
		if (te.visible) {
			int[] b = te.region.clone();
			for (int i = 0; i < 3; i++) {
				b[i  ] -= pos.getX();
				b[i+3] -= pos.getY();
				b[i+6] -= pos.getZ();
				b[i*3+2]++;
			}
			cube(mat, vb, 0xffbfbf00, te.missing == 0 ? 0xff00ff00 : 0xff0000ff, b);
		}
		if (te.block != null) {
			pos = te.block.left.subtract(pos);
			int[] b = new int[9];
			b[2] = (b[1] = b[0] = pos.getX()) + 1;
			b[5] = (b[4] = b[3] = pos.getY()) + 1;
			b[8] = (b[7] = b[6] = pos.getZ()) + 1;
			cube(mat, vb, 0x7f7f007f, 0xff7f007f, b);
		}
	}

	private static void cube(Matrix4f mat, IVertexBuilder vb, int c0, int c1, int[] b) {
		line(mat, vb, c0, 0.5F, 0.5F, 0.5F, b[1] + 0.5F, b[4] + 0.5F, b[7] + 0.5F);
		for (byte i : EDGES)
			line(mat, vb, c1,
				b[i << 1 & 2], b[(i      & 2) + 3], b[(i >> 1 & 2) + 6],
				b[i >> 3 & 2], b[(i >> 4 & 2) + 3], b[(i >> 5 & 2) + 6]
			);
	}

	private static void line(
		Matrix4f mat, IVertexBuilder vb, int c,
		float x0, float y0, float z0,
		float x1, float y1, float z1
	) {
		int r = c & 0xff, g = c >> 8 & 0xff, b = c >> 16 & 0xff, a = c >> 24 & 0xff;
		vb.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
		vb.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
	}

	@Override
	public boolean shouldRenderOffScreen(FrameController te) {
		return true;
	}

}
