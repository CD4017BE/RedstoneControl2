package cd4017be.rs_ctr2.container;

import static cd4017be.rs_ctr2.Content.iTEM_FILTER;

import java.util.function.IntSupplier;

import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.container.slot.SlotHolo;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.Button;
import cd4017be.lib.gui.comp.GuiFrame;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.part.ItemFilter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author CD4017BE */
public class ContainerItemFilter extends AdvancedContainer {

	public ContainerItemFilter(int id, PlayerInventory inv, PacketBuffer buf) {
		this(id, inv, new BasicInventory(8), ItemFilter.class);
	}

	public ContainerItemFilter(int id, PlayerInventory pinv, BasicInventory inv, Object sync) {
		super(iTEM_FILTER, id, pinv, StateSyncAdv.of(sync instanceof Class, sync), 0);
		for (int i = 0; i < 8; i++)
			addSlot(new SlotHolo(inv, i, 17 + i * 18, 16, false, false));
		addPlayerInventory(8, 50);
	}

	public static final ResourceLocation TEX = Main.rl("textures/gui/item_filter.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerItemFilter> setupGui(PlayerInventory inv, ITextComponent name) {
		IntSupplier idx = sync.intGetter("idx", true);
		
		ModularGui<ContainerItemFilter> gui = new ModularGui<>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 176, 132, 8)
		.background(TEX, 0, 0).title("gui.rs_ctr2.item_filter", 0.5F);
		for (int i = 0; i < 8; i++) {
			int _i = i;
			new Button(
				frame, 18, 18, 16 + i * 18, 15,
				0, ()-> slotState(idx.getAsInt(), _i), null
			).texture(176, 0);
		}
		return gui.setComps(frame, false);
	}

	private static int slotState(int idx, int i) {
		return idx < 0 ? (idx == -1 ? 1 : 2) : (idx == i ? 1 : 0);
	}

}
