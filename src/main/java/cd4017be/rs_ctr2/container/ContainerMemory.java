package cd4017be.rs_ctr2.container;

import static cd4017be.lib.network.GuiNetworkHandler.GNH_INSTANCE;
import static cd4017be.lib.network.GuiNetworkHandler.preparePacket;
import static cd4017be.rs_ctr2.Content.mEMORY;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.function.IntSupplier;
import cd4017be.lib.container.AdvancedContainer;
import cd4017be.lib.network.StateSyncAdv;
import cd4017be.rs_ctr2.container.gui.GuiRAM;
import cd4017be.rs_ctr2.part.Memory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class ContainerMemory extends AdvancedContainer {

	private static final int[] SYNC_FORMAT = new int[66];
	static {
		Arrays.fill(SYNC_FORMAT, 4);
		SYNC_FORMAT[64] = 2;
		SYNC_FORMAT[65] = 1;
		StateSyncAdv.sequence(SYNC_FORMAT);
	}

	private final Memory part;
	private byte page;

	public ContainerMemory(int id, PlayerInventory inv, PacketBuffer pkt) {
		this(id, inv, (Memory)null);
	}

	public ContainerMemory(int id, PlayerInventory inv, Memory part) {
		super(mEMORY, id, inv, StateSyncAdv.of(part == null, SYNC_FORMAT, 0, new Object[0]), 0);
		this.part = part;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return player.isAlive() && (part == null || part.host != null);
	}

	@Override
	protected void detectChanges(BitSet chng) {
		int[] data = part.data;
		int l = data.length;
		for (int i = 0, j = page << 6; i < 64; i++, j++)
			sync.setInt(i, j < l ? data[j] : 0);
		sync.setShort(64, l);
		sync.setByte(65, page);
	}

	@Override
	protected void writeChanges(BitSet chng, PacketBuffer pkt) {
		if (chng.get(sync.objIdxOfs())) pkt.writeVarInt(0);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void readChanges(BitSet chng, PacketBuffer pkt) throws Exception {
		if (!chng.get(sync.objIdxOfs())) return;
		int l = pkt.readVarInt();
		if (l == 0) return;
		byte[] mem = new byte[l << 2];
		pkt.readBytes(mem);
		@SuppressWarnings("resource")
		Screen gui = Minecraft.getInstance().screen;
		if (gui instanceof GuiRAM) ((GuiRAM)gui).processDownload(mem);
	}

	public static final byte A_PAGE = 0, A_SET_MEM = 1, A_DOWNLOAD = 2, A_UPLOAD = 3;

	@Override
	public void handlePlayerPacket(PacketBuffer pkt, ServerPlayerEntity sender)
	throws Exception {
		int[] memory = part.data;
		switch(pkt.readByte()) {
		case A_PAGE:
			page = pkt.readByte();
			if (page <= 0) page = 0;
			else if (page >= memory.length + 63 >> 6)
				page = (byte)Math.max(0, (memory.length + 63 >> 6) - 1);
			break;
		case A_SET_MEM: {
			int i = pkt.readShort() & 0xffff, v = pkt.readByte();
			int bit = (i & 7) * 4, idx = i >> 3;
			if (idx < memory.length)
				memory[idx] = v << bit | memory[idx] & ~(15 << bit);
			break;
		}
		case A_DOWNLOAD:
			sync.clear();
			sync.set(0, null);
			sync.write(pkt = preparePacket(this));
			pkt.writeVarInt(memory.length);
			for (int i : memory) pkt.writeIntLE(i);
			GNH_INSTANCE.sendToPlayer(pkt, (ServerPlayerEntity)inv.player);
			break;
		case A_UPLOAD:
			int l = pkt.readUnsignedShort();
			if (l > memory.length) return;
			for (int i = 0; i < l; i++)
				memory[i] = pkt.readIntLE();
			Arrays.fill(memory, l, memory.length, 0);
			sender.displayClientMessage(new TranslationTextComponent("msg.rs_ctr2.import_succ"), false);
			break;
		default: return;
		}
	}

	public IntSupplier page() {
		return sync.intGetter(65, false);
	}

	public IntSupplier size() {
		return sync.intGetter(64, false);
	}

	public IntBuffer get() {
		return ((ByteBuffer)sync.buffer().clear()).asIntBuffer();
	}

}
