package cd4017be.rs_ctr2.part;

import static cd4017be.rs_ctr2.Content.comp_in;

import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr2.api.gate.ports.ISignalReceiver;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class ComparatorIn extends SignalGate {

	public ComparatorIn() {
		super(1);
	}

	public void set(int pos, Orientation orient) {
		super.set(pos, orient);
		setBounds(pos, pos);
		setPort(0, pos, Direction.SOUTH, ISignalReceiver.TYPE_ID);
	}

	@Override
	public Item item() {
		return comp_in;
	}

	@Override
	public Object getHandler(int port) {
		return null;
	}

	@Override
	public void onTEChange(World world, BlockPos pos, Direction dir) {
		if (dir == orient.b.getOpposite()) update();
	}

	@Override
	public boolean evaluate() {
		active = false;
		if (host == null) return false;
		BlockPos pos = host.pos().relative(orient.b.getOpposite());
		World world = host.world();
		return state != (state =
			world.getBlockState(pos).getAnalogOutputSignal(world, pos)
		);
	}

}