package cd4017be.rs_ctr2;

import static cd4017be.rs_ctr2.Main.rl;
import static cd4017be.lib.block.BlockTE.flags;

import cd4017be.rs_ctr2.api.grid.GridPart;
import cd4017be.rs_ctr2.container.ContainerAssembler;
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

@EventBusSubscriber(modid = Main.ID, bus = Bus.MOD)
public class Content {

	static void register() {}

	public static BlockTE<RsGrid> GRID;
	public static BlockTE<Assembler> ASSEMBLER;

	public static TEModeledItem grid;
	public static DocumentedBlockItem assembler;
	public static MicroBlockItem microblock;
	public static OrientedPartItem analog_in, logic_in, analog_out, logic_out;
	public static WireItem wire;

	public static ContainerType<ContainerAssembler> C_ASSEMBLER;

	@SubscribeEvent
	public static void registerBlocks(Register<Block> ev) {
		Properties p = Properties.of(Material.METAL).strength(1.5F);
		Properties p_grid = Properties.of(Material.STONE).strength(1.5F)
		.noOcclusion().dynamicShape();
		ev.getRegistry().registerAll(
			(GRID = new BlockTE<>(p_grid, flags(RsGrid.class))).setRegistryName(rl("grid")),
			(ASSEMBLER = new BlockTE<>(p, flags(Assembler.class))).setRegistryName(rl("assembler"))
		);
		GridPart.GRID_HOST_BLOCK = GRID.defaultBlockState();
	}

	@SubscribeEvent
	public static void registerItems(Register<Item> ev) {
		Item.Properties p = new Item.Properties().tab(creativeTab);
		ev.getRegistry().registerAll(
			grid = new TEModeledItem(GRID, p),
			assembler = new DocumentedBlockItem(ASSEMBLER, p),
			(microblock = new MicroBlockItem(p)).setRegistryName(rl("microblock")),
			(analog_in = new OrientedPartItem(p, AnalogIn::new)).setRegistryName(rl("analog_in")),
			(logic_in = new OrientedPartItem(p, LogicIn::new)).setRegistryName(rl("logic_in")),
			(analog_out = new OrientedPartItem(p, AnalogOut::new)).setRegistryName(rl("analog_out")),
			(logic_out = new OrientedPartItem(p, LogicOut::new)).setRegistryName(rl("logic_out")),
			(wire = new WireItem(p)).setRegistryName(rl("wire"))
		);
	}

	@SubscribeEvent
	public static void registerTileEntities(Register<TileEntityType<?>> ev) {
		ev.getRegistry().registerAll(
			GRID.makeTEType(RsGrid::new),
			ASSEMBLER.makeTEType(Assembler::new)
		);
	}

	@SubscribeEvent
	public static void registerContainers(Register<ContainerType<?>> ev) {
		ev.getRegistry().registerAll(
			(C_ASSEMBLER = IForgeContainerType.create(ContainerAssembler::new)).setRegistryName(rl("assembler"))
		);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void setupClient(FMLClientSetupEvent ev) {
		ScreenManager.register(C_ASSEMBLER, ContainerAssembler::setupGui);
	}


	public static final ItemGroup creativeTab = new ItemGroup(Main.ID) {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(logic_in);
		}
	};

}
