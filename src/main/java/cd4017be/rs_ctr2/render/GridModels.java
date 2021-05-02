package cd4017be.rs_ctr2.render;

import static cd4017be.lib.render.model.WrappedBlockModel.MODELS;
import static cd4017be.math.Linalg.sca;
import static cd4017be.math.Linalg.vec;
import static cd4017be.math.Orient.orient;
import static cd4017be.math.Orient.origin;

import java.util.*;

import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.render.model.TileEntityModel;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;

public class GridModels {

	private static final HashMap<Item, BakedQuad[]> CUBE_MODELS = new HashMap<>();
	static {
		TileEntityModel.registerCacheInvalidate(CUBE_MODELS::clear);
	}

	public static void putCube(GridPart part, JitBakedModel model, long opaque, int ofs, int orient) {
		BakedQuad[] faces = CUBE_MODELS.computeIfAbsent(part.item(), GridModels::load);
		float[] v = origin(orient, sca(3, vec(ofs & 3, ofs >> 2 & 3, ofs >> 4 & 3), .25F), .5F, .5F, .5F);
		long b = part.bounds; opaque = ~(opaque | b);
		for (int i = 0; i < 6; i++) {
			BakedQuad quad = faces[i];
			if (quad == null) continue;
			int j = orient(orient, i);
			ArrayList<BakedQuad> quads;
			if ((b & GridPart.FACES[j]) != 0) quads = model.quads[j];
			else {
				int s = GridPart.step(j);
				if ((((j & 1) != 0 ? b << s : b >>> s) & opaque) == 0) continue;
				quads = model.inner();
			}
			quads.add(orient(orient, quad, v));
		}
	}

	private static BakedQuad[] load(Item key) {
		IBakedModel model = MODELS.getModelManager().getModel(
			new ModelResourceLocation(key.getRegistryName(), "inventory")
		);
		BakedQuad[] quads = new BakedQuad[6];
		Random rand = new Random(42);
		//first take inner quads according to orientation
		for (BakedQuad quad : model.getQuads(null, null, rand, EmptyModelData.INSTANCE))
			quads[quad.getDirection().ordinal()] = quad;
		//but if a side has a cull face quad, take that instead
		for (int i = 0; i < 6; i++) {
			List<BakedQuad> list = model.getQuads(null, Direction.from3DDataValue(i), rand, EmptyModelData.INSTANCE);
			if (!list.isEmpty()) quads[i] = list.get(0);
		}
		return quads;
	}

}
