package cd4017be.rs_ctr2.advancements;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import cd4017be.rs_ctr2.Main;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;


/**
 * @author CD4017BE */
public class CircuitTestTrigger extends AbstractCriterionTrigger<CircuitTestTrigger.Instance> {

	private static final ResourceLocation ID = Main.rl("circuit_test");
	private final Map<PlayerAdvancements, Set<ResourceLocation>> active = Maps.newIdentityHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> listener) {
		active.computeIfAbsent(advancements, k -> Sets.newHashSet())
		.add(listener.getTriggerInstance().test);
		super.addPlayerListener(advancements, listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<Instance> listener) {
		active.computeIfPresent(advancements, (k, v)-> {
			v.remove(listener.getTriggerInstance().test);
			return v.isEmpty() ? null : v;
		});
		super.removePlayerListener(advancements, listener);
	}

	@Override
	public void removePlayerListeners(PlayerAdvancements advancements) {
		active.remove(advancements);
		super.removePlayerListeners(advancements);
	}

	@Override
	protected Instance createInstance(
		JsonObject obj, AndPredicate pred, ConditionArrayParser cond
	) {
		ResourceLocation test = obj.has("test")
			? new ResourceLocation(JSONUtils.getAsString(obj, "test")) : null;
		return new Instance(pred, test);
	}

	public void trigger(ServerPlayerEntity player, ResourceLocation test) {
		trigger(player, i -> i.matches(test));
	}

	public Set<ResourceLocation> getActive(ServerPlayerEntity player) {
		return active.get(player.getAdvancements());
	}


	public static class Instance extends CriterionInstance {

		public final ResourceLocation test;

		public Instance(AndPredicate pred, ResourceLocation test) {
			super(ID, pred);
			this.test = test;
		}

		public boolean matches(ResourceLocation test) {
			return this.test == null || this.test.equals(test);
		}

		@Override
		public JsonObject serializeToJson(ConditionArraySerializer cond) {
			JsonObject obj = super.serializeToJson(cond);
			if (test != null) obj.addProperty("test", test.toString());
			return obj;
		}

	}

}
