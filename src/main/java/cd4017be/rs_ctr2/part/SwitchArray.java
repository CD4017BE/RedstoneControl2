package cd4017be.rs_ctr2.part;

import static cd4017be.lib.network.Sync.ALL;
import static cd4017be.math.Linalg.dadd;
import static cd4017be.math.MCConv.blockRelVecF;
import static cd4017be.math.Orient.orient;
import static cd4017be.rs_ctr2.Content.SWITCH_FLIp;
import static cd4017be.rs_ctr2.Content.switch_array;
import static cd4017be.rs_ctr2.part.Switch.OFF;
import static cd4017be.rs_ctr2.part.Switch.ON;

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
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * @author CD4017BE */
public class SwitchArray extends ExtendablePart implements IDynamicPart {

	ISignalReceiver out = ISignalReceiver.NOP;
	@Sync(to = ALL) public byte state;

	public SwitchArray() {
		super(1);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setPort(0, pos + (pos >> 6 & 3), Direction.NORTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return switch_array;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		ActionResultType ret = super.onInteract(player, hand, hit, pos);
		if (ret != ActionResultType.PASS || player.isCrouching()) return ret;
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		Vector3d vec = hit.getLocation();
		float[] v = dadd(3, blockRelVecF(vec, hit.getBlockPos()), -0.5F);
		orient(orient.inv().o, v);
		pos = ((this.pos << 1) + (this.pos >> 5) & 6) + 1 - (int)(v[0] * 8F + 4F);
		state ^= 1 << pos;
		out.updateInput(state & 0xff);
		host.updateDisplay();
		player.level.playSound(null, vec.x, vec.y, vec.z, SWITCH_FLIp, SoundCategory.BLOCKS, 1F, 0.5F);
		return ActionResultType.SUCCESS;
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		(out = ISignalReceiver.of(handler)).updateInput(state & 0xff);
	}

	@Override
	public boolean isMaster(int port) {
		return true;
	}

	@Override
	public byte getLayer() {
		return L_INNER;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(MatrixStack ms, IRenderTypeBuffer rtb, int light, int overlay, float t, long opaque) {
		if ((bounds & ~opaque) == 0) return;
		ms.pushPose();
		transform(ms);
		ms.translate(-0.0625, 0, 0);
		IVertexBuilder vb = rtb.getBuffer(RenderType.solid());
		for (int i = pos >> 5 & 6 | 1; i >= 0; i--) {
			GridModels.draw(
				(state >> i & 1) != 0 ? ON : OFF, ms.last(),
				vb, -1, light, overlay
			);
			ms.translate(0.125, 0, 0);
		}
		ms.popPose();
	}

	@Override
	public void readSync(PacketBuffer pkt) {
		state = pkt.readByte();
	}

	@Override
	public void writeSync(PacketBuffer pkt, boolean init) {
		pkt.writeByte(state);
	}

}
