package cd4017be.rs_ctr2.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

/**@author CD4017BE */
public class Printer {
	public final FontRenderer fr;
	private final IRenderTypeBuffer rtb;
	private final Matrix4f mat;
	private final int light;
	public int x, y, color = -1;

	@SuppressWarnings("resource")
	public Printer(IRenderTypeBuffer rtb, MatrixStack mat, int light) {
		this.fr = Minecraft.getInstance().font;
		this.rtb = rtb;
		this.mat = mat.last().pose();
		this.light = light;
	}

	public Printer col(int x) {
		this.x = x;
		return this;
	}

	public Printer row(int y) {
		this.y = y;
		return this;
	}

	public Printer color(int c) {
		color = c;
		return this;
	}

	public Printer nl() {
		y += fr.lineHeight;
		x = 0;
		return this;
	}

	public Printer print(String s) {
		x += fr.drawInBatch(s, x, y, color, false, mat, rtb, false, 0, light);
		return this;
	}

	public void print(ITextComponent t, int w) {
		for (IReorderingProcessor rop : fr.split(t, w)) {
			fr.drawInBatch(rop, x, y, color, false, mat, rtb, false, 0, light);
			y += fr.lineHeight;
		}
	}
}