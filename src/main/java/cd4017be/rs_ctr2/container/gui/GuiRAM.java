package cd4017be.rs_ctr2.container.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.IntSupplier;

import static cd4017be.rs_ctr2.container.ContainerMemory.*;
import static org.lwjgl.glfw.GLFW.*;

import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.*;
import cd4017be.lib.network.GuiNetworkHandler;
import cd4017be.lib.text.TooltipUtil;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.container.ContainerMemory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.loading.FMLPaths;

/** @author cd4017be */
public class GuiRAM extends ModularGui<ContainerMemory> {

	private static final ResourceLocation TEX = Main.rl("textures/gui/ram.png");
	private static final File dir = FMLPaths.GAMEDIR.get().resolve("rs_ctr2/memory")
	.toFile().getAbsoluteFile();

	private int fmtW, fmtH, mode;
	private final IntSupplier size, page;
	private final IntBuffer data;

	public GuiRAM(ContainerMemory cont, PlayerInventory player, ITextComponent title) {
		super(cont, player, title);
		this.size = cont.size();
		this.page = cont.page();
		this.data = cont.get();
		
		GuiFrame frame = new GuiFrame(this, 160, 135, 5)
		.background(TEX, 0, 0).title("gui.rs_ctr2.ram", 0.5F);
		new Editor(frame, 144, 102, 8, 16);
		new Button(frame, 18, 9, 7, 119, 0, null, (i)-> {
			dir.mkdirs();
			File file = new File(dir, "ram.hex");
			GuiFrame fb = new FileBrowser(frame, this::importData, null)
			.setFile(file).title("gui.rs_ctr2.import_file", 0.5F);
			fb.init(width, height, 0, font);
			fb.position(8, 8);
		}).tooltip("gui.rs_ctr2.import_file");
		new Button(frame, 18, 9, 25, 119, 0, null, (i)-> sendPkt(A_DOWNLOAD))
		.tooltip("gui.rs_ctr2.export_file");
		new TextField(frame, 48, 7, 54, 120, 8, ()-> Integer.toString(fmtW), (t)-> {
			try {
				fmtW = Integer.parseInt(t);
				if (fmtW <= 0) fmtW = 1;
				else if (fmtW > 4096) fmtW = 4096;
				fmtH = Math.max(1, Math.min(4096, addrSize() / fmtW));
			} catch(NumberFormatException e) {}
		}).tooltip("gui.rs_ctr2.fmt_w");
		new TextField(frame, 48, 7, 104, 120, 8, ()-> Integer.toString(fmtH), (t)-> {
			try {
				fmtH = Integer.parseInt(t);
				if (fmtH <= 0) fmtH = 1;
				else if (fmtH > 4096) fmtH = 4096;
			} catch(NumberFormatException e) {}
		}).tooltip("gui.rs_ctr2.fmt_h");
		compGroup = frame;
		mode = 0;
		fmtW = 16;
		fmtH = size.getAsInt() >> 4;
	}

	public int addrSize() {
		return size.getAsInt() << mode;
	}

	public int bits() {
		return 1 << 5 - mode;
	}

	public int get(int addr) {
		int bits = bits();
		return data.get(addr >> mode & 63) >>> addr * bits;
		//& (1 << bits) - 1;
	}

	public void processDownload(byte[] mem) {
		ByteBuffer data = ByteBuffer.wrap(mem);
		dir.mkdirs();
		File file = new File(dir, "ram.hex");
		GuiFrame f = new FileBrowser((GuiFrame)compGroup, (fb)-> {
			fb.close();
			exportData(fb.getFile(), data);
		}, null).setFile(file).title("gui.rs_ctr2.export_file", 0.5F);
		f.init(width, height, 0, font);
		f.position(8, 8);
	}

	private void exportData(File file, ByteBuffer data) {
		if (!file.getAbsolutePath().startsWith(dir.getAbsolutePath())) {
			sendChat(TooltipUtil.format("msg.rs_ctr2.dir_invalid", file));
			return;
		}
		String name = file.getName();
		RAMImageFormat fmt = RAMImageFormat.get(name);
		try(FileOutputStream fos = new FileOutputStream(file)) {
			int bits = bits();
			sendChat(fmt.infoMessage(true, name, fmtW, fmtH, bits));
			fmt.exportFile(fos, data, fmtW, fmtH, bits);
		} catch(FileNotFoundException e) {
			sendChat(TooltipUtil.format("msg.rs_ctr2.no_file", file));
			return;
		} catch(Exception e) {
			e.printStackTrace();
			sendChat("\u00a74" + e.toString());
			return;
		}
		sendChat(TooltipUtil.format("msg.rs_ctr2.export_succ"));
	}

	private void importData(FileBrowser fb) {
		fb.close();
		File file = fb.getFile();
		String name = file.getName();
		RAMImageFormat fmt = RAMImageFormat.get(name);
		try(FileInputStream fis = new FileInputStream(file)) {
			int bits = bits();
			sendChat(fmt.infoMessage(false, name, fmtW, fmtH, bits));
			PacketBuffer buff = GuiNetworkHandler.preparePacket(menu);
			buff.writeByte(A_UPLOAD);
			int[] wh = {fmtW, fmtH};
			fmt.importFile(fis, buff, wh, bits, size.getAsInt());
			fmtW = wh[0]; fmtH = wh[1];
			GuiNetworkHandler.GNH_INSTANCE.sendToServer(buff);
		} catch(FileNotFoundException e) {
			sendChat(TooltipUtil.format("msg.rs_ctr2.no_file", file));
		} catch(Exception e) {
			e.printStackTrace();
			sendChat("\u00a74" + e.toString());
		}
	}

