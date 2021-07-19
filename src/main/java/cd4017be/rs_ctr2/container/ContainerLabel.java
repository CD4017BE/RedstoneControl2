package cd4017be.rs_ctr2.container;

import static cd4017be.rs_ctr2.Content.lABEL;

import java.util.function.Supplier;

import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.GuiFrame;
import cd4017be.lib.gui.comp.TextField;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.part.Label;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * @author CD4017BE */
public class ContainerLabel extends AdvancedContainer {

	public ContainerLabel(int id, PlayerInventory inv, PacketBuffer pkt) {
		super(lABEL, id, inv, StateSyncAdv.of(true, Label.class), 0);
	}

	public ContainerLabel(int id, PlayerInventory inv, Label part) {
		super(lABEL, id, inv, StateSyncAdv.of(false, part), 0);
	}

	public static final ResourceLocation TEX = Main.rl("textures/gui/parts.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerLabel> setupGui(PlayerInventory inv, ITextComponent name) {
		Supplier<String> text = sync.objGetter("text");
		
		ModularGui<ContainerLabel> gui = new ModularGui<>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 136, 51, 1)
		.background(TEX, 86, 0).title("gui.rs_ctr2.label", 0.5F);
		
		new TextField(frame, 120, 27, 8, 16, 256, text, t -> gui.sendPkt(t))
		.allowFormat().multiline();
		return gui.setComps(frame, false);
	}

}
