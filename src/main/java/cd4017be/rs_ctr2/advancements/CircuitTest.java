package cd4017be.rs_ctr2.advancements;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

/**
 * @author CD4017BE */
public class CircuitTest implements Comparable<CircuitTest> {

	public static Map<ResourceLocation, CircuitTest> TESTS = new HashMap<>();
	public static CircuitTest[] SORTED = new CircuitTest[0];

	public final ResourceLocation id;
	public final String name;
	public final byte[] columns;
	public final int[] values;
	public final boolean shuffle;
	private final int lvl;

	public CircuitTest(ResourceLocation id, String name, byte[] columns, int[] values, boolean shuffle, int sort) {
		this.id = id;
		this.name = name;
		this.columns = columns;
		this.values = values;
		this.shuffle = shuffle;
		this.lvl = sort;
	}

	public int columns() {
		return columns.length;
	}

	public int rows() {
		return values.length / columns.length;
	}

	@Override
	public int compareTo(CircuitTest o) {
		int d = lvl - o.lvl;
		return d != 0 ? d : id.compareTo(o.id);
	}

}
