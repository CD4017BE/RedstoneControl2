package cd4017be.rs_ctr2.container;

import static cd4017be.lib.gui.comp.Progressbar.H_SLIDE;
import static cd4017be.rs_ctr2.Content.bIT_SHUFFLE;

import java.nio.ByteBuffer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import cd4017be.api.grid.GridPart;
import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.*;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.part.BitShuffle;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class ContainerBitShuffle extends AdvancedContainer {

	public static final byte A_AND = 0, A_XOR = 1, A_SHIFT = 2;

	public ContainerBitShuffle(int id, PlayerInventory inv, PacketBuffer buf) {
		super(bIT_SHUFFLE, id, inv, StateSyncAdv.of(true, BitShuffle.class), 0);
	}

	public ContainerBitShuffle(int id, PlayerInventory inv, GridPart part) {
		super(bIT_SHUFFLE, id, inv, StateSyncAdv.of(false, part), 0);
	}

	public static final ResourceLocation TEX = Main.rl("textures/gui/parts.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerBitShuffle> setupGui(PlayerInventory inv, ITextComponent name) {
		IntSupplier value = sync.intGetter("value", true);
		IntSupplier xor = sync.intGetter("xor", true);
		int idx = (int)sync.rawIndex(null, "shift");
		ByteBuffer buf = sync.buffer();
		DoubleSupplier shift = ()-> buf.get(idx) & 0x3f;
		
		ModularGui<ContainerBitShuffle> gui = new ModularGui<>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 86, 52, 2)
		.background(TEX, 0, 82).title("gui.rs_ctr2.bit_shuffle", 0.5F);
		
		new TextField(frame, 48, 7, 29, 17, 8,
			()-> String.format("%08X", value.getAsInt()),
			t -> {
				try { gui.sendPkt(A_AND, (int)Long.parseLong(t, 16)); }
				catch(NumberFormatException e) {}
			}
		).tooltip("gui.rs_ctr2.and_mask");
		new TextField(frame, 48, 7, 29, 37, 8,
			()-> String.format("%08X", xor.getAsInt()),
			t -> {
				try { gui.sendPkt(A_XOR, (int)Long.parseLong(t, 16)); }
				catch(NumberFormatException e) {}
			}
		).tooltip("gui.rs_ctr2.xor_mask");
		new Slider(frame, 1, 6, 48, 29, 27, 0, 250, true,
			shift, v -> buf.put(idx, (byte)v),
			()-> gui.sendPkt(A_SHIFT, buf.get(idx)), 31, 0
		).scroll(1).tooltip("gui.rs_ctr2.shift");
		new Progressbar(frame, 48, 3, 29, 30, 0, 253, H_SLIDE, shift, 32, 0);
		new FormatText(frame, 12, 8, 15, 26, "\\%d", ()-> new Object[] {buf.get(idx) & 0x3f});
		return gui.setComps(frame, false);
	}

}
