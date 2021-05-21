package cd4017be.rs_ctr2.container;

import static cd4017be.rs_ctr2.Content.aUTOCRAFT;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.container.SmartSlot;
import cd4017be.lib.container.slot.SlotHolo;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.*;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.tileentity.AutoCrafter;
import cd4017be.rs_ctr2.util.RefCraftingInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;


public class ContainerAutoCraft extends AdvancedContainer implements Runnable {

	final BasicInventory result = new BasicInventory(1);
	final CraftingInventory craftInv;
	final RecipeManager rm;

	public ContainerAutoCraft(int id, PlayerInventory inv, PacketBuffer pkt) {
		super(aUTOCRAFT, id, inv, StateSyncAdv.of(true, AutoCrafter.class), 0);
		BasicInventory buf = new BasicInventory(16);
		LongSupplier p = sync.longGetter("pattern", false);
		this.craftInv = new RefCraftingInventory(3, 3, buf.items,
			s -> (int)(p.getAsLong() >> (s << 2)) & 15, this
		);
		this.rm = inv.player.level.getRecipeManager();
		addSlots(buf);
	}

	public ContainerAutoCraft(int id, PlayerInventory inv, AutoCrafter tile) {
		super(aUTOCRAFT, id, inv, StateSyncAdv.of(false, tile), 0);
		this.craftInv = tile.craftInv;
		this.rm = null;
		addSlots(tile.inv);
	}

	private void addSlots(IItemHandlerModifiable bufInv) {
		for (int j = 0, k = 0; j < 4; j++)
			for (int i = 0; i < 4; i++, k++)
				addSlot(new SlotItemHandler(bufInv, k, 8 + i * 18, 16 + j * 18));
		for (int j = 0, k = 0; j < 3; j++)
			for (int i = 0; i < 3; i++, k++)
				addSlot(new SmartSlot(craftInv, k, 89 + i * 18, 34 + j * 18));
		addSlot(new SmartSlot(craftInv, 9, 152, 70));
		addSlot(new SlotHolo(result, 0, 152, 52, true, true));
		addPlayerInventory(8, 104);
	}

	@Override
	public void run() {
		result.items[0] = rm.getRecipeFor(IRecipeType.CRAFTING, craftInv, inv.player.level)
			.map(r -> r.assemble(craftInv)).orElse(ItemStack.EMPTY);
	}

	@Override
	public void setItem(int slotID, ItemStack stack) {
		if (slotID < 16 || slotID >= 27) super.setItem(slotID, stack);
	}

	public static final ResourceLocation TEX = Main.rl("textures/gui/autocraft.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerAutoCraft> setupGui(PlayerInventory inv, ITextComponent name) {
		IntSupplier p2x3 = sync.intGetter("p2", false);
		IntSupplier p1x3 = sync.intGetter("p1", false);
		
		ModularGui<ContainerAutoCraft> gui = new ModularGui<>(this, inv, name);
		GuiFrame frame = new GuiFrame(gui, 176, 186, 3)
		.background(TEX, 0, 0).title("gui.rs_ctr2.autocraft", 0.5F);
		new TextField(frame, 48, 7, 91, 17, 8,
			()-> String.format("%08X", p2x3.getAsInt()),
			t -> {
				try {gui.sendPkt((byte)0, Integer.parseInt(t, 16));}
				catch(NumberFormatException e) {}
			}
		).tooltip("gui.rs_ctr2.pattern");
		new TextField(frame, 18, 7, 148, 17, 3,
			()-> String.format("%03X", p1x3.getAsInt()),
			t -> {
				try {gui.sendPkt((byte)1, Integer.parseInt(t, 16));}
				catch(NumberFormatException e) {}
			}
		).tooltip("gui.rs_ctr2.pattern");
		new SlotIndicators(frame, 89, 34, sync.longGetter("pattern", false));
		return gui.setComps(frame, false);
	}

	@OnlyIn(Dist.CLIENT)
	private static class SlotIndicators extends GuiCompBase<GuiCompGroup> {

		final LongSupplier get;

		public SlotIndicators(GuiCompGroup parent, int x, int y, LongSupplier get) {
			super(parent, 79, 52, x, y);
			this.get = get;
		}

		@Override
		public void drawBackground(MatrixStack stack, int mx, int my, float t) {
			long pattern = get.getAsLong();
			for (int j = 0; j < 3; j++)
				for (int i = 0; i < 3; i++, pattern >>>= 4) {
					int k = (int)(pattern & 15);
					if (k == 0) continue;
					parent.drawRect(stack, x + i * 18, y + j * 18, 176 + k * 4, 0, 4, 6);
				}
			int k = (int)(pattern & 15);
			if (k != 0) parent.drawRect(stack, x + 63, y + 36, 176 + k * 4, 0, 4, 6);
			k = (int)(pattern >>> 4 & 15);
			parent.drawRect(stack, x + 63, y + 18, 176 + k * 4, 0, 4, 6);
		}

	}

}
