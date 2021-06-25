package cd4017be.rs_ctr2.container;

import static cd4017be.rs_ctr2.Content.iTEM_PLACER;

import java.util.function.IntSupplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.container.slot.HidableSlot;
import cd4017be.lib.container.slot.SlotArmor;
import cd4017be.lib.gui.ModularGui;
import cd4017be.lib.gui.comp.*;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.tileentity.ItemPlacer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * @author CD4017BE */
public class ContainerItemPlacer extends AdvancedContainer {

	final ItemPlacer tile;

	public ContainerItemPlacer(int id, PlayerInventory inv, PacketBuffer pkt) {
		this(id, inv, (ItemPlacer)null);
	}

	public ContainerItemPlacer(int id, PlayerInventory inv, ItemPlacer tile) {
		super(
			iTEM_PLACER, id, inv,
			tile == null ? StateSyncAdv.of(true, ItemPlacer.class)
				: StateSyncAdv.of(false, tile), 0
		);
		this.tile = tile;
		inv = tile != null ? tile.getPlayer().inventory : new PlayerInventory(inv.player);
		int x = 26, y = 26;
		for (int i = 0; i < 9; i++)
			addSlot(new HidableSlot(inv, i, x + i * 18, y + 58));
		for (int i = 0; i < 3; i++) 
			for (int j = 0; j < 9; j++)
				addSlot(new HidableSlot(inv, i * 9 + j + 9, x + j * 18, y + i * 18));
		for (int i = 0; i < 4; i++)
			addSlot(new SlotArmor(inv, i + 36, x - 18, y - i * 18 + 36, EquipmentSlotType.values()[i + 2]));
		addSlot(new SlotArmor(inv, 40, x - 18, y + 58, EquipmentSlotType.OFFHAND));
		addPlayerInventory(26, 124, true);
	}

	@Override
	public ItemStack clicked(int s, int b, ClickType m, PlayerEntity player) {
		if (s < 36 && m == ClickType.PICKUP && tile != null && !player.inventory.getCarried().isEmpty())
			tile.resetRestock(s);
		return super.clicked(s, b, m, player);
	}

	private static final ResourceLocation TEX = Main.rl("textures/gui/item_placer.png");

	@OnlyIn(Dist.CLIENT)
	public ModularGui<ContainerItemPlacer> setupGui(PlayerInventory inv, ITextComponent title) {
		IntSupplier aim = sync.intGetter("aim0", false);
		ModularGui<ContainerItemPlacer> gui = new ModularGui<>(this, inv, title);
		GuiFrame frame = new GuiFrame(gui, 194, 206, 8)
		.background(TEX, 0, 0).title("gui.rs_ctr2.item_placer", 0.45F);
		
		new Tooltip(frame, 16, 16, 152, 8, "gui.rs_ctr2.item_placer.look", ()-> {
			int i = aim.getAsInt();
			return new Object[] {i >> 8 & 3, i >> 10 & 3};
		});
		new Tooltip(frame, 16, 16, 170, 8, "gui.rs_ctr2.item_placer.aim", ()-> {
			int i = aim.getAsInt();
			return new Object[] {i >> 16 & 15, i >> 20 & 15};
		});
		new Button(frame, 16, 8, 134, 16, 2, ()-> aim.getAsInt() >> 12 & 1, null)
		.texture(224, 0).tooltip("gui.rs_ctr2.item_placer.sneak#");
		new Button(frame, 16, 8, 98, 16, 2, ()-> aim.getAsInt() >> 14 & 1, null)
		.texture(224, 16).tooltip("gui.rs_ctr2.item_placer.side#");
		new Button(frame, 16, 8, 80, 16, 2, ()-> aim.getAsInt() >> 15 & 1, null)
		.texture(224, 32).tooltip("gui.rs_ctr2.item_placer.air#");
		new Button(frame, 16, 8, 26, 16, 2, ()-> aim.getAsInt() >> 7 & 1, null)
		.texture(224, 48).tooltip("gui.rs_ctr2.item_placer.restock#");
		new Button(frame, 16, 16, 152, 8, 16, ()-> aim.getAsInt() >> 8 & 15, null)
		.texture(240, 0);
		new SlotMarker(frame, 25, 25, aim);
		return gui.setComps(frame, false);
	}

	@OnlyIn(Dist.CLIENT)
	private static class SlotMarker extends GuiCompBase<GuiCompGroup> {

		private final IntSupplier aim;

		public SlotMarker(GuiCompGroup parent, int x, int y, IntSupplier aim) {
			super(parent, 162, 76, x, y);
			this.aim = aim;
		}

		@Override
		public void drawBackground(MatrixStack stack, int mx, int my, float t) {
			int i = aim.getAsInt(), j = i >> 16 & 0xff;
			parent.drawRect(stack, x + 144 + (j & 15), y - 3 - (j >> 4), 212, 0, 3, 3);
			if ((i &= 0x3f) >= 36)
				parent.drawRect(stack, x - 18, y + 58, 194, 0, 18, 18);
			else parent.drawRect(stack, x + i % 9 * 18, y + (i < 9 ? 58 : (i / 9 - 1) * 18), 194, 0, 18, 18);
		}

	}

}
