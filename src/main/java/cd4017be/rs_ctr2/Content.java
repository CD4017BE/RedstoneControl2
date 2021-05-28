package cd4017be.rs_ctr2;

import static cd4017be.lib.block.BlockTE.flags;
import static cd4017be.lib.property.PropertyOrientation.HOR_AXIS;
import static cd4017be.rs_ctr2.Main.*;

import cd4017be.api.grid.port.*;
import cd4017be.rs_ctr2.container.*;
import cd4017be.rs_ctr2.container.gui.GuiRAM;
import cd4017be.rs_ctr2.item.*;
import cd4017be.rs_ctr2.part.*;
import cd4017be.rs_ctr2.render.SignalProbeRenderer;
import cd4017be.rs_ctr2.tileentity.*;
import cd4017be.lib.block.BlockTE;
import cd4017be.lib.block.OrientedBlock;
import cd4017be.lib.item.DocumentedBlockItem;
import cd4017be.lib.item.DocumentedItem;
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
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
	public static final OrientedBlock<AutoCrafter> AUTOCRAFT = null;
	public static final OrientedBlock<BlockBreaker> BLOCK_BREAKER = null;
	public static final OrientedBlock<ItemPlacer> ITEM_PLACER = null;

	// items:
	public static final DocumentedBlockItem autocraft = null;
	public static final DocumentedBlockItem block_breaker = null, item_placer = null;
	public static final SignalProbeItem probe = null;
	public static final CableItem
	data_cable = null, power_cable = null, item_cable = null, fluid_cable = null;
	public static final OrientedPartItem
	analog_in = null, logic_in = null, analog_out = null, logic_out = null,
	comp_in = null, power_io = null, item_io = null, fluid_io = null,
	splitter = null, not_gate = null, clock = null, constant = null,
	or_gate = null, and_gate = null, nor_gate = null, nand_gate = null,
	xor_gate = null, schmitt_trigger = null, delay = null, comparator = null,
	sr_latch = null, data_mux = null, clamp_gate = null, or_buffer = null,
	bit_shift = null, sum_gate = null, product_gate = null, division_gate = null,
	neg_gate = null, counter = null, mem_read = null, mem_write = null,
	transformer = null, item_mover = null, fluid_mover = null,
	power_splitter = null, item_splitter = null, fluid_splitter = null;
	public static final MultiblockItem<Battery> battery = null;
	public static final MultiblockItem<SolarCell> solarcell = null;
	public static final MultiblockItem<Memory> memory = null;

	// containers:
	public static final ContainerType<ContainerConstant> cONSTANT = null;
	public static final ContainerType<ContainerAutoCraft> aUTOCRAFT = null;
	public static final ContainerType<ContainerMemory> mEMORY = null;
	public static final ContainerType<ContainerItemPlacer> iTEM_PLACER = null;

	@SubscribeEvent
	public static void registerBlocks(Register<Block> ev) {
		Properties p = Properties.of(Material.STONE).strength(1.5F);
		ev.getRegistry().registerAll(
			new OrientedBlock<>(p, flags(AutoCrafter.class), HOR_AXIS).setRegistryName(rl("autocraft")),
			new OrientedBlock<>(p, flags(BlockBreaker.class), HOR_AXIS).setRegistryName(rl("block_breaker")),
			new OrientedBlock<>(p, flags(ItemPlacer.class), HOR_AXIS).setRegistryName(rl("item_placer"))
		);
	}

	@SubscribeEvent
	public static void registerItems(Register<Item> ev) {
		// use redstone tab so recipes don't appear under miscellaneous
		Item.Properties rs = new Item.Properties().tab(ItemGroup.TAB_REDSTONE);
		Item.Properties probe = new Item.Properties().tab(ItemGroup.TAB_TOOLS)
		.stacksTo(1).setISTER(()-> SignalProbeRenderer::new);
		Item.Properties p = new Item.Properties().tab(CREATIVE_TAB);
		ev.getRegistry().registerAll(
			new DocumentedBlockItem(AUTOCRAFT, p),
			new DocumentedBlockItem(BLOCK_BREAKER, p),
			new DocumentedBlockItem(ITEM_PLACER, p),
			new SignalProbeItem(probe).tab(CREATIVE_TAB).setRegistryName(rl("probe")),
			new CableItem(rs, ISignalReceiver.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("data_cable")),
			new CableItem(rs, IEnergyAccess.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("power_cable")),
			new CableItem(rs, IInventoryAccess.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("item_cable")),
			new CableItem(rs, IFluidAccess.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("fluid_cable")),
			new OrientedPartItem(rs, AnalogIn::new).setRegistryName(rl("analog_in")),
			new OrientedPartItem(rs, LogicIn::new).setRegistryName(rl("logic_in")),
			new OrientedPartItem(rs, ComparatorIn::new).setRegistryName(rl("comp_in")),
			new OrientedPartItem(rs, AnalogOut::new).setRegistryName(rl("analog_out")),
			new OrientedPartItem(rs, LogicOut::new).setRegistryName(rl("logic_out")),
			new OrientedPartItem(rs, PowerIO::new).setRegistryName(rl("power_io")),
			new OrientedPartItem(rs, ItemIO::new).setRegistryName(rl("item_io")),
			new OrientedPartItem(rs, FluidIO::new).setRegistryName(rl("fluid_io")),
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
			new OrientedPartItem(rs, SRLatch::new).setRegistryName(rl("sr_latch")),
			new OrientedPartItem(rs, DataMux::new).setRegistryName(rl("data_mux")),
			new OrientedPartItem(rs, ClampGate::new).setRegistryName(rl("clamp_gate")),
			new OrientedPartItem(rs, OrBuffer::new).setRegistryName(rl("or_buffer")),
			new OrientedPartItem(rs, BitShift::new).setRegistryName(rl("bit_shift")),
			new OrientedPartItem(rs, SumGate::new).setRegistryName(rl("sum_gate")),
			new OrientedPartItem(rs, MultiplyGate::new).setRegistryName(rl("product_gate")),
			new OrientedPartItem(rs, DivisionGate::new).setRegistryName(rl("division_gate")),
			new OrientedPartItem(rs, Negate::new).setRegistryName(rl("neg_gate")),
			new OrientedPartItem(rs, Counter::new).setRegistryName(rl("counter")),
			new OrientedPartItem(rs, MemRead::new).setRegistryName(rl("mem_read")),
			new OrientedPartItem(rs, MemWrite::new).setRegistryName(rl("mem_write")),
			new OrientedPartItem(rs, Transformer::new).setRegistryName(rl("transformer")),
			new OrientedPartItem(rs, ItemMover::new).tooltipArgs(SERVER_CFG.move_item).setRegistryName(rl("item_mover")),
			new OrientedPartItem(rs, FluidMover::new).tooltipArgs(SERVER_CFG.move_fluid).setRegistryName(rl("fluid_mover")),
			new OrientedPartItem(rs, SplitterP::new).setRegistryName(rl("power_splitter")),
			new OrientedPartItem(rs, SplitterI::new).setRegistryName(rl("item_splitter")),
			new OrientedPartItem(rs, SplitterF::new).setRegistryName(rl("fluid_splitter")),
			new MultiblockItem<>(p, Battery::new).tooltipArgs(SERVER_CFG.battery_cap).setRegistryName(rl("battery")),
			new MultiblockItem<>(p, SolarCell::new).tooltipArgs(SERVER_CFG.solar_power, SERVER_CFG.daytime).setRegistryName(rl("solarcell")),
			new MultiblockItem<>(p, Memory::new).tooltipArgs(SERVER_CFG.memory_size).setRegistryName(rl("memory")),
			item(p, "corerope1"), item(p, "corerope2")
		);
	}

	private static Item item(Item.Properties p, String id) {
		return new DocumentedItem(p).setRegistryName(rl(id));
	}

	@SubscribeEvent
	public static void registerTileEntities(Register<TileEntityType<?>> ev) {
		ev.getRegistry().registerAll(
			AUTOCRAFT.makeTEType(AutoCrafter::new),
			BLOCK_BREAKER.makeTEType(BlockBreaker::new),
			ITEM_PLACER.makeTEType(ItemPlacer::new)
		);
	}

	@SubscribeEvent
	public static void registerContainers(Register<ContainerType<?>> ev) {
		ev.getRegistry().registerAll(
			IForgeContainerType.create(ContainerConstant::new).setRegistryName(rl("constant")),
			IForgeContainerType.create(ContainerAutoCraft::new).setRegistryName(rl("autocraft")),
			IForgeContainerType.create(ContainerMemory::new).setRegistryName(rl("memory")),
			IForgeContainerType.create(ContainerItemPlacer::new).setRegistryName(rl("item_placer"))
		);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void setupClient(FMLClientSetupEvent ev) {
		ScreenManager.register(cONSTANT, ContainerConstant::setupGui);
		ScreenManager.register(aUTOCRAFT, ContainerAutoCraft::setupGui);
		ScreenManager.register(mEMORY, GuiRAM::new);
		ScreenManager.register(iTEM_PLACER, ContainerItemPlacer::setupGui);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerModels(ModelRegistryEvent ev) {
		ModelLoader.addSpecialModel(SignalProbeRenderer.baseModel(probe));
		for (ResourceLocation loc : Cable.MODELS)
			ModelLoader.addSpecialModel(loc);
		ModelLoader.addSpecialModel(Battery.MODEL);
		ModelLoader.addSpecialModel(SolarCell.MODEL);
		ModelLoader.addSpecialModel(Memory.MODEL);
	}

}
