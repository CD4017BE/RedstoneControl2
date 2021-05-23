package cd4017be.rs_ctr2.render;

import static cd4017be.lib.render.model.WrappedBlockModel.MODELS;
import static cd4017be.math.Linalg.*;
import static cd4017be.math.Orient.*;

import java.util.*;

import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.render.model.TileEntityModel;
import cd4017be.lib.util.Orientation;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

public class GridModels {

	private static final HashMap<Object, BakedQuad[]> CUBE_MODELS = new HashMap<>();
	public static final ResourceLocation[] PORTS;
	static {
		TileEntityModel.registerCacheInvalidate(CUBE_MODELS::clear);
		ResourceLocation obj_p = Main.rl("part/obj_p"), obj_u = Main.rl("part/obj_u");
		PORTS = new ResourceLocation[] {
			Main.rl("part/data_in"), Main.rl("part/data_out"),
			Main.rl("part/power_p"), Main.rl("part/power_u"),
			Main.rl("part/item_p"), Main.rl("part/item_u"),
			Main.rl("part/fluid_p"), Main.rl("part/fluid_u"),
			obj_p, obj_u, obj_p, obj_u, obj_p, obj_u, obj_p, obj_u
		};
	}

	private static final int[] ORIENTS = {
		Orientation.W12.o, Orientation.E12.o,
		Orientation.DS.o, Orientation.UN.o,
		Orientation.N12.o, Orientation.S12.o
	};

	public static void drawPort(JitBakedModel model, short port, boolean master, long b, long opaque) {
		opaque |= b;
		int p = IGridHost.posOfport(port);
		int q = IGridHost.posOfport(port - 0x111);
		int o = Integer.numberOfTrailingZeros(0x111 & ~port) >> 1 & 6;
		if (o >= 6 || p >= 0 && q >= 0 && (opaque >> p & opaque >> q & 1) != 0) return;
		boolean outer = p < 0 || q < 0;
		if (p < 0 || (b >> p & 1) == 0) {
			if (q < 0 || (b >> q & 1) == 0) return;
			p = q;
			o |= 1;
		}
		BakedQuad face = CUBE_MODELS.computeIfAbsent(
			PORTS[port >> 11 & 14 | (master ? 1:0)], GridModels::load
		)[3];
		if (face == null) return;
		o = ORIENTS[o];
		ArrayList<BakedQuad> quads = outer ? model.quads[orient(o, 3)] : model.inner();
		float[] vec = dadd(3, sca(3, vec(p & 3, p >> 2 & 3, p >> 4 & 3), .25F), -.375F);
		orient(inv(o), vec);
		origin(o, dadd(3, vec, .375F), 0.5F, 0.5F, 0.5F);
		quads.add(orient(o, face, vec));
	}

	public static void putCube(Object key, JitBakedModel model, long b, long opaque, int ofs, int orient) {
		BakedQuad[] faces = CUBE_MODELS.computeIfAbsent(key, GridModels::load);
		float[] v = originOf(orient, ofs);
		opaque = ~(opaque | b);
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

	private static float[] originOf(int orient, int ofs) {
		return origin(orient, sca(3, vec(ofs & 3, ofs >> 2 & 3, ofs >> 4 & 3), .25F), .5F, .5F, .5F);
	}

	private static BakedQuad[] load(Object key) {
		IBakedModel model = MODELS.getModelManager().getModel(
			key instanceof ResourceLocation ? (ResourceLocation)key
			: new ModelResourceLocation(((Item)key).getRegistryName(), "inventory")
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
