package net.mcreator.swordssmp.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;

public class SwordssmpCustomItemGroups {
	public static final ResourceKey<CreativeModeTab> SWORDSMP_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("swordssmp", "swordsmp_items"));
	public static CreativeModeTab SWORDSMP_TAB_INSTANCE;

	public static void register() {
		SWORDSMP_TAB_INSTANCE = FabricItemGroup.builder()
				.title(Component.literal("Swordsmp Items"))
				.icon(() -> new ItemStack(SwordssmpModItems.WIND_BLADE))
				.displayItems((parameters, output) -> {
					SwordssmpModItems.SWORDSMP_TAB_ITEMS.forEach(output::accept);
					addPotionStacks(output, SwordssmpModPotions.PV_PPOTION);
					addPotionStacks(output, SwordssmpModPotions.CORROSIVE_FIRE_POTION);
					addPotionStacks(output, SwordssmpModPotions.INSTANT_XP_POTION);
				})
				.build();

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SWORDSMP_TAB, SWORDSMP_TAB_INSTANCE);
	}

	private static void addPotionStacks(CreativeModeTab.Output output, Holder<Potion> potion) {
		output.accept(potionStack(Items.POTION, potion));
		output.accept(potionStack(Items.SPLASH_POTION, potion));
		output.accept(potionStack(Items.LINGERING_POTION, potion));
	}

	private static ItemStack potionStack(Item item, Holder<Potion> potion) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return stack;
	}
}
