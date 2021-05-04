package cd4017be.rs_ctr2.item;

import static cd4017be.rs_ctr2.api.grid.GridPart.L_OUTER;
import static net.minecraft.util.math.vector.Vector3d.upFromBottomCenterOf;

import java.util.ArrayList;

import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.part.MicroBlock;
import cd4017be.rs_ctr2.render.MicroBlockFace;
import cd4017be.lib.item.DocumentedItem;
import cd4017be.lib.render.model.*;
import cd4017be.lib.text.TooltipUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataMap;


public class MicroBlockItem extends DocumentedItem implements IGridItem, IModelDataItem {

	public MicroBlockItem(Properties p) {
		super(p);
	}

	@Override
	public GridPart createPart() {
		return new MicroBlock();
	}

	private ItemStack of(Block block) {
		return wrap(new ItemStack(block), block.defaultBlockState(), 1);
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
			items.add(of(Blocks.STONE));
			items.add(of(Blocks.OAK_PLANKS));
			items.add(of(Blocks.REDSTONE_BLOCK));
		}
	}

	@Override
	public ITextComponent getName(ItemStack stack) {
		ItemStack cont = ItemStack.of(stack.getOrCreateTag());
		return TooltipUtil.cFormat(getDescriptionId(), cont.getHoverName().getString());
	}

	@Override
	public ActionResultType onInteract(
		IGridHost grid, ItemStack stack, PlayerEntity player,
		Hand hand, BlockRayTraceResult hit
	) {
		if (hand == null) return ActionResultType.PASS;
		int pos = IGridHost.target(hit, false);
		if (pos < 0 || grid.getPart(pos, L_OUTER) != null)
			pos = IGridHost.target(hit, true);
		if (pos < 0) return ActionResultType.PASS;
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		
		CompoundNBT tag = stack.getOrCreateTag();
		BlockState block = getBlock(new BlockItemUseContext(
			player, hand, ItemStack.of(tag), hit
		));
		if (block == null) return ActionResultType.FAIL;
		MicroBlock part = (MicroBlock)grid.findPart(
			p -> p instanceof MicroBlock && ((MicroBlock)p).block == block
		);
		if (!(
			part != null ? part.addVoxel(pos)
			: grid.addPart(new MicroBlock(block, tag, 1L << pos))
		)) return ActionResultType.FAIL;
		if (!player.isCreative()) stack.shrink(1);
		return ActionResultType.SUCCESS;
	}

	public ActionResultType useOn(ItemUseContext context) {
		return placeAndInteract(context);
	}

	public ItemStack convert(ItemStack stack, World world, BlockPos pos) {
		BlockState state = getBlock(new BlockItemUseContext(
			world, null, Hand.MAIN_HAND, stack,
			new BlockRayTraceResult(
				upFromBottomCenterOf(pos, 1),
				Direction.UP, pos, false
			)
		));
		return state != null ? wrap(stack, state, 64) : ItemStack.EMPTY;
	}

	@Override
	public boolean canAttackBlock(
		BlockState state, World world, BlockPos pos, PlayerEntity player
	) {
		if (player.isCreative())
			world.getBlockState(pos).attack(world, pos, player);
		return false;
	}

	public static BlockState getBlock(BlockItemUseContext context) {
		ItemStack stack = context.getItemInHand();
		if (!(stack.getItem() instanceof BlockItem)) return null;
		Block block = ((BlockItem)stack.getItem()).getBlock();
		BlockState state = block.getStateForPlacement(context);
		return state.isCollisionShapeFullBlock(EmptyBlockReader.INSTANCE, BlockPos.ZERO)
			? state : null;
	}

	public ItemStack wrap(ItemStack stack, BlockState state, int n) {
		ItemStack ret = new ItemStack(this, n);
		CompoundNBT nbt = stack.save(ret.getOrCreateTag());
		nbt.putByte("Count", (byte)1);
		nbt.putInt("state", Block.getId(state));
		return ret;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ModelDataMap getModelData(ItemStack stack, ClientWorld world, LivingEntity entity) {
		BlockState state = Block.stateById(stack.getOrCreateTag().getInt("state"));
		if (state.getBlock() == Blocks.AIR) //so the item is not invisible in recipes
			state = Blocks.STONE.defaultBlockState();
		ModelDataMap data = TileEntityModel.MODEL_DATA_BUILDER.build();
		ArrayList<BakedQuad> quads = JitBakedModel.make(data).inner();
		float[] ofs = {.25F, .25F, .25F}, size = {.5F, .5F, .5F};
		for (MicroBlockFace f : MicroBlockFace.facesOf(state))
			if (f != null) quads.add(f.makeRect(ofs, size));
		return data;
	}

}
