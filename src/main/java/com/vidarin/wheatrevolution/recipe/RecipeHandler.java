package com.vidarin.wheatrevolution.recipe;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class RecipeHandler {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, WheatRevolution.MODID);

    public static final RegistryObject<RecipeSerializer<CompressorRecipe>> COMPRESSOR_SERIALIZER =
            SERIALIZERS.register("compressing", () -> CompressorRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<LatheRecipe>> LATHE_SERIALIZER =
            SERIALIZERS.register("lathe", () -> LatheRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<AssemblerRecipe>> ASSEMBLER_SERIALIZER =
            SERIALIZERS.register("assembling", () -> AssemblerRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ChemicalReactorRecipe>> CHEMICAL_REACTOR_SERIALIZER =
            SERIALIZERS.register("chemical_reactor", () -> ChemicalReactorRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
