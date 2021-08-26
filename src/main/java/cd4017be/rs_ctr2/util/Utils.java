package cd4017be.rs_ctr2.util;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author CD4017BE */
public class Utils {

	/**@param quad to be colored (gets modified)
	 * @param c 0xAABBGGRR color
	 * @return quad
	 * @see #RGBtoBGR(int) */
	@OnlyIn(Dist.CLIENT)
	public static BakedQuad colorQuad(BakedQuad quad, int c) {
		int[] vert = quad.getVertices();
		int stride = vert.length >> 2;
		for (int i = 3; i < vert.length; i += stride) vert[i] = c;
		return quad;
	}

	/**@param player
	 * @param hand
	 * @return color for held item */
	public static @Nullable DyeColor heldColor(PlayerEntity player, @Nullable Hand hand) {
		if (hand == null) return null;
		ItemStack stack = player.getItemInHand(hand);
		return stack.isEmpty() ? null : DyeColor.getColor(stack);
	}

	/**@param player
	 * @param action to run only on server side
	 * @return {@link ActionResultType#sidedSuccess(client)} */
	public static ActionResultType serverAction(PlayerEntity player, Runnable action) {
		return serverAction(player.level.isClientSide, action);
	}

	/**@param client
	 * @param action to run only on server side
	 * @return {@link ActionResultType#sidedSuccess(client)} */
	public static ActionResultType serverAction(boolean client, Runnable action) {
		if (client) return ActionResultType.SUCCESS;
		action.run();
		return ActionResultType.CONSUME;
	}

	/**@param c color in 1: 0xAARRGGBB or 2: 0xAABBGGRR format
	 * @return the given color converted from format 1 to 2 or vice versa. */
	public static int RGBtoBGR(int c) {
		return c & 0xff00ff00 | c << 16 & 0xff0000 | c >> 16 & 0xff;
	}

}
