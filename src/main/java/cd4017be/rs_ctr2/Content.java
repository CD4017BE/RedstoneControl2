package cd4017be.rs_ctr2;

import static cd4017be.lib.block.BlockTE.flags;
import static cd4017be.lib.property.PropertyOrientation.HOR_AXIS;
import static cd4017be.rs_ctr2.Main.*;
import static net.minecraftforge.client.model.ModelLoader.addSpecialModel;

import java.util.function.Supplier;

import cd4017be.api.grid.port.*;
import cd4017be.rs_ctr2.block.AccessPipe;
import cd4017be.rs_ctr2.container.*;
import cd4017be.rs_ctr2.container.gui.GuiRAM;
import cd4017be.rs_ctr2.item.*;
import cd4017be.rs_ctr2.part.*;
import cd4017be.rs_ctr2.render.FrameRenderer;
import cd4017be.rs_ctr2.render.SignalProbeRenderer;
import cd4017be.rs_ctr2.tileentity.*;
import cd4017be.lib.block.BlockTE;
import cd4017be.lib.block.OrientedBlock;
import cd4017be.lib.item.DocumentedBlockItem;
import cd4017be.lib.item.DocumentedItem;
import cd4017be.lib.part.OrientedPart;
import cd4017be.lib.templates.BaseSound;
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
	public static final OrientedBlock<PipeController> PIPE_CONTROLLER = null;
	public static final OrientedBlock<FrameController> FRAME_CONTROLLER = null;
	public static final AccessPipe ACCESS_PIPE = null;
	public static final BlockTE<Frame> FRAME = null;

	// items:
	public static final DocumentedBlockItem
	autocraft = null, block_breaker = null, item_placer = null,
	pipe_controller = null, frame_controller = null;
	public static final DocumentedBlockItem frame = null, access_pipe = null;

	public static final SignalProbeItem probe = null;
	public static final CableItem
	data_cable = null, power_cable = null, item_cable = null, fluid_cable = null, block_cable = null;
	public static final OrientedPartItem<?>
	analog_in = null, logic_in = null, analog_out = null, logic_out = null,
	comp_in = null, power_io = null, item_io = null, fluid_io = null, block_io = null,
	splitter = null, power_splitter = null, item_splitter = null, fluid_splitter = null, block_splitter = null,
	not_gate = null, clock = null, constant = null, and_filter = null,
	or_gate = null, and_gate = null, nor_gate = null, nand_gate = null,
	xor_gate = null, schmitt_trigger = null, delay = null, comparator = null,
	sr_latch = null, data_mux = null, clamp_gate = null, or_buffer = null,
	bit_shift = null, sum_gate = null, product_gate = null, division_gate = null,
	neg_gate = null, counter = null, mem_read = null, mem_write = null,
	transformer = null, item_mover = null, fluid_mover = null,
	item_filter = null, item_counter = null, item_dropper = null,
	switcH = null, led = null, switch_array = null, led_array = null,
	_7segment = null, bcd_converter = null, label = null, button = null,
	hardness_sensor = null;
	public static final WirelessItem
	data_send = null, data_recv = null, block_send = null, block_recv = null;
	public static final MultiblockItem<Battery> battery = null;
	public static final MultiblockItem<SolarCell> solarcell = null;
	public static final MultiblockItem<Memory> memory = null;
	public static final MultiblockItem<ItemBuffer> item_buffer = null;

	// containers:
	public static final ContainerType<ContainerConstant> cONSTANT = null;
	public static final ContainerType<ContainerAutoCraft> aUTOCRAFT = null;
	public static final ContainerType<ContainerMemory> mEMORY = null;
	public static final ContainerType<ContainerItemPlacer> iTEM_PLACER = null;
	public static final ContainerType<ContainerItemFilter> iTEM_FILTER = null;
	public static final ContainerType<ContainerItemBuffer> iTEM_BUFFER = null;
	public static final ContainerType<ContainerLabel> lABEL = null;
	public static final ContainerType<ContainerButton> bUTTON = null;

	//sounds:
	public static final BaseSound SWITCH_FLIp = null, BUTTON_PRESs = null, BUTTON_RELEASe = null;

	@SubscribeEvent
	public static void registerBlocks(Register<Block> ev) {
		Properties p = Properties.of(Material.STONE).strength(1.5F).requiresCorrectToolForDrops();
		ev.getRegistry().registerAll(
			new OrientedBlock<>(p, flags(AutoCrafter.class), HOR_AXIS).setRegistryName(rl("autocraft")),
			new OrientedBlock<>(p, flags(BlockBreaker.class), HOR_AXIS).setRegistryName(rl("block_breaker")),
			new OrientedBlock<>(p, flags(ItemPlacer.class), HOR_AXIS).setRegistryName(rl("item_placer")),
			new OrientedBlock<>(p, flags(PipeController.class), HOR_AXIS).setRegistryName(rl("pipe_controller")),
			new OrientedBlock<>(p, flags(FrameController.class), HOR_AXIS).setRegistryName(rl("frame_controller")),
			new AccessPipe(
				Properties.copy(Blocks.OBSIDIAN).harvestTool(ToolType.PICKAXE).harvestLevel(3)
			).setRegistryName(rl("access_pipe")),
			new BlockTE<>(p, flags(Frame.class)).setRegistryName(rl("frame"))
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
			new DocumentedBlockItem(AUTOCRAFT, p).tooltipArgs(SERVER_CFG.craft),
			new DocumentedBlockItem(BLOCK_BREAKER, p).tooltipArgs(SERVER_CFG.block_break, SERVER_CFG.hardness_break),
			new DocumentedBlockItem(ITEM_PLACER, p).tooltipArgs(SERVER_CFG.item_place),
			new DocumentedBlockItem(PIPE_CONTROLLER, p).tooltipArgs(SERVER_CFG.pipe_limit),
			new DocumentedBlockItem(FRAME_CONTROLLER, p),
			new DocumentedBlockItem(ACCESS_PIPE, p),
			new DocumentedBlockItem(FRAME, p).tooltipArgs(SERVER_CFG.frame_range, SERVER_CFG.device_range),
			new SignalProbeItem(probe).tab(CREATIVE_TAB).setRegistryName(rl("probe")),
			new CableItem(rs, ISignalReceiver.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("data_cable")),
			new CableItem(rs, IEnergyAccess.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("power_cable")),
			new CableItem(rs, IInventoryAccess.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("item_cable")),
			new CableItem(rs, IFluidAccess.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("fluid_cable")),
			new CableItem(rs, IBlockSupplier.TYPE_ID).tab(CREATIVE_TAB).setRegistryName(rl("block_cable")),
			orientedPart("analog_in", rs, AnalogIn::new),
			orientedPart("logic_in", rs, LogicIn::new),
			orientedPart("comp_in", rs, ComparatorIn::new),
			orientedPart("analog_out", rs, AnalogOut::new),
			orientedPart("logic_out", rs, LogicOut::new),
			orientedPart("power_io", rs, PowerIO::new),
			orientedPart("item_io", rs, ItemIO::new),
			orientedPart("fluid_io", rs, FluidIO::new),
			orientedPart("block_io", rs, BlockIO::new),
			orientedPart("splitter", rs, Splitter::new).tooltipArgs(SERVER_CFG.rec_data),
			orientedPart("power_splitter", rs, SplitterP::new).tooltipArgs(SERVER_CFG.rec_power),
			orientedPart("item_splitter", rs, SplitterI::new).tooltipArgs(SERVER_CFG.rec_item),
			orientedPart("fluid_splitter", rs, SplitterF::new).tooltipArgs(SERVER_CFG.rec_fluid),
			orientedPart("block_splitter", rs, SplitterB::new).tooltipArgs(SERVER_CFG.rec_block),
			orientedPart("not_gate", rs, NotGate::new),
			orientedPart("clock", rs, Clock::new),
			orientedPart("constant", rs, Constant::new),
			orientedPart("and_filter", rs, AndFilter::new),
			orientedPart("or_gate", rs, OrGate::new),
			orientedPart("and_gate", rs, AndGate::new),
			orientedPart("nor_gate", rs, NorGate::new),
			orientedPart("nand_gate", rs, NandGate::new),
			orientedPart("xor_gate", rs, XorGate::new),
			orientedPart("schmitt_trigger", rs, SchmittTrigger::new),
			orientedPart("delay", rs, Delay::new),
			orientedPart("comparator", rs, Comparator::new),
			orientedPart("sr_latch", rs, SRLatch::new),
			orientedPart("data_mux", rs, DataMux::new),
			orientedPart("clamp_gate", rs, ClampGate::new),
			orientedPart("or_buffer", rs, OrBuffer::new),
			orientedPart("bit_shift", rs, BitShift::new),
			orientedPart("sum_gate", rs, SumGate::new),
			orientedPart("product_gate", rs, MultiplyGate::new),
			orientedPart("division_gate", rs, DivisionGate::new),
			orientedPart("neg_gate", rs, Negate::new),
			orientedPart("counter", rs, Counter::new),
			orientedPart("mem_read", rs, MemRead::new),
			orientedPart("mem_write", rs, MemWrite::new),
			orientedPart("transformer", rs, Transformer::new),
			orientedPart("item_mover", rs, ItemMover::new).tooltipArgs(SERVER_CFG.move_item),
			orientedPart("fluid_mover", rs, FluidMover::new).tooltipArgs(SERVER_CFG.move_fluid),
			orientedPart("item_filter", rs, ItemFilter::new),
			orientedPart("item_counter", rs, ItemCounter::new),
			orientedPart("item_dropper", rs, ItemDropper::new),
			orientedPart("label", rs, Label::new),
			orientedPart("button", rs, Button::new),
			orientedPart("switch", rs, Switch::new),
			orientedPart("led", rs, LED::new),
			orientedPart("switch_array", rs, SwitchArray::new),
			orientedPart("led_array", rs, LEDArray::new),
			orientedPart("_7segment", rs, _7Segment::new),
			orientedPart("bcd_converter", rs, BCDConverter::new),
			orientedPart("hardness_sensor", rs, HardnessSensor::new),
			new MultiblockItem<>(p, Battery::new).tooltipArgs(SERVER_CFG.battery_cap).setRegistryName(rl("battery")),
			new MultiblockItem<>(p, SolarCell::new).tooltipArgs(SERVER_CFG.solar_power, SERVER_CFG.daytime).setRegistryName(rl("solarcell")),
			new MultiblockItem<>(p, Memory::new).tooltipArgs(SERVER_CFG.memory_size).setRegistryName(rl("memory")),
			new MultiblockItem<>(p, ItemBuffer::new).tooltipArgs(SERVER_CFG.item_buffer_size).setRegistryName(rl("item_buffer")),
			item(p, "corerope1"), item(p, "corerope2")
		);
		new WirelessItem(rs, WirelessData::new, ISignalReceiver.TYPE_ID)
		.register(ev.getRegistry(), rl("data_send"), rl("data_recv"));
		new WirelessItem(rs, WirelessBlock::new, IBlockSupplier.TYPE_ID)
		.register(ev.getRegistry(), rl("block_send"), rl("block_recv"));
	}

	private static <T extends OrientedPart> OrientedPartItem<T> orientedPart(
		String id, Item.Properties p, Supplier<T> factory
	) {
		OrientedPartItem<T> item = new OrientedPartItem<>(p, factory);
		item.setRegistryName(rl(id));
		return item;
	}

	private static Item item(Item.Properties p, String id) {
		return new DocumentedItem(p).setRegistryName(rl(id));
	}

	@SubscribeEvent
	public static void registerTileEntities(Register<TileEntityType<?>> ev) {
		ev.getRegistry().registerAll(
			AUTOCRAFT.makeTEType(AutoCrafter::new),
			BLOCK_BREAKER.makeTEType(BlockBreaker::new),
			ITEM_PLACER.makeTEType(ItemPlacer::new),
			PIPE_CONTROLLER.makeTEType(PipeController::new),
			FRAME_CONTROLLER.makeTEType(FrameController::new),
			FRAME.makeTEType(Frame::new)
		);
	}

	@SubscribeEvent
	public static void registerContainers(Register<ContainerType<?>> ev) {
		ev.getRegistry().registerAll(
			IForgeContainerType.create(ContainerConstant::new).setRegistryName(rl("constant")),
			IForgeContainerType.create(ContainerAutoCraft::new).setRegistryName(rl("autocraft")),
			IForgeContainerType.create(ContainerMemory::new).setRegistryName(rl("memory")),
			IForgeContainerType.create(ContainerItemPlacer::new).setRegistryName(rl("item_placer")),
			IForgeContainerType.create(ContainerItemFilter::new).setRegistryName(rl("item_filter")),
			IForgeContainerType.create(ContainerItemBuffer::new).setRegistryName(rl("item_buffer")),
			IForgeContainerType.create(ContainerLabel::new).setRegistryName(rl("label")),
			IForgeContainerType.create(ContainerButton::new).setRegistryName(rl("button"))
		);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void setupClient(FMLClientSetupEvent ev) {
		ScreenManager.register(cONSTANT, ContainerConstant::setupGui);
		ScreenManager.register(aUTOCRAFT, ContainerAutoCraft::setupGui);
		ScreenManager.register(mEMORY, GuiRAM::new);
		ScreenManager.register(iTEM_PLACER, ContainerItemPlacer::setupGui);
		ScreenManager.register(iTEM_FILTER, ContainerItemFilter::setupGui);
		ScreenManager.register(iTEM_BUFFER, ContainerItemBuffer::setupGui);
		ScreenManager.register(lABEL, ContainerLabel::setupGui);
		ScreenManager.register(bUTTON, ContainerButton::setupGui);
		ClientRegistry.bindTileEntityRenderer(FRAME_CONTROLLER.tileType, FrameRenderer::new);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerModels(ModelRegistryEvent ev) {
		addSpecialModel(SignalProbeRenderer.baseModel(probe));
		for (ResourceLocation loc : Cable.MODELS)
			if (loc != null) addSpecialModel(loc);
		addSpecialModel(Battery.MODEL);
		addSpecialModel(SolarCell.MODEL);
		addSpecialModel(Memory.MODEL);
		addSpecialModel(ItemBuffer.MODEL);
		addSpecialModel(Label.MODEL);
		addSpecialModel(Switch.BASE);
		addSpecialModel(Switch.OFF);
		addSpecialModel(Switch.ON);
		addSpecialModel(LED.LED);
		addSpecialModel(Button.OFF);
		addSpecialModel(Button.ON);
		ExtendablePart.registerModels(switch_array);
		ExtendablePart.registerModels(_7segment);
		for (int i = 0; i < 16; i++)
			addSpecialModel(_7Segment.MODELS[i] = rl("part/7seg" + Integer.toHexString(i)));
	}

	@SubscribeEvent
	public static void registerSounds(Register<SoundEvent> ev) {
		ev.getRegistry().registerAll(
			new BaseSound(rl("switch_flip")),
			new BaseSound(rl("button_press")),
			new BaseSound(rl("button_release"))
		);
	}

}
