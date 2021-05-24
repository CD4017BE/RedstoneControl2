package cd4017be.rs_ctr2.item;

import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.api.grid.*;
import cd4017be.lib.item.DocumentedItem;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.rs_ctr2.api.IProbeInfo;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/** @author CD4017BE */
public class SignalProbeItem extends DocumentedItem implements IGridItem {

	public SignalProbeItem(Properties p) {
		super(p);
	}

	@Override
	public GridPart createPart() {
		return null;
	}

	@Override
	public ActionResultType onInteract(
		IGridHost grid, ItemStack stack, PlayerEntity player, Hand hand, BlockRayTraceResult hit
	) {
		if (hand == null) return ActionResultType.PASS;
		int pos = IGridHost.target(hit, false);
		if (pos < 0) return ActionResultType.PASS;
		if (player.level.isClientSide) return ActionResultType.CONSUME;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		nbt.putByte("gp", (byte)pos);
		nbt.putLong("bp", hit.getBlockPos().asLong());
		return ActionResultType.SUCCESS;
	}

	@SuppressWarnings("resource")
	@Override
	public ActionResultType useOn(ItemUseContext use) {
		if (use.getLevel().isClientSide) return ActionResultType.CONSUME;
		CompoundNBT nbt = use.getItemInHand().getOrCreateTag();
		nbt.putByte("gp", (byte)-1);
		nbt.putLong("bp", use.getClickedPos().asLong());
		return ActionResultType.SUCCESS;
	}

	@Override
	public void inventoryTick(
		ItemStack stack, World world, Entity entity, int slot, boolean sel
	) {
		if(!sel || world.isClientSide) return;
		CompoundNBT nbt = stack.getOrCreateTag();
		BlockPos pos = BlockPos.of(nbt.getLong("bp"));
		if (!pos.closerThan(entity.position(), 8)) {
			nbt.remove("ports");
			nbt.remove("info");
			return;
		}
		TileEntity te = world.getBlockEntity(pos);
		byte gp = nbt.getByte("gp");
		if (gp >= 0 && te instanceof IGridHost) {
			IGridHost host = (IGridHost)te;
			long m = 1L << gp;
			GridPart part = host.findPart(p -> (p.bounds & m) != 0 && p.ports.length > 0);
			if (part != null) {
				packInfo(nbt, part);
				return;
			} else nbt.putByte("gp", (byte)-1);
		}
		packInfo(nbt, te);
	}

	private static void packInfo(CompoundNBT nbt, Object info) {
		//ports:
		if (info instanceof IGridPortHolder)
			nbt.put("ports", ((IGridPortHolder)info).extPorts().serializeNBT());
		else if (info instanceof GridPart) {
			short[] ports = ((GridPart)info).ports;
			int[] arr = new int[ports.length];
			for (int i = 0; i < ports.length; i++)
				arr[i] = ports[i] & 0xffff
				| (((GridPart)info).isMaster(i) ? 0x90000000 : 0x10000000);
			nbt.putIntArray("ports", arr);
		} else nbt.putIntArray("ports", ArrayUtils.EMPTY_INT_ARRAY);
		//info:
		Object[] args = info instanceof IProbeInfo ?
			((IProbeInfo)info).stateInfo() : null;
		if (args == null || args.length < 1) {
			nbt.remove("info");
			return;
		}
		CompoundNBT tag = new CompoundNBT();
		tag.putString("key", Objects.toString(args[0]));
		for (int i = 1; i < args.length; i++) {
			String key = Integer.toString(i - 1);
			Object o = args[i];
			if (o instanceof Double || o instanceof Float)
				tag.putDouble(key, ((Number)o).doubleValue());
			else if (o instanceof Long)
				tag.putLong(key, (Long)o);
			else if (o instanceof Number)
				tag.putInt(key, ((Number)o).intValue());
			else if (o instanceof Boolean)
				tag.putBoolean(key, (Boolean)o);
			else tag.putString(key, Objects.toString(o));
		}
		nbt.put("info", tag);
	}

	public static int[] readPorts(CompoundNBT nbt) {
		INBT tag = nbt.get("ports");
		if (tag instanceof IntArrayNBT)
			return ((IntArrayNBT)tag).getAsIntArray();
		if (tag instanceof LongArrayNBT) {
			long[] arr = ((LongArrayNBT)tag).getAsLongArray();
			int[] ports = new int[arr.length];
			for (int i = 0; i < arr.length; i++)
				ports[i] = (int)(arr[i] >> 32);
			return ports;
		}
		return null;
	}

	public static String readInfo(CompoundNBT nbt) {
		nbt = nbt.getCompound("info");
		if (nbt.isEmpty()) return TooltipUtil.translate("state.rs_ctr2.none");
		String key = nbt.getString("key");
		Object[] args = new Object[nbt.size() - 1];
		for (String k : nbt.getAllKeys()) {
			try {
				int i = Integer.parseInt(k);
				if (i >= args.length) continue;
				INBT tag = nbt.get(k);
				switch(tag.getId()) {
				case NBT.TAG_BYTE: args[i] = ((NumberNBT)tag).getAsByte() != 0; break;
				case NBT.TAG_INT: args[i] = ((NumberNBT)tag).getAsInt(); break;
				case NBT.TAG_LONG: args[i] = ((NumberNBT)tag).getAsLong(); break;
				case NBT.TAG_DOUBLE: args[i] = ((NumberNBT)tag).getAsDouble(); break;
				case NBT.TAG_STRING: args[i] = TooltipUtil.translate(((StringNBT)tag).getAsString()); break;
				default: continue;
				}
			} catch (NumberFormatException e) {}
		}
		return TooltipUtil.format(key, args);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || oldStack.getItem() != newStack.getItem();
	}

	@Override
	public boolean canAttackBlock(
		BlockState state, World world, BlockPos pos, PlayerEntity player
	) {
		if (!world.isClientSide && player.isCreative())
			world.getBlockState(pos).attack(world, pos, player);
		return false;
	}

}
