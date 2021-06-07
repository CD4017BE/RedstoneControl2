package cd4017be.rs_ctr2;

import java.util.function.Consumer;

import cd4017be.api.grid.Link;
import cd4017be.lib.config.ModConfig;
import cd4017be.rs_ctr2.part.SolarCell;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

/**@author CD4017BE */
public class ConfigServer extends ModConfig implements Consumer<FMLServerStartingEvent> {

	public final IntValue battery_cap, solar_power, move_item, move_fluid,
	craft, block_break, hardness_break, item_place;
	public final DoubleValue daytime;
	public final IntValue rec_data, rec_power, rec_item, rec_fluid;
	public final IntValue memory_size, pipe_limit;

	protected ConfigServer() {
		super(Type.SERVER);
		Builder b = new Builder();
		b.push("energy");
		battery_cap = b
		.comment("RF capacity per battery cell.")
		.defineInRange("battery_cap", 16000, 1, Integer.MAX_VALUE / 64);
		solar_power = b
		.comment("RF generated per cell over 8 ticks at maximum sun illumination.")
		.defineInRange("solar_power", 8, 0, Integer.MAX_VALUE / 64);
		daytime = b
		.comment("Integrated fraction of the day with full sun illumination.")
		.defineInRange("daytime", 7.0/18.0, 0.0, 1.0);
		move_item = b
		.comment("RF required to move 64 items.")
		.defineInRange("move_item", 64, 0, Integer.MAX_VALUE);
		move_fluid = b
		.comment("RF required to move one bucket of liquid.")
		.defineInRange("move_fluid", 64, 0, Integer.MAX_VALUE);
		craft = b
		.comment("RF required per crafting operation.")
		.defineInRange("craft", 20, 0, Integer.MAX_VALUE);
		item_place = b
		.comment("RF required per placement/interact operation.")
		.defineInRange("item_place", 50, 0, Integer.MAX_VALUE);
		block_break = b
		.comment("Base RF required to break a block.")
		.defineInRange("block_break", 100, 0, Integer.MAX_VALUE);
		hardness_break = b
		.comment("Additional RF required per block hardness value.")
		.defineInRange("hardness_cost", 40, 0, Integer.MAX_VALUE);
		b.pop();
		
		memory_size = b
		.comment("memory size in bits per cell.")
		.defineInRange("memory_size", 256, 32, 16384);
		pipe_limit = b
		.comment("Maximum distance Block Interaction Pipes can be extended.")
		.defineInRange("max_pipe_length", 256, 0, 30000000);
		
		b.push("recursion_limit");
		rec_data = b.defineInRange("data", Link.REC_DATA, 0, 64);
		rec_power = b.defineInRange("power", Link.REC_POWER, 0, 64);
		rec_item = b.defineInRange("power", Link.REC_ITEM, 0, 64);
		rec_fluid = b.defineInRange("power", Link.REC_FLUID, 0, 64);
		b.pop();
		finish(b);
		MinecraftForge.EVENT_BUS.addListener(this);
	}

	@Override
	public void accept(FMLServerStartingEvent t) {
		Link.REC_DATA = rec_data.get();
		Link.REC_POWER = rec_power.get();
		Link.REC_ITEM = rec_item.get();
		Link.REC_FLUID = rec_fluid.get();
		SolarCell.INV_DAY_LENGHT = 1F / (float)(daytime.get() * 18000.0);
	}

}
