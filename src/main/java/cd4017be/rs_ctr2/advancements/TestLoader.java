package cd4017be.rs_ctr2.advancements;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import cd4017be.rs_ctr2.Main;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;

/**
 * @author CD4017BE */
public class TestLoader extends ReloadListener<Map<ResourceLocation, CircuitTest>>
implements Consumer<AddReloadListenerEvent> {

	private static final String[] IO_NAMES = {"a", "b", "c", "d", "x", "y"};
	private static final String DIR = "circuit_test", SUFFIX = ".csv";

	@Override
	public void accept(AddReloadListenerEvent t) {
		t.addListener(this);
	}

	@Override
	protected Map<ResourceLocation, CircuitTest>
	prepare(IResourceManager rm, IProfiler profiler) {
		Map<ResourceLocation, CircuitTest> map = new HashMap<>();
		for(ResourceLocation loc : rm.listResources(DIR, name -> name.endsWith(SUFFIX))) {
			String s = loc.getPath();
			ResourceLocation dst = new ResourceLocation(loc.getNamespace(), s.substring(
				DIR.length() + 1, s.length() - SUFFIX.length()
			));
			try {
				map.put(dst, load(dst, rm.getResource(loc).getInputStream()));
			} catch(IOException e) {
				Main.LOG.error("failed to load " + loc + " :", e);
			}
		}
		Main.LOG.info("Loaded {} circuit tests", map.size());
		return map;
	}

	private static CircuitTest load(ResourceLocation id, InputStream is) throws IOException {
		int n = 0;
		try(BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String name = "";
			int lvl = 0;
			boolean shuffle = false;
			String l;
			while((l = br.readLine()) != null && l.startsWith("#")) {
				int i = l.indexOf('=');
				String v = null;
				if (i < 0) i = l.length();
				else v = l.substring(i + 1).trim();
				String k = l.substring(1, i).trim();
				switch(k) {
				case "shuffle": shuffle = true; break;
				case "name": name = v; break;
				case "level": lvl = parseInt(v); break;
				}
			}
			if (l == null) throw new EOFException();
			String[] row = l.split(",");
			int m = row.length, m1 = m;
			byte[] columns = new byte[m];
			for (int i = 0; i < m; i++)
				if ((columns[i] = ioIndex(row[i])) >= 4)
					m1++;
			IntArrayList values = new IntArrayList(4 * m);
			for(;(l = br.readLine()) != null; n++) {
				row = l.split(",");
				if (row.length != m) throw new IOException(
					String.format("row %d has %d entries, should have %d", n, row.length, m)
				);
				for (int i = 0; i < m; i++) {
					values.add(parseInt(row[i]));
					if (columns[i] >= 4) values.add(0);
				}
			}
			if (m1 > m) {
				byte[] arr = new byte[m1];
				m1 = 0;
				for (byte b : columns) {
					arr[m1++] = b;
					if (b >= 4) arr[m1++] = -1;
				}
				columns = arr;
			}
			return new CircuitTest(id, name, columns, values.toIntArray(), shuffle, lvl);
		} catch(NumberFormatException e) {
			throw new IOException("invalid entry in row " + n + ":", e);
		}
	}

	private static byte ioIndex(String name) {
		name = name.trim().toLowerCase();
		for (byte i = 0; i < IO_NAMES.length; i++)
			if (IO_NAMES[i].equals(name)) return i;
		return -1;
	}

	private static int parseInt(String entry) throws NumberFormatException {
		entry = entry.trim().toLowerCase();
		if (entry.isEmpty()) return 0;
		if (entry.startsWith("0x"))
			return Integer.parseInt(entry.substring(2), 16);
		return Integer.parseInt(entry);
	}

	@Override
	protected void apply(
		Map<ResourceLocation, CircuitTest> map, IResourceManager rm, IProfiler profiler
	) {
		CircuitTest.TESTS = map;
		CircuitTest.SORTED = map.values().toArray(new CircuitTest[map.size()]);
		Arrays.sort(CircuitTest.SORTED);
	}

}
