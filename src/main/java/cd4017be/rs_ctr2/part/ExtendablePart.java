package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.IGridHost;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;


/**
 * @author CD4017BE */
public abstract class ExtendablePart extends OrientedPart {

	public ExtendablePart(int ports) {
		super(ports);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos + (pos >> 6 & 3));
	}

	@Override
	public ItemStack asItemStack() {
		return new ItemStack(item(), (pos >> 6 & 3) + 1);
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		ItemStack stack;
		if (hand == null || (stack = player.getItemInHand(hand)).getItem() != item())
			return super.onInteract(player, hand, hit, pos);
		Direction d = hit.getDirection(), r = orient.r;
		if (d != r && d != r.getOpposite()) return ActionResultType.PASS;
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		pos = this.pos;
		if (d == r) {
			if ((pos & 3) + (pos >> 6 & 3) >= 3) return ActionResultType.FAIL;
			pos += 64;
		} else {
			if ((pos & 3) == 0) return ActionResultType.FAIL;
			pos += 63;
		}
		if (!player.isCreative()) stack.shrink(1);
		IGridHost host = this.host;
		host.removePart(this);
		set(pos, orient);
		host.addPart(this);
		return ActionResultType.SUCCESS;
	}

	@Override
	protected ResourceLocation model() {
		ResourceLocation res = item().getRegistryName();
		return new ResourceLocation(res.getNamespace(), "part/" + res.getPath() + (pos >> 6 & 3));
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerModels(Item item) {
		ResourceLocation res = item.getRegistryName();
		for (int i = 0; i < 4; i++)
			ModelLoader.addSpecialModel(new ResourceLocation(
				res.getNamespace(), "part/" + res.getPath() + i
			));
	}

}
