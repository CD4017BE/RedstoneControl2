package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Main.SERVER_CFG;
import static cd4017be.rs_ctr2.api.gate.GateUpdater.GATE_UPDATER;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.lib.network.Sync;
import cd4017be.lib.render.model.JitBakedModel;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.api.gate.IGate;
import cd4017be.rs_ctr2.api.gate.ports.IEnergyAccess;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.render.GridModels;
import cd4017be.rs_ctr2.render.MicroBlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class Battery extends GridPart implements IGate, IEnergyAccess {

	ISignalReceiver out = ISignalReceiver.NOP;

	@Sync public int energy, state;
	@Sync public boolean active;
	int cap;

	public Battery() {
		super(0);
	}

	public Battery(int pos) {
		this();
		bounds = 1L << pos;
		cap = SERVER_CFG.battery_cap.get();
	}

	@Override
	public Item item() {
		return Content.battery;
	}

	@Override
	public ItemStack asItemStack() {
		return new ItemStack(item(), Long.bitCount(bounds));
	}

	@Override
	public ActionResultType onInteract(
		PlayerEntity player, Hand hand, BlockRayTraceResult hit, int pos
	) {
		boolean client = player.level.isClientSide;
		if (hand == null) {
			if (client || !(player.getMainHandItem().getItem() instanceof IGridItem))
				return ActionResultType.CONSUME;
			removeVoxel(pos);
			if (!player.isCreative())
				ItemFluidUtil.dropStack(super.asItemStack(), player);
			return ActionResultType.CONSUME;
		}
		//right click interact
		Item item = player.getItemInHand(hand).getItem();
		short port = port(pos, hit.getDirection(), 0);
		int p = findPort(port);
		if (item == Items.AIR) {
			if (client) return ActionResultType.CONSUME;
			if (!player.isShiftKeyDown())
				player.displayClientMessage(new TranslationTextComponent(
					"msg.rs_ctr2.battery", energy, cap, Long.bitCount(bounds)
				), true);
			else if (p >= 0) {
				//remove port
				IGridHost host = this.host;
				host.removePart(this);
				ports = ArrayUtils.remove(ports, p);
				host.addPart(this);
			}
		} else if (item == Content.power_cable) {
			if (p >= 0) return ActionResultType.PASS;
			if (client) return ActionResultType.CONSUME;
			//add power port
			IGridHost host = this.host;
			host.removePart(this);
			ports = ArrayUtils.add(ports, (short)(port | IEnergyAccess.TYPE_ID << 12));
			host.addPart(this);
		} else if (item == Content.data_cable) {
			if (p >= 0) return ActionResultType.PASS;
			if (client) return ActionResultType.CONSUME;
			// add or change data port
			IGridHost host = this.host;
			host.removePart(this);
			if (ports.length > 0 && (ports[0] & 0xf000) == 0) ports[0] = port;
			else ports = ArrayUtils.insert(0, ports, port);
			host.addPart(this);
		} else return ActionResultType.PASS;
		return ActionResultType.SUCCESS;
	}

	private int findPort(int port) {
		port &= 0xfff;
		for (int i = 0; i < ports.length; i++)
			if ((ports[i] & 0xfff) == port) return i;
		return -1;
	}

	private static int portValid(short port, long b) {
		int p = IGridHost.posOfport(port);
		int q = IGridHost.posOfport(port - 0x111);
		return ~p >>> 31 & (int)(b >>> p)
			^ ~q >>> 31 & (int)(b >>> q);
	}

	@Override
	public void setBounds(long b) {
		cap = Long.bitCount(b) * SERVER_CFG.battery_cap.get();
		energy = min(energy, cap);
		//check ports
		long valid = 0;
		for (int i = 0; i < ports.length; i++)
			valid |= (long)portValid(ports[i], b) << i;
		int n = Long.bitCount(valid);
		IGridHost host = this.host;
		if (n >= ports.length) {
			//all ports valid, just change bounds
			bounds = b;
			if (host == null) return;
			host.updateBounds();
			host.onPartChange();
			return;
		}
		//remove invalid ports
		if (host != null) host.removePart(this);
		short[] arr = new short[n];
		for (int i = 0, j = 0; valid != 0; i++, valid >>>= 1)
			if ((valid & 1) != 0) arr[j++] = ports[i];
		ports = arr;
		bounds = b;
		if (host != null) host.addPart(this);
	}

	public void removeVoxel(int pos) {
		long b = bounds & ~(1L << pos);
		IGridHost host = this.host;
		if (b == 0) {// completely removed
			host.removePart(this);
			host.removeIfEmpty();
			return;
		}
		long b1 = floodFill(b, Long.lowestOneBit(b));
		if (b1 == b) {// still in one piece
			setBounds(b);
			return;
		}
		host.removePart(this);
		do {
			//split off piece
			Battery part = new Battery();
			part.energy = (int)((long)energy * Long.bitCount(b1) / Long.bitCount(b));
			energy -= part.energy;
			part.ports = ports;
			part.setBounds(b1);
			host.addPart(part);
			b &= ~b1;
			b1 = floodFill(b, Long.lowestOneBit(b));
		} while(b1 != b);
		setBounds(b);
		host.addPart(this);
	}

	public boolean addVoxel(int pos) {
		if (host.getPart(pos, L_FULL) != null) return false;
		long b = bounds | 1L << pos;
		long o = outline(b) & ~b;
		IGridHost host = this.host;
		for (Battery part; (part = (Battery)host.findPart(
			p -> p instanceof Battery && (p.bounds & o) != 0
		)) != null;) { //merge with touching other battery
			host.removePart(part);
			b |= part.bounds;
			energy += part.energy;
			short[] p = part.ports;
			int i0 = p.length > 0 && (p[0] & 0xf000) == 0 ? 1 : 0;
			if (p.length <= i0) continue;
			// add other's ports
			host.removePart(this);
			int l = ports.length;
			ports = Arrays.copyOf(ports, l + p.length - i0);
			System.arraycopy(p, i0, ports, l, p.length - i0);
		}
		setBounds(b);
		host.addPart(this);
		return true;
	}

	@Override
	public Object getHandler(int port) {
		return this;
	}

	@Override
	public void setHandler(int port, Object handler) {
		(out = ISignalReceiver.of(handler)).updateInput(state);
	}

	@Override
	public boolean isMaster(int channel) {
		return channel == 0 && ports.length > 0
		&& (ports[0] & 0xf000) == 0;
	}

	@Override
	public int transferEnergy(int amount, boolean test, int rec) {
		if (amount == 0) return 0;
		int e = max(min(energy + amount, cap), 0);
		amount = e - energy;
		if (!test) {
			energy = e;
			if (!active) {
				active = true;
				GATE_UPDATER.add(this);
			}
		}
		return amount;
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null) return false;
		state = energy;
		return true;
	}

	@Override
	public void latchOut() {
		out.updateInput(state);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		super.loadState(nbt, mode);
		bounds = nbt.getLong("b");
		cap = Long.bitCount(bounds) * SERVER_CFG.battery_cap.get();
		int[] arr = nbt.getIntArray("p");
		ports = new short[arr.length];
		for (int i = 0; i < arr.length; i++)
			ports[i] = (short)arr[i];
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		super.storeState(nbt, mode);
		nbt.putLong("b", bounds);
		int[] arr = new int[ports.length];
		for (int i = 0; i < arr.length; i++)
			arr[i] = ports[i];
		nbt.putIntArray("p", arr);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillModel(JitBakedModel model, long opaque) {
		MicroBlockFace.drawVoxels(model, MODEL, bounds, opaque);
		for (short port : ports)
			GridModels.drawPort(model, port, (port & 0xf000) == 0, bounds, opaque);
	}

	public static final ResourceLocation MODEL = Main.rl("part/battery");

}
