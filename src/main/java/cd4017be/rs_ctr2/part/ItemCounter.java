package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.item_counter;

import java.util.function.ObjIntConsumer;

import cd4017be.api.grid.port.IInventoryAccess;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.network.Sync;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;


/**
 * @author CD4017BE */
public class ItemCounter extends OrientedPart
implements ISignalReceiver, ObjIntConsumer<ItemStack>, IProbeInfo {

	ISignalReceiver out = ISignalReceiver.NOP;
	IInventoryAccess inv = IInventoryAccess.NOP;
	@Sync public int clk, state;
	@Sync public boolean empty;

	public ItemCounter() {
		super(3);
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.NORTH, ISignalReceiver.TYPE_ID);
		setPort(1, pos, Direction.SOUTH, IInventoryAccess.TYPE_ID);
		setPort(2, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return item_counter;
	}

	@Override
	public Object getHandler(int port) {
		return port == 2 ? this : null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port == 0) (out = ISignalReceiver.of(handler)).updateInput(state);
		else if (port == 1) inv = IInventoryAccess.of(handler);
	}

	@Override
	public boolean isMaster(int port) {
		return port < 2;
	}

	@Override
	public void updateInput(int value, int rec) {
		if (--rec < 0 || (~clk & (clk = value)) == 0) return;
		value = state; state = 0;
		inv.getContent(this);
		if (state != value) out.updateInput(state, rec);
	}

	@Override
	public void accept(ItemStack stack, int value) {
		value = stack.getCount();
		state += empty ? (value == 0 ? 1 : 0) : value;
	}

	@Override
	public ActionResultType
	onInteract(PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos) {
		if (hand == null || player.isCrouching())
			return super.onInteract(player, hand, hit, pos);
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		player.displayClientMessage(new TranslationTextComponent(
			(empty = !empty) ? "msg.rs_ctr2.count_slots" : "msg.rs_ctr2.count_items"
		), true);
		return ActionResultType.SUCCESS;
	}

	@Override
	public Object[] stateInfo() {
		return new Object[] {
			empty ? "state.rs_ctr2.slot_counter" : "state.rs_ctr2.item_counter",
			state, inv != IInventoryAccess.NOP, clk
		};
	}

}
