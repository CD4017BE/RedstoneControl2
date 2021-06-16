package cd4017be.rs_ctr2.part;

import cd4017be.api.grid.IGridHost;
import cd4017be.api.grid.IGridItem;
import cd4017be.api.grid.Link;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.item.WirelessItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;


/**
 * @author CD4017BE */
public abstract class Wireless extends OrientedPart {

	private final WirelessItem item;
	@Sync public int link;

	public Wireless(WirelessItem item) {
		super(1);
		this.item = item;
	}

	@Override
	public Item item() {
		return item;
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, item.type);
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public boolean isMaster(int port) {
		return false;
	}

	@Override
	public void setHost(IGridHost host) {
		super.setHost(host);
		if (host != null) Link.load(this, 1, link);
		else Link.unload(this, 1, link);
	}

	@Override
	public ItemStack asItemStack() {
		ItemStack stack = super.asItemStack();
		stack.getOrCreateTag().putInt("link", link);
		return stack;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		ItemStack stack = player.getMainHandItem();
		if (hand != null) return ActionResultType.PASS;
		if (!player.level.isClientSide && stack.getItem() instanceof IGridItem) {
			IGridHost host = this.host;
			host.removePart(this);
			host.removeIfEmpty();
			if (stack.getItem() == item.pair && WirelessItem.hasLink(stack)) {
				stack.shrink(1);
				ItemFluidUtil.dropStack(super.asItemStack(), player);
			} else ItemFluidUtil.dropStack(asItemStack(), player);
		}
		return ActionResultType.CONSUME;
	}

}
