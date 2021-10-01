package cd4017be.rs_ctr2.tileentity;

import static cd4017be.api.grid.port.ISignalReceiver.NOP;
import static cd4017be.api.grid.port.ISignalReceiver.TYPE_ID;
import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.SAVE;
import static cd4017be.lib.network.Sync.Type.I8;
import static cd4017be.lib.part.OrientedPart.port;
import static cd4017be.lib.tick.GateUpdater.GATE_UPDATER;
import static net.minecraft.util.Direction.SOUTH;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import cd4017be.api.grid.ExtGridPorts;
import cd4017be.api.grid.port.ISignalReceiver;
import cd4017be.lib.block.BlockTE.ITEInteract;
import cd4017be.lib.container.IUnnamedContainerProvider;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tick.IGate;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.Main;
import cd4017be.rs_ctr2.advancements.CircuitTest;
import cd4017be.rs_ctr2.container.ContainerCircuitTest;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.fml.network.NetworkHooks;


/**
 * @author CD4017BE */
public class CircuitTester extends Machine
implements IGate, IUnnamedContainerProvider, ITEInteract {

	public CircuitTest test;
	ISignalReceiver[] out = {NOP, NOP, NOP, NOP};
	@Sync(to = SAVE|GUI, type = I8)
	public int lat = 5, intv = 5;
	@Sync(to = SAVE|GUI)
	public int inX, inY, t = -1, err;
	@Sync(to = GUI)
	public short rows() {return (short)(test == null ? 0 : test.rows());}
	@Sync(to = GUI)
	public byte[] header() {return test == null ? new byte[0] : test.columns;}
	@Sync(to = GUI)
	public String name() {return test == null ? "" : test.name;}
	@Sync public byte[] perm = ByteArrays.EMPTY_ARRAY;

	public CircuitTester(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public ISignalReceiver getHandler(int port) {
		return port == 4 ? (v, r) -> inX = v
			: port == 5 ? (v, r) -> inY = v
			: null;
	}

	@Override
	public void setHandler(int port, Object handler) {
		if (port < 4) out[port] = ISignalReceiver.of(handler);
	}

	@Override
	protected void init(ExtGridPorts ports, Orientation o) {
		ports.createPort(port(o, 0x30, SOUTH, TYPE_ID), true, true);
		ports.createPort(port(o, 0x31, SOUTH, TYPE_ID), true, true);
		ports.createPort(port(o, 0x32, SOUTH, TYPE_ID), true, true);
		ports.createPort(port(o, 0x33, SOUTH, TYPE_ID), true, true);
		ports.createPort(port(o, 0x34, SOUTH, TYPE_ID), false, true);
		ports.createPort(port(o, 0x37, SOUTH, TYPE_ID), false, true);
	}

	@Override
	public boolean evaluate() {
		if (unloaded || t < 0) return false;
		int i = t - lat - 1;
		if (i >= 0 && i % intv == 0) {
			int i0 = i / intv;
			i = i0 * test.columns();
			int[] val = test.values;
			if (i >= val.length) {
				t = err == 0 ? -2 : -3;
				return false;
			}
			i = perm(i0);
			byte[] col = test.columns;
			for (int j = 0; j < col.length; j++, i++) {
				int xy;
				switch(col[j] & 0xff) {
				case 4: xy = inX; break;
				case 5: xy = inY; break;
				default: continue;
				}
				val[i + 1] = xy;
				if (val[i] != xy) err++;
			}
		}
		return true;
	}

	@Override
	public void latchOut() {
		if (t % intv == 0) {
			int i0 = t / intv;
			int i = i0 * test.columns();
			int[] val = test.values;
			if (i < val.length) {
				i = perm(i0);
				byte[] col = test.columns;
				for (int j = 0; j < col.length; j++, i++) {
					int k = col[j] & 0xff;
					if (k < 4) out[k].updateInput(val[i]);
				}
			}
		}
		t++;
		GATE_UPDATER.add(this);
	}

	public int perm(int i) {
		return (i < perm.length ? perm[i] & 0xff : i) * test.columns();
	}

	public void setTest(String id) {
		test = CircuitTest.TESTS.get(new ResourceLocation(id));
		t = -1;
		perm = ByteArrays.EMPTY_ARRAY;
	}

	public void startStop(ServerPlayerEntity sender) {
		if (test == null || t >= 0) t = -1;
		else if (t == -2) Main.CIRCUIT_TEST_CRIT.trigger(sender, test.id);
		else {
			t = 0;
			err = 0;
			if (test.shuffle) {
				int r = test.rows();
				if (perm.length != r) {
					perm = new byte[r];
					for (int i = 0; i < r; i++)
						perm[i] = (byte)i;
				}
				ArrayUtils.shuffle(perm, level.random);
			} else perm = ByteArrays.EMPTY_ARRAY;
			GATE_UPDATER.add(this);
		}
	}

	public void setInterval(int dt) {
		intv = Math.max(1, dt);
		t = -1;
	}

	public void setLatency(int dt) {
		lat = Math.max(0, dt);
		t = -1;
	}

	@Override
	public ActionResultType
	onActivated(PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (hit.getDirection() == orientation().b)
			return ActionResultType.PASS;
		if (player instanceof ServerPlayerEntity) {
			ServerPlayerEntity spe = (ServerPlayerEntity)player;
			NetworkHooks.openGui(spe, this, pkt -> {
				CircuitTest[] tests = CircuitTest.SORTED;
				Set<ResourceLocation> active = Main.CIRCUIT_TEST_CRIT.getActive(spe);
				pkt.writeVarInt(tests.length);
				for (CircuitTest test : tests) {
					pkt.writeUtf(test.id.toString());
					pkt.writeUtf(test.name);
					pkt.writeBoolean(active != null && active.contains(test.id));
				}
			});
		}
		return ActionResultType.sidedSuccess(level.isClientSide);
	}

	@Override
	public void onClicked(PlayerEntity player) {}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new ContainerCircuitTest(id, inv, this, this);
	}

	@Override
	public void storeState(CompoundNBT nbt, int mode) {
		if ((mode & SAVE) != 0 && test != null)
			nbt.putString("test", test.id.toString());
		super.storeState(nbt, mode);
	}

	@Override
	public void loadState(CompoundNBT nbt, int mode) {
		if ((mode & SAVE) != 0)
			test = CircuitTest.TESTS.get(new ResourceLocation(nbt.getString("test")));
		super.loadState(nbt, mode);
	}

}