	class Editor extends GuiCompBase<GuiCompGroup> {

		int cursor = -1;

		public Editor(GuiCompGroup parent, int w, int h, int x, int y) {
			super(parent, w, h, x, y);
		}

		@Override
		public void drawOverlay(MatrixStack mstack, int mx, int my) {
			if (my - y > 6) return;
			compGroup.drawTooltip(
				mstack, TooltipUtil.format("gui.rs_ctr2.ram.layout", 32 >> mode, addrSize()),
				mx, my
			);
		}

		@Override
		public void drawBackground(MatrixStack mstack, int mx, int my, float t) {
			int bits = mode;
			compGroup.drawRect(mstack, x, y, 0, 232 + bits * 6, 128, 6);
			print(mstack, 128, 0, addrSize() - 1, 4, 4);
			int p = page.getAsInt() << 6;
			int addr = p << bits, l = (Math.min(64, size.getAsInt() - p + 3) >> 2) * 6;
			for(int i = 6, j = 0; i <= l; i += 6) {
				print(mstack, 128, i, addr + j, 4, 5);
				for(int k = (4 << bits) - 1; k >= 0; k--, j++)
					print(mstack, k << 5 - bits, i, get(j), 8 >> bits, 0);
			}
			if(cursor >= 0) {
				int y = this.y + 6 + (cursor >> 5) * 6;
				int x = this.x + 124 - (cursor & 31) * 4;
				int v = (data.get(cursor >> 3 & 63) >> (cursor << 2 & 28) & 15) * 8;
				if((cursor & 7 >> bits) == 0) v += 4;
				compGroup.drawRect(mstack, x, y, v, 190, 4, 6);
			}
		}

		private void print(MatrixStack mstack, int x, int y, int v, int w, int c) {
			x += this.x + 4 * (w - 1);
			y += this.y;
			c = 196 + c * 6;
			compGroup.drawRect(mstack, x, y, (v & 15) * 8 + 4, c, 4, 6);
			for(w--; w > 0; w--)
				compGroup.drawRect(mstack, x -= 4, y, ((v >>= 4) & 15) * 8, c, 4, 6);
		}

		@Override
		public boolean keyIn(char c, int k, byte d) {
			byte v;
			switch(k) {
			case GLFW_KEY_UP: cursor -= 31;
			case GLFW_KEY_RIGHT:
				if (--cursor < 0)
					cursor = mvPage(-1) ? cursor + 512 : 0;
				if (hasShiftDown()) cursor &= ~(7 >> mode);
				return true;
			case GLFW_KEY_DOWN: cursor += 31;
			case GLFW_KEY_LEFT:
				if (hasShiftDown()) cursor |= 7 >> mode;
				if (++cursor > 511)
					cursor = mvPage(1) ? cursor - 512 : 511;
				return true;
			case GLFW_KEY_PAGE_UP: mvPage(-1); return true;
			case GLFW_KEY_PAGE_DOWN: mvPage(1); return true;
			case GLFW_KEY_HOME:
				cursor = 0;
				sendPkt(A_PAGE, (byte)0);
				return true;
			case GLFW_KEY_END:
				cursor = 511;
				sendPkt(A_PAGE, (byte)127);
				return true;
			case GLFW_KEY_0: v = 0; break;
			case GLFW_KEY_1: v = 1; break;
			case GLFW_KEY_2: v = 2; break;
			case GLFW_KEY_3: v = 3; break;
			case GLFW_KEY_4: v = 4; break;
			case GLFW_KEY_5: v = 5; break;
			case GLFW_KEY_6: v = 6; break;
			case GLFW_KEY_7: v = 7; break;
			case GLFW_KEY_8: v = 8; break;
			case GLFW_KEY_9: v = 9; break;
			case GLFW_KEY_A: v = 10; break;
			case GLFW_KEY_B: v = 11; break;
			case GLFW_KEY_C: v = 12; break;
			case GLFW_KEY_D: v = 13; break;
			case GLFW_KEY_E: v = 14; break;
			case GLFW_KEY_F: v = 15; break;
			default: return false;
			}
			sendPkt(A_SET_MEM, (short)(cursor | page.getAsInt() << 9), v);
			if (hasControlDown()) cursor += 31;
			if (++cursor > 511)
				cursor = mvPage(1) ? cursor - 512 : 511;
			return true;
		}

		@Override
		public boolean mouseIn(int mx, int my, int b, byte d) {
			mx = 31 - (mx - x) / 4;
			my = (my - y) / 6 - 1;
			if (d == A_DOWN) {
				if (my < 0) mode = mode + (b == B_LEFT ? 1 : 3) & 3;
				cursor = my * 32 + (mx < 0 ? 0 : mx);
			} else if (d == A_SCROLL)
				mvPage(-b);
			return true;
		}

		private boolean mvPage(int incr) {
			incr += page.getAsInt();
			if (incr < 0 || incr >= size.getAsInt() + 63 >> 6) return false;
			sendPkt(A_PAGE, (byte)incr);
			return true;
		}

		@Override
		public boolean focus() {
			return true;
		}

		@Override
		public void unfocus() {
			cursor = -1;
		}

	}

}
