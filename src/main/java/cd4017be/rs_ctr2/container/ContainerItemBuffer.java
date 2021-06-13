package cd4017be.rs_ctr2.container;

import static cd4017be.lib.gui.comp.Progressbar.V_FILL_R;
import static cd4017be.rs_ctr2.Content.iTEM_BUFFER;

import java.util.function.DoubleSupplier;
import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.container.slot.GlitchSaveSlot;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.*;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.part.ItemBuffer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

/**
 * @author CD4017BE */
public class ContainerItemBuffer extends AdvancedContainer {

	int slots, max;

	public ContainerItemBuffer(int id, PlayerInventory inv, PacketBuffer buf) {
		this(id, inv, new BasicInventory(16), ItemBuffer.class);
		this.slots = buf.readUnsignedByte();
		this.max = buf.readInt();
	}

	public ContainerItemBuffer(int id, PlayerInventory pinv, IItemHandler inv, Object sync) {
		super(iTEM_BUFFER, id, pinv, StateSyncAdv.of(
			sync instanceof Class, new int[0], 16, new Object[] {sync}
		), 16);
		for (int j = 0, k = 0; j < 4; j++)
			for (int i = 0; i < 4; i++, k++)
				addSlot(new GlitchSaveSlot(inv, k, 26 + i * 36, 16 + j * 18, false), true);
		addPlayerInventory(8, 104);
		if (!pinv.player.level.isClientSide)
			transferHandlers.add((stack, cont) -> {
				cont.hardInvUpdate();
				return cont.moveItemStackTo(stack, 0, 16, false);
			});
	}

	public static final ResourceLocation TEX = Main.rl("textures/gui/item_buffer.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerItemBuffer> setupGui(PlayerInventory inv, ITextComponent name) {
		DoubleSupplier n = sync.floatGetter("n", true);
		DoubleSupplier scroll = sync.floatGetter("scroll", true);
		
		ModularGui<ContainerItemBuffer> gui = new ModularGui<>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 176, 186, 6)
		.background(TEX, 0, 0).title("gui.rs_ctr2.item_buffer", 0.5F);
		
		new Slider(
			frame, 8, 12, 70, 160, 16, 248, 0, false,
			scroll, x -> {
				if ((int)x == (int)scroll.getAsDouble()) return;
				gui.sendPkt((byte)x);
			}, null, 0, slots - 16
		).scroll(-4F).tooltip("gui.cd4017be.scroll");
		new Progressbar(
			frame, 4, 70, 153, 16, 252, 12, V_FILL_R,
			n, 0, max
		).tooltip("gui.rs_ctr2.buffer_fill");
		for (int i = 0; i < 4; i++) {
			int s = slots - i * 4;
			new Progressbar(
				frame, 144, 16, 7, 16 + i * 18, 0, 240, Progressbar.H_FILL,
				()-> s - scroll.getAsDouble(), 4, 0
			);
		}
		return gui.setComps(frame, false);
	}

}
