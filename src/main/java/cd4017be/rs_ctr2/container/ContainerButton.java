package cd4017be.rs_ctr2.container;

import static cd4017be.rs_ctr2.Content.bUTTON;

import java.util.function.IntSupplier;

import cd4017be.api.grid.GridPart;
import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.GuiFrame;
import cd4017be.lib.gui.comp.Spinner;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.part.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author CD4017BE */
public class ContainerButton extends AdvancedContainer {

	public ContainerButton(int id, PlayerInventory inv, PacketBuffer buf) {
		super(bUTTON, id, inv, StateSyncAdv.of(true, Button.class), 0);
	}

	public ContainerButton(int id, PlayerInventory inv, GridPart part) {
		super(bUTTON, id, inv, StateSyncAdv.of(false, part), 0);
	}

	public static final ResourceLocation TEX = Main.rl("textures/gui/parts.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerButton> setupGui(PlayerInventory inv, ITextComponent name) {
		IntSupplier value = sync.intGetter("delay", false);
		
		ModularGui<ContainerButton> gui = new ModularGui<>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 86, 40, 2)
		.background(TEX, 0, 42).title("gui.rs_ctr2.pulse_len", 0.5F);
		
		new Spinner(frame, 36, 18, 34, 15, false, "\\%.2fs",
			()-> value.getAsInt() * 0.05,
			v -> gui.sendPkt((byte)(v * 20)),
			0.05, 12.5, 1.0, 0.05
		);
		return gui.setComps(frame, false);
	}

}
