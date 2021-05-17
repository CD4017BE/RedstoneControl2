package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.api.gate.GateUpdater.GATE_UPDATER;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack;

import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import cd4017be.lib.network.Sync;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.api.gate.IGate;
import cd4017be.rs_ctr2.api.gate.ports.IInventoryAccess;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class ItemIO extends CapabilityIO<IItemHandler>
implements IInventoryAccess, IGate {

	@Sync
	public int in0, in1, s0, s1;
	@Sync public boolean active;

	public ItemIO() {
		super(3);
		in0 = s0 = 0;
		in1 = s1 = 255;
	}

	@Override
	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, IInventoryAccess.TYPE_ID);
		setPort(1, pos, Direction.WEST, ISignalReceiver.TYPE_ID);
		setPort(2, pos, Direction.EAST, ISignalReceiver.TYPE_ID);
	}

	@Override
	protected IItemHandler fallback() {
		return EmptyHandler.INSTANCE;
	}

	@Override
	protected Capability<IItemHandler> capability() {
		return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Override
	public Item item() {
		return Content.item_io;
	}

	@Override
	public Object getHandler(int port) {
		switch(port) {
		case 0: return super.getHandler(port);
		case 1: return (ISignalReceiver) (v, r) -> {
			if (in0 != (in0 = v)) update();
		};
		case 2: return (ISignalReceiver) (v, r) -> {
			if (in1 != (in1 = v)) update();
		};
		default: return null;
		}
	}

	private void update() {
		if (!active) {
			active = true;
			GATE_UPDATER.add(this);
		}
	}

	@Override
	public void setHost(IGridHost host) {
		super.setHost(host);
		if (active && host != null)
			GATE_UPDATER.add(this);
	}

	@Override
	public boolean evaluate() {
		active = false;
		s0 = min(in0, in1);
		s1 = max(in0, in1) + 1;
		return false;
	}

	@Override
	public void getContent(ObjIntConsumer<ItemStack> inspector, int rec) {
		if (inv == null) accept(null);
		for (int l = min(s1, inv.getSlots()), i = max(s0, 0); i < l; i++)
			inspector.accept(inv.getStackInSlot(i), inv.getSlotLimit(i));
	}

	@Override
	public int transfer(int amount, Predicate<ItemStack> filter, UnaryOperator<ItemStack> target, int rec) {
		if (inv == null) accept(null);
		for (int l = min(s1, inv.getSlots()), i = max(s0, 0); i < l; i++) {
			//find matching item
			ItemStack stack = inv.extractItem(i, amount, true);
			int n = stack.getCount();
			if (n <= 0 || !filter.test(stack)) continue;
			//find more of it if needed
			for (int j = i + 1; j < l && n < amount; j++) {
				ItemStack stack1 = inv.extractItem(j, amount - n, true);
				if (canItemStacksStack(stack, stack1))
					n += stack1.getCount();
			}
			stack.setCount(n);
			//try transfer
			n -= target.apply(stack).getCount();
			if (n <= 0) continue;
			amount = n;
			//extract
			n -= inv.extractItem(i, n, false).getCount();
			for (int j = i + 1; j < l && n > 0; j++) {
				ItemStack stack1 = inv.getStackInSlot(j);
				if (canItemStacksStack(stack, stack1))
					n -= inv.extractItem(j, n, false).getCount();
			}
			if (n > 0) Main.LOG.fatal(
					"Missing {} of {} got duplicated, extracting from {}. Please report this bug!",
					n, stack, inv.getClass()
				);
			return amount;
		}
		return 0;
	}

	@Override
	public ItemStack insert(ItemStack stack, int rec) {
		if (inv == null) accept(null);
		int l = min(s1, inv.getSlots()), p = l;
		//fill non empty slots
		for (int i = max(s0, 0); i < l; i++) {
			if (inv.getStackInSlot(i).isEmpty())
				p = min(p, i);
			else if ((stack = inv.insertItem(i, stack, false)).isEmpty())
				return ItemStack.EMPTY;
		}
		//fill empty slots
		for(; p < l; p++) {
			if (!inv.getStackInSlot(p).isEmpty()) continue;
			if ((stack = inv.insertItem(p, stack, false)).isEmpty())
				return ItemStack.EMPTY;
		}
		return stack;
	}

	@Override
	public String toString() {
		return TooltipUtil.format("state.rs_ctr2.item_io", in0, in1);
	}

}
