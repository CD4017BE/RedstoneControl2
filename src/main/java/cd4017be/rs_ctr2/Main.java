package cd4017be.rs_ctr2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cd4017be.rs_ctr2.api.gate.GateUpdater;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(Main.ID)
public class Main {

	public static final String ID = "rs_ctr2";
	public static final Logger LOG = LogManager.getLogger(ID);
	public static final ConfigServer SERVER_CFG = new ConfigServer();
	public static final ItemGroup CREATIVE_TAB = new ItemGroup(ID) {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(Content.logic_in);
		}
	};

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}

	public Main() {
		SERVER_CFG.register("RedstoneControl2");
		MinecraftForge.EVENT_BUS.register(GateUpdater.class);
	}

}
