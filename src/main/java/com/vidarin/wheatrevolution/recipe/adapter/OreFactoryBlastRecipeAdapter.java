package com.vidarin.wheatrevolution.recipe.adapter;

import com.vidarin.wheatrevolution.recipe.OreFactoryRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class OreFactoryBlastRecipeAdapter extends OreFactoryRecipe {
    private final BlastingRecipe blastingRecipe;

    public OreFactoryBlastRecipeAdapter(BlastingRecipe recipe) {
        super(recipe.getIngredients(), recipe.getResultItem(null), recipe.getId());
        this.blastingRecipe = recipe;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean matches(SimpleContainer container, Level level) {
        return blastingRecipe.matches(container, level);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return blastingRecipe.assemble(container, registryAccess);
    }

    @Override
    public @NotNull ItemStack getResultItem(@Nullable RegistryAccess registryAccess) {
        return blastingRecipe.getResultItem(null);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return blastingRecipe.getSerializer();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return blastingRecipe.getType();
    }
}
