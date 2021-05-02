package cd4017be.rs_ctr2.api.gate;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat.LevelSave;
import net.minecraftforge.fml.WorldPersistenceHooks;
import net.minecraftforge.fml.WorldPersistenceHooks.WorldPersistenceHook;

public class Link {

	private IPortHolder provider, master;
	private int providerPort, masterPort;

	private Link(int id) {}

	private void load(IPortHolder obj, int port) {
		if (obj.isMaster(port)) {
			master = obj;
			masterPort = port;
		} else {
			provider = obj;
			providerPort = port;
		}
		if (provider != null && master != null)
			master.setHandler(masterPort, provider.getHandler(providerPort));
	}

	private boolean unload(IPortHolder obj, int port) {
		if (obj.isMaster(port)) {
			obj.setHandler(port, null);
			if (master == obj && masterPort == port) {
				master = null;
				if (provider == null) return true;
			}
		} else if (provider == obj && providerPort == port) {
			provider = null;
			if (master == null) return true;
			master.setHandler(masterPort, null);
		}
		return false;
	}


	private static final Int2ObjectOpenHashMap<Link> LINKS = new Int2ObjectOpenHashMap<>();
	private static int NEXT_ID;
	static {
		WorldPersistenceHooks.addHook(new WorldPersistenceHook() {

			@Override
			public String getModId() {
				return "indlog2";
			}

			@Override
			public CompoundNBT getDataForWriting(
				LevelSave levelSave, IServerConfiguration serverInfo
			) {
				CompoundNBT nbt = new CompoundNBT();
				nbt.putInt("nextLink", NEXT_ID);
				return nbt;
			}

			@Override
			public void readData(
				LevelSave levelSave, IServerConfiguration serverInfo, CompoundNBT tag
			) {
				NEXT_ID = tag.getInt("nextLink");
			}
		});
	}

	public static void clear() {
		LINKS.clear();
		NEXT_ID = 0;
	}

	public static int newId() {
		return NEXT_ID++;
	}

	public static void load(IPortHolder obj, int port, int linkId) {
		LINKS.computeIfAbsent(linkId, Link::new).load(obj, port);
	}

	public static void unload(IPortHolder obj, int port, int linkId) {
		Link l = LINKS.get(linkId);
		if (l != null && l.unload(obj, port))
			LINKS.remove(linkId);
	}

}
