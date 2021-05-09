package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.lib.network.Sync.Type.Enum;

import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.render.GridModels;
import cd4017be.lib.network.Sync;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.util.Orientation;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public abstract class OrientedPart extends GridPart {

	@Sync(to=ALL, type=Enum)
	public Orientation orient;
	@Sync(to=ALL)
	public byte pos;

	public OrientedPart(int ports) {
		super(ports);
	}

	public void set(int pos, Orientation orient) {
		this.pos = (byte)pos;
		this.orient = orient;
	}

	protected void setPort(int i, int pos, Direction dir, int type) {
		ports[i] = port(pos(pos, orient), orient.apply(dir), type);
	}

	protected void setBounds(int p0, int p1) {
		bounds = bounds(pos(p0, orient), pos(p1, orient));
	}

	public static int pos(int pos, Orientation orient) {
		int o = orient.o;
		pos ^= (o & 1 | o >> 2 & 4 | o >> 4 & 16) * 3;
		return (pos & 3) << (o & 6)
		| (pos >> 2 & 3) << (o >> 4 & 6)
		| (pos >> 4 & 3) << (o >> 8 & 6);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		set(pos, orient);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillModel(JitBakedModel model, long opaque) {
		GridModels.putCube(item(), model, bounds, opaque, pos, orient.o);
	}

}
