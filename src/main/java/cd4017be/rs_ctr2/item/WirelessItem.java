package cd4017be.rs_ctr2.item;

import java.util.function.Supplier;

import cd4017be.api.grid.Link;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.rs_ctr2.part.Wireless;
import cd4017be.rs_ctr2.part.WirelessMaster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * @author CD4017BE */
public class WirelessItem extends OrientedPartItem<Wireless> {

	public final WirelessItem pair;
	public final int type;

	public WirelessItem(Properties p, Supplier<Wireless> factory, int type) {
		super(p, factory);
		this.type = type;
		this.pair = new WirelessItem(p, this);
	}

	private WirelessItem(Properties p, WirelessItem item) {
		super(p, null);
		this.type = item.type;
		this.pair = item;
	}

	public void register(
		IForgeRegistry<Item> registry,
		ResourceLocation main, ResourceLocation master
	) {
		registry.registerAll(
			this.setRegistryName(main),
			pair.setRegistryName(master)
		);
	}

	@Override
	public Wireless createPart() {
		return factory != null ? factory.get() : new WirelessMaster(this);
	}

	@Override
	protected void onPlace(Wireless part, ItemStack stack, PlayerEntity player) {
		if (hasLink(stack)) part.link = stack.getTag().getInt("link");
		else {
			ItemStack stack1 = new ItemStack(pair);
			stack1.getOrCreateTag().putInt("link", part.link = Link.newId());
			ItemFluidUtil.dropStack(stack1, player);
		}
		super.onPlace(part, stack, player);
	}

	public static boolean hasLink(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		return nbt != null && nbt.contains("link", NBT.TAG_INT);
	}

}
