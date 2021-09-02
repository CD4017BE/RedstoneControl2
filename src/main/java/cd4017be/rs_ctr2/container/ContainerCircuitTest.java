package cd4017be.rs_ctr2.container;

import static cd4017be.rs_ctr2.Content.cIRCUIT_TESTER;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.function.*;

import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.*;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.tileentity.CircuitTester;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class ContainerCircuitTest extends AdvancedContainer {

	private static final int[] SYNC_FORMAT = new int[113];
	static {
		Arrays.fill(SYNC_FORMAT, 4);
		SYNC_FORMAT[112] = 1;
		StateSyncAdv.sequence(SYNC_FORMAT);
	}

	private String[] names, ids;
	private final CircuitTester tile;
	private byte page;

	public ContainerCircuitTest(int id, PlayerInventory inv, PacketBuffer pkt) {
		this(id, inv, null, CircuitTester.class);
		this.names = new String[pkt.readVarInt()];
		this.ids = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			ids[i] = pkt.readUtf();
			String name = TooltipUtil.translate(pkt.readUtf());
			names[i] = pkt.readBoolean() ? name : "§2" + name;
		}
	}

	public ContainerCircuitTest(int id, PlayerInventory inv, CircuitTester tile, Object... args) {
		super(cIRCUIT_TESTER, id, inv, StateSyncAdv.of(tile == null, SYNC_FORMAT, 0, args), 0);
		this.tile = tile;
	}

	@Override
	protected void detectChanges(BitSet chng) {
		int[] data = tile.test == null ? new int[0] : tile.test.values;
		int w = tile.header().length;
		int l = data.length;
		for (int i = 0, k = 0; i < 16; i++) {
			int p = page * 16 + i;
			p = p < l ? tile.perm(p) : p * w;
			for (int j = 0; j < 7; j++, k++)
				sync.setInt(k, j < w && p < l ? data[p++] : 0);
		}
		sync.setByte(112, page);
	}

	public static final ResourceLocation TEX = Main.rl("textures/gui/circuit_tester.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerCircuitTest> setupGui(PlayerInventory inv, ITextComponent name) {
		DoubleSupplier intv = sync.floatGetter("intv", true);
		DoubleSupplier lat = sync.floatGetter("lat", true);
		IntSupplier t = sync.intGetter("t", true);
		
		ModularGui<ContainerCircuitTest> gui = new ModularGui<>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 228, 144, 7)
		.background(TEX, 0, 0);
		
		new Spinner(frame, 18, 18, 25, 119, false, "\\%.0ft", intv,
			v -> gui.sendPkt(A_INTERVAL, (byte)v), 1, 120, 10, 1
		).tooltip("gui.rs_ctr2.test_int");
		new Spinner(frame, 18, 18, 61, 119, false, "\\%.0ft", lat,
			v -> gui.sendPkt(A_LATENCY, (byte)v), 0, 120, 10, 1
		).tooltip("gui.rs_ctr2.test_lat");
		new Button(frame, 16, 16, 168, 120, 0,
			()-> mode(t.getAsInt()),
			a -> gui.sendPkt(A_TOGGLE)
		).texture(240, 0).tooltip("gui.rs_ctr2.test_mode#");
		Editor editor = new Editor(frame, 212, 102, 8, 16, sync);
		new Button(frame, 16, 16, 186, 120, 0,
			()-> editor.hex ? 1 : 0,
			a -> editor.hex = !editor.hex
		).texture(240, 48).tooltip("gui.rs_ctr2.hex#");
		
		GuiList list = new GuiList(frame, 200, 8, 8, 16, 12, (l, i) -> {
			l.setEnabled(false);
			frame.get(l.getIdx() + 1)//scrollbar
			.setEnabled(false);
			editor.setEnabled(true);
			if (i >= 0) gui.sendPkt(A_OPEN, ids[i]);
		}).setElements(names);
		Slider scroll = list.scrollbar(8, 12, 232, 0);
		list.setEnabled(false);
		scroll.setEnabled(false);
		new Button(frame, 16, 16, 204, 120, 0, null,
			a -> {
				boolean en = editor.enabled();
				editor.setEnabled(!en);
				list.setEnabled(en);
				scroll.setEnabled(en);
				frame.setFocus(en ? list : null);
			}
		).tooltip("gui.rs_ctr2.text_open");
		return gui.setComps(frame, false);
	}

	private static int mode(int t) {
		return t == -2 ? 2 : t < 0 ? 0 : 1;
	}

	public static final byte A_PAGE = 0, A_OPEN = 1, A_TOGGLE = 2, A_INTERVAL = 3, A_LATENCY = 4;

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		switch(pkt.readByte()) {
		case A_PAGE:
			page = pkt.readByte();
			break;
		case A_OPEN:
			tile.setTest(pkt.readUtf());
			break;
		case A_TOGGLE:
			tile.startStop(sender);
			break;
		case A_INTERVAL:
			tile.setInterval(pkt.readUnsignedByte());
			break;
		case A_LATENCY:
			tile.setLatency(pkt.readUnsignedByte());
			break;
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static class Editor extends GuiCompBase<GuiFrame> {

		final IntBuffer buf;
		final Supplier<byte[]> header;
		final Supplier<String> name;
		final IntSupplier size;
		final IntSupplier page;
		final IntSupplier t, intv, lat;
		int idx = -1;
		String lastName;
		boolean hex;

		public Editor(GuiFrame parent, int w, int h, int x, int y, StateSyncAdv sync) {
			super(parent, w, h, x, y);
			this.buf = ((ByteBuffer)sync.buffer().clear()).asIntBuffer();
			this.header = sync.objGetter("header");
			this.size = sync.intGetter("rows", false);
			this.page = sync.intGetter(112, false);
			this.t = sync.intGetter("t", true);
			this.intv = sync.intGetter("intv", false);
			this.lat = sync.intGetter("lat", false);
			this.name = sync.objGetter("name");
		}

		@Override
		public void drawOverlay(MatrixStack stack, int mx, int my) {
			if (idx < 0) return;
			int d = idx % 7;
			byte[] head = header.get();
			if (d >= head.length) return;
			String key, val = "";
			if (head[d] >= 4) {
				key = "gui.rs_ctr2.test_out";
				val = String.format(hex ? "0x%08X" : "%d", buf.get(idx + 1));
			} else key = "gui.rs_ctr2.test_in";
			parent.drawTooltip(stack, TooltipUtil.format(key, val), mx, my);
		}

		@Override
		public void drawBackground(MatrixStack stack, int mx, int my, float pt) {
			update();
			mx = mx - x >> 2;
			parent.drawRect(stack, x, y, 204, 250, 12, 6);
			int p = page.getAsInt() * 16;
			int l = Math.min(size.getAsInt() - p, 16);
			for (int i = 1, q = p; i <= l; i++, q++)
				print(stack, 0, i, q, 3, 0);
			int t = this.t.getAsInt(), intv = Math.max(1, this.intv.getAsInt()), lat = this.lat.getAsInt();
			int pi, po, pm = -1;
			if (t < -1) po = pi = l;
			else if (t < 0) po = pi = -1;
			else {
				pi = t / intv - p;
				po = (t - lat + intv - 2) / intv - p;
			}
			byte[] hd = header.get();
			for (int i = 0, x = 3; i < hd.length; i++, x += 10) {
				int k = hd[i];
				if (k < 0) {
					x -= 10;
					continue;
				}
				if (mx >= x && mx < x + 10) pm = i;
				parent.drawRect(stack, this.x + x * 4, y, 216 - (k >> 2) * 40, 232 + (k & 3) * 6, 40, 6);
				for (int j = 0; j < l; j++) {
					int q = j * 7 + i;
					int c = k < 4
						? j == pi ? 1 : 0
						: j == po ? 1 : j > po ? 0
						: buf.get(q) == buf.get(q + 1) ? 2 : 3;
					print(stack, x, j + 1, buf.get(q), hex ? -1 : 10, c);
				}
			}
			my = (my - y) / 6 - 1;
			idx = pm >= 0 && my < l ? my * 7 + pm : -1;
		}

		private void update() {
			String s = name.get();
			if (!s.equals(lastName)) {
				lastName = s;
				parent.title(s.isEmpty() ? "gui.rs_ctr2.circuit_test" : s, 0.5F);
			}
		}

		private void print(MatrixStack stack, int x, int y, int n, int w, int t) {
			x = this.x + x * 4;
			y = this.y + y * 6;
			int ty = t * 6 + 232;
			if (w < 0 || n < -999999999) {
				parent.drawRect(stack, x, y, 128, ty, 8, 6);
				x += 36;
				for (int i = 0; i < 8; i++, x -= 4, n >>= 4)
					parent.drawRect(stack, x, y, (n & 15) << 3 | (i == 0 ? 4 : 0), ty, 4, 6);
			} else {
				String s = Integer.toString(n);
				int l = w - s.length(), dx = (n < 0 ? l + 1 : l) * 4;
				parent.drawRect(stack, x, y, 168 - l * 4, ty, dx, 6);
				if (l > 8) parent.drawRect(stack, x, y, 200 - l * 4, ty, (l - 8) * 4, 6);
				x += dx;
				l = s.length();
				for (int i = n < 0 ? 1 : 0; i < l; i++, x += 4) {
					int tx = (s.charAt(i) - '0') * 8;
					if (i == l - 1) tx += 4;
					parent.drawRect(stack, x, y, tx, ty, 4, 6);
				}
			}
		}

	}

}
