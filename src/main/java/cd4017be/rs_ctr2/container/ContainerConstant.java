package cd4017be.rs_ctr2.container;

import static cd4017be.rs_ctr2.Content.cONSTANT;

import java.util.function.IntSupplier;

import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.GuiFrame;
import cd4017be.lib.gui.comp.TextField;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.part.Constant;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class ContainerConstant extends AdvancedContainer {

	public ContainerConstant(int id, PlayerInventory inv, PacketBuffer buf) {
		super(cONSTANT, id, inv, StateSyncAdv.of(true, Constant.class), 0);
	}

	public ContainerConstant(int id, PlayerInventory inv, Constant part) {
		super(cONSTANT, id, inv, StateSyncAdv.of(false, part), 0);
	}

	public static final ResourceLocation TEX = Main.rl("textures/gui/parts.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerConstant> setupGui(PlayerInventory inv, ITextComponent name) {
		IntSupplier value = sync.intGetter("value", true);
		
		ModularGui<ContainerConstant> gui = new ModularGui<>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 86, 42, 2)
		.background(TEX, 0, 0).title("gui.rs_ctr2.constant", 0.5F);
		
		new TextField(frame, 70, 7, 10, 17, 11,
			()-> Integer.toString(value.getAsInt()),
			t -> {
				try { gui.sendPkt(Integer.parseInt(t)); }
				catch (NumberFormatException e) {}
			}
		);
		new TextField(frame, 52, 7, 28, 27, 8,
			()-> String.format("%08X", value.getAsInt()),
			t -> {
				try { gui.sendPkt((int)Long.parseLong(t, 16)); }
				catch(NumberFormatException e) {}
			}
		);
		return gui.setComps(frame, false);
	}

}
