package cd4017be.rs_ctr2.render;

import static cd4017be.lib.render.model.WrappedBlockModel.MODELS;
import static cd4017be.math.Linalg.*;
import static cd4017be.math.MCConv.intBitsToVec;
import static cd4017be.math.MCConv.vecToIntBits;
import static java.lang.Float.floatToRawIntBits;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import cd4017be.lib.render.model.TileEntityModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class MicroBlockFace {

	private final BakedQuad quad;
	private final float[] u, v;
	private final int ax;

	public MicroBlockFace(BakedQuad quad) {
		this.quad = quad;
		this.ax = quad.getDirection().getAxis().ordinal();
		int[] data = quad.getVertices();
		float[][] mat = new float[3][5];
		for (int i = 0; i < 3; i++) {
			float[] row = mat[i];
			intBitsToVec(3, row, 0, data, i * 8);
			intBitsToVec(2, row, 3, data, i * 8 + 4);
			row[ax] = 1F;
		}
		solveGauss(mat, 3, 5);
		this.u = col(3, new float[3], mat, 3);
		this.v = col(3, new float[3], mat, 4);
	}

	public List<BakedQuad> addFaces(List<BakedQuad> quads, long mask, float layer) {
		int su = ax == 0 ? 4 : 1, sv = ax == 2 ? 4 : 16, s;
		while ((s = Long.numberOfTrailingZeros(mask)) < 64) {
			int i = (s | sv - 1) + 1, e;
			long strip = 1L << s;
			for (e = s + su; e < i; e += su) {
				if ((mask >>> e & 1) == 0) break;
				strip |= 1L << e;
			}
			mask &= ~strip;
			strip <<= sv;
			long not = s + sv > i && e < i ? strip << 1 | strip >>> 1 : 0L;
			while((strip & ~mask) == 0 && (not & ~mask) != 0) {
				mask &= ~strip;
				strip <<= sv;
				not <<= sv;
				e += sv;
			}
			e -= s + su;
			float[] p0 = vec(s);
			float[] size = dadd(3, vec(e), 1);
			p0[ax] += layer;
			sca(3, p0, .25F);
			sca(3, size, .25F);
			quads.add(makeRect(p0, size));
		}
		return quads;
	}

	public BakedQuad makeRect(float[] p0, float[] size) {
		float[] vec = new float[3];
		int[] data = quad.getVertices().clone();
		for (int i = 0; i < 32; i+=8) {
			intBitsToVec(3, vec, 0, data, i);
			mul(3, vec, size);
			add(3, vec, p0);
			vecToIntBits(3, vec, data, i);
			vec[ax] = 1F;
			data[i+4] = floatToRawIntBits(dot(3, vec, u));
			data[i+5] = floatToRawIntBits(dot(3, vec, v));
		}
		return new BakedQuad(
			data, quad.getTintIndex(), quad.getDirection(),
			quad.getSprite(), quad.isShade()
		);
	}

	public static float[] vec(int pos) {
		return new float[] {pos & 3, pos >> 2 & 3, pos >> 4 & 3};
	}

	private static final HashMap<BlockState, MicroBlockFace[]> MODEL_CACHE = new HashMap<>();
	static {
		TileEntityModel.registerCacheInvalidate(MODEL_CACHE::clear);
	}

	public static MicroBlockFace[] facesOf(BlockState block) {
		return MODEL_CACHE.computeIfAbsent(block, MicroBlockFace::create);
	}

	private static MicroBlockFace[] create(BlockState block) {
		Random rand = new Random();
		IBakedModel model = MODELS.getBlockModel(block);
		MicroBlockFace[] faces = new MicroBlockFace[6];
		for (Direction d : Direction.values()) {
			rand.setSeed(42L);
			List<BakedQuad> quads = model.getQuads(block, d, rand, EmptyModelData.INSTANCE);
			if (quads.isEmpty()) continue;
			faces[d.ordinal()] = new MicroBlockFace(quads.get(0));
		}
		return faces;
	}

}