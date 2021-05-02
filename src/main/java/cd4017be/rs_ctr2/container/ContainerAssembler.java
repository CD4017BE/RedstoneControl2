package cd4017be.rs_ctr2.container;

import static cd4017be.lib.gui.comp.Progressbar.H_FILL;
import static cd4017be.lib.network.StateSyncAdv.array;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.rs_ctr2.Content;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.api.grid.IGridItem;
import cd4017be.rs_ctr2.tileentity.Assembler;
import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.container.slot.GlitchSaveSlot;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.*;
import cd4017be.lib.network.StateSyncAdv;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;


public class ContainerAssembler extends AdvancedContainer {

	final Assembler tile;

	public ContainerAssembler(int id, PlayerInventory inv, PacketBuffer buf) {
		this(id, inv, null, Assembler.class);
	}

	public ContainerAssembler(int id, PlayerInventory inv, Assembler tile, Object... toSync) {
		super(Content.C_ASSEMBLER, id, inv, StateSyncAdv.of(
			tile == null, array(2, 21), 0, toSync
		), 0);
		this.tile = tile;
		IItemHandler inv1 = tile != null ? tile : new BasicInventory(23);
		addSlot(new GlitchSaveSlot(inv1, 0, 8, 16, false));
		addSlot(new GlitchSaveSlot(inv1, 1, 8, 52, false));
		for (int j = 0, k = 2; j < 3; j++)
			for (int i = 0; i < 7; i++, k++)
				addSlot(new GlitchSaveSlot(inv1, k, 44 + i*18, 16 + j*18, false));
		addPlayerInventory(8, 86);
		transferHandlers.add((stack, cont)->
			stack.getItem() instanceof IGridItem
				? cont.moveItemStackTo(stack, 2, 23, false)
				: cont.moveItemStackTo(stack, 0, 1, false)
		);
	}

	@Override
	protected void detectChanges(BitSet chng) {
		super.detectChanges(chng);
		for (int i = 0; i < 21; i++)
			sync.setShort(i, tile.count(i));
	}

	@OnlyIn(Dist.CLIENT)
	private static final ResourceLocation TEX = Main.rl("textures/gui/assembler.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerAssembler> setupGui(PlayerInventory inv, ITextComponent name) {
		ModularGui<ContainerAssembler> gui
		= new ModularGui<ContainerAssembler>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 176, 168, 16)
		.title("gui.rs_ctr2.assembler", 0.5F).background(TEX, 0, 0);
		
		new Progressbar(
			frame, 16, 10, 26, 19, 240, 0, H_FILL,
			sync.floatGetter("progressDA", false), 0.0, -1.0
		);
		new Progressbar(
			frame, 16, 10, 26, 55, 240, 10, H_FILL,
			sync.floatGetter("progressASS", false), 0.0, -1.0
		);
		new ItemCounts(frame, 126, 54, 43, 15, sync.buffer());
		gui.slotTooltips.put(0, "gui.rs_ctr2.destroy");
		gui.slotTooltips.put(1, "gui.rs_ctr2.replicate");
		return gui.setComps(frame, false);
	}

	private static class ItemCounts extends GuiCompBase<GuiCompGroup> {

		final ByteBuffer buf;

		public ItemCounts(GuiCompGroup parent, int w, int h, int x, int y, ByteBuffer buf) {
			super(parent, w, h, x, y);
			this.buf = buf.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
		}

		@Override
		public void drawBackground(MatrixStack stack, int mx, int my, float t) {
			buf.rewind();
			int x0 = x + 17, y0 = y + 11;
			for (int y = 0; y < h; y += 18)
				for (int x = 0; x < w; x += 18) {
					int n = buf.getShort();
					if (n <= 0)
						drawNumber(stack, x0 + x, y0 + y, n);
				}
		}

		private void drawNumber(MatrixStack stack, int x, int y, int n) {
			char[] symbols = Integer.toString(n).toCharArray();
			x -= symbols.length << 2;
			for (char c : symbols) {
				parent.drawRect(stack, x, y, 192 + (c - '0' << 2), 0, 4, 6);
				x += 4;
			}
		}

	}

}
