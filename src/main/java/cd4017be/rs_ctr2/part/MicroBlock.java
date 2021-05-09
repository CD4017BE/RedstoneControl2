package cd4017be.rs_ctr2.part;

import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.render.MicroBlockFace;
import cd4017be.lib.network.Sync;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.EmptyBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MicroBlock extends GridPart {

	public BlockState block;
	private CompoundNBT tag;

	public MicroBlock() {
		super(0);
	}

	public MicroBlock(BlockState block, CompoundNBT tag, long bounds) {
		this();
		this.block = block;
		this.tag = tag;
		this.bounds = bounds;
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		nbt.putLong("m", bounds);
		nbt.putInt("s", Block.getId(block));
		if ((mode & Sync.SAVE) != 0)
			nbt.put("t", tag);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		bounds = nbt.getLong("m");
		block = Block.stateById(nbt.getInt("s"));
		tag = nbt.getCompound("t");
	}

	@Override
	public Object getHandler(int port) {return null;}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int channel) {
		return false;
	}

	@Override
	public Item item() {
		return Content.microblock;
	}

	@Override
	public ItemStack asItemStack() {
		ItemStack stack = super.asItemStack();
		stack.setTag(tag);
		stack.setCount(Long.bitCount(bounds));
		return stack;
	}

	@Override
	public byte getLayer() {
		return L_OUTER;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand != null) return ActionResultType.PASS;
		if (!player.level.isClientSide && player.getMainHandItem().getItem() instanceof IGridItem) {
			long b = bounds & ~(1L << pos);
			if (b == 0) {
				IGridHost host = this.host;
				host.removePart(this);
				host.removeIfEmpty();
			} else setBounds(b);
			if (!player.isCreative()) {
				ItemStack stack = new ItemStack(item());
				stack.setTag(tag);
				ItemFluidUtil.dropStack(stack, player);
			}
		}
		return ActionResultType.CONSUME;
	}

	public boolean addVoxel(int pos) {
		if (host.getPart(pos, L_OUTER) != null) return false;
		setBounds(bounds | 1L << pos);
		return true;
	}

	@Override
	public int analogOutput(Direction side) {
		return block.getSignal(EmptyBlockReader.INSTANCE, BlockPos.ZERO, side);
	}

	@Override
	public boolean connectRedstone(Direction side) {
		return block.canConnectRedstone(EmptyBlockReader.INSTANCE, BlockPos.ZERO, side);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillModel(JitBakedModel model, long opaque) {
		MicroBlockFace.drawVoxels(model, block, bounds, opaque);
	}

}
