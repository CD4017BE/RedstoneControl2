package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.*;
import static cd4017be.rs_ctr2.Main.SERVER_CFG;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.Sync;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.api.grid.IGridHost;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.container.ContainerMemory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;

/**@author CD4017BE */
public class Memory extends MultiBlock<Memory> implements IUnnamedContainerProvider {

	@Sync
	public int[] data;

	public Memory(int pos) {
		super(pos);
	}

	@Override
	public Item item() {
		return memory;
	}

	@Override
	public Object getHandler(int port) {
		return data;
	}

	@Override
	public void setHandler(int port, Object handler) {}

	@Override
	public boolean isMaster(int port) {
		return false;
	}

	@Override
	protected void onBoundsChange() {
		int l = Long.bitCount(bounds) * (SERVER_CFG.memory_size.get() >> 5);
		if (data == null) data = new int[l];
		else if (l != data.length) data = Arrays.copyOf(data, l);
	}

	private boolean isMemory(IGridItem item) {
		return item == mem_read || item == mem_write;
	}

	@Override
	protected ActionResultType createPort(IGridItem item, short port, boolean client) {
		if (!isMemory(item)) return ActionResultType.PASS;
		if (client) return ActionResultType.CONSUME;
		IGridHost host = this.host;
		host.removePart(this);
		ports = ArrayUtils.add(ports, (short)(port | 0xf000));
		host.addPart(this);
		return ActionResultType.SUCCESS;
	}

	@Override
	protected ActionResultType onInteract(PlayerEntity player, boolean client) {
		if (client) return ActionResultType.CONSUME;
		player.openMenu(this);
		return ActionResultType.SUCCESS;
	}

	@Override
	protected Memory splitOff(long splitBounds, long thisBounds) {
		return new Memory(-1);
	}

	@Override
	protected short[] merge(Memory other) {
		if (other.data.length > this.data.length)
			data = other.data;
		return ArrayUtils.addAll(ports, other.ports);
	}

	public final static ResourceLocation MODEL = Main.rl("part/memory");

	@Override
	protected ResourceLocation model() {
		return MODEL;
	}

	@Override
	public ContainerMemory createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerMemory(id, inv, this);
	}

	@Override
	public String toString() {
		return TooltipUtil.format("state.rs_ctr2.memory", data.length << 5);
	}

}
