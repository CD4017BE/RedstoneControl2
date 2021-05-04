package cd4017be.rs_ctr2;

import static cd4017be.rs_ctr2.Main.CREATIVE_TAB;
import static cd4017be.rs_ctr2.Main.rl;
import static cd4017be.lib.block.BlockTE.flags;

import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.container.ContainerAssembler;
import cd4017be.rs_ctr2.container.ContainerConstant;
import cd4017be.rs_ctr2.item.*;
import cd4017be.rs_ctr2.part.*;
import cd4017be.rs_ctr2.tileentity.*;
import cd4017be.lib.block.BlockTE;
import cd4017be.lib.item.DocumentedBlockItem;
import cd4017be.lib.item.TEModeledItem;
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ObjectHolder;

/**Registers all added game content.
 * Elements that belong to the same feature typically use the same registry name
 * so they are differentiated by case to avoid variable name conflicts:<br>
 * Block -> ALL_UPPERCASE<br>
 * Item -> all_lowercase<br>
 * TileEntity -> stored in {@link BlockTE#tileType}<br>
 * ContainerType -> fIRST_LOWERCASE_REMAINING_UPPERCASE
 * @author CD4017BE */
@EventBusSubscriber(modid = Main.ID, bus = Bus.MOD)
@ObjectHolder(value = Main.ID)
public class Content {

	// blocks:
	public static final BlockTE<RsGrid> GRID = null;
	public static final BlockTE<Assembler> ASSEMBLER = null;

	// items:
	public static final TEModeledItem grid = null;
	public static final DocumentedBlockItem assembler = null;
	public static final MicroBlockItem microblock = null;
	public static final WireItem wire = null;
	public static final OrientedPartItem
	analog_in = null, logic_in = null, analog_out = null, logic_out = null,
	splitter = null, not_gate = null, clock = null, constant = null,
	or_gate = null, and_gate = null, nor_gate = null, nand_gate = null,
	xor_gate = null, schmitt_trigger = null, delay = null, comparator = null,
	sr_latch = null;

	// containers:
	public static final ContainerType<ContainerAssembler> aSSEMBLER = null;
	public static final ContainerType<ContainerConstant> cONSTANT = null;

	@SubscribeEvent
	public static void registerBlocks(Register<Block> ev) {
		Properties p = Properties.of(Material.STONE).strength(1.5F);
		Properties p_grid = Properties.of(Material.STONE).strength(1.5F)
		.noOcclusion().dynamicShape();
		ev.getRegistry().registerAll(
			new BlockTE<>(p_grid, flags(RsGrid.class)).setRegistryName(rl("grid")),
			new BlockTE<>(p, flags(Assembler.class)).setRegistryName(rl("assembler"))
		);
	}

	@SubscribeEvent
	public static void registerItems(Register<Item> ev) {
		// use redstone tab so recipes don't appear under miscellaneous
		Item.Properties rs = new Item.Properties().tab(ItemGroup.TAB_REDSTONE);
		Item.Properties p = new Item.Properties().tab(CREATIVE_TAB);
		ev.getRegistry().registerAll(
			new TEModeledItem(GRID, p),
			new DocumentedBlockItem(ASSEMBLER, p),
			new MicroBlockItem(p).setRegistryName(rl("microblock")),
			new WireItem(rs).tab(CREATIVE_TAB).setRegistryName(rl("wire")),
			new OrientedPartItem(rs, AnalogIn::new).setRegistryName(rl("analog_in")),
			new OrientedPartItem(rs, LogicIn::new).setRegistryName(rl("logic_in")),
			new OrientedPartItem(rs, AnalogOut::new).setRegistryName(rl("analog_out")),
			new OrientedPartItem(rs, LogicOut::new).setRegistryName(rl("logic_out")),
			new OrientedPartItem(rs, Splitter::new).setRegistryName(rl("splitter")),
			new OrientedPartItem(rs, NotGate::new).setRegistryName(rl("not_gate")),
			new OrientedPartItem(rs, Clock::new).setRegistryName(rl("clock")),
			new OrientedPartItem(rs, Constant::new).setRegistryName(rl("constant")),
			new OrientedPartItem(rs, OrGate::new).setRegistryName(rl("or_gate")),
			new OrientedPartItem(rs, AndGate::new).setRegistryName(rl("and_gate")),
			new OrientedPartItem(rs, NorGate::new).setRegistryName(rl("nor_gate")),
			new OrientedPartItem(rs, NandGate::new).setRegistryName(rl("nand_gate")),
			new OrientedPartItem(rs, XorGate::new).setRegistryName(rl("xor_gate")),
			new OrientedPartItem(rs, SchmittTrigger::new).setRegistryName(rl("schmitt_trigger")),
			new OrientedPartItem(rs, Delay::new).setRegistryName(rl("delay")),
			new OrientedPartItem(rs, Comparator::new).setRegistryName(rl("comparator")),
			new OrientedPartItem(rs, SRLatch::new).setRegistryName(rl("sr_latch"))
		);
	}

	@SubscribeEvent
	public static void registerTileEntities(Register<TileEntityType<?>> ev) {
		ev.getRegistry().registerAll(
			GRID.makeTEType(RsGrid::new),
			ASSEMBLER.makeTEType(Assembler::new)
		);
		GridPart.GRID_HOST_BLOCK = GRID.defaultBlockState();
	}

	@SubscribeEvent
	public static void registerContainers(Register<ContainerType<?>> ev) {
		ev.getRegistry().registerAll(
			IForgeContainerType.create(ContainerAssembler::new).setRegistryName(rl("assembler")),
			IForgeContainerType.create(ContainerConstant::new).setRegistryName(rl("constant"))
		);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void setupClient(FMLClientSetupEvent ev) {
		ScreenManager.register(aSSEMBLER, ContainerAssembler::setupGui);
		ScreenManager.register(cONSTANT, ContainerConstant::setupGui);
	}

}
