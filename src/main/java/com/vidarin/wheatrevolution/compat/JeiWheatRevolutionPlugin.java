package com.vidarin.wheatrevolution.compat;


import com.vidarin.wheatrevolution.gui.screen.CompressorMachineScreen;
import com.vidarin.wheatrevolution.gui.screen.LatheMachineScreen;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.CompressorRecipe;
import com.vidarin.wheatrevolution.recipe.LatheRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
@SuppressWarnings("unused")
public class JeiWheatRevolutionPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(WheatRevolution.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CompressingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new LatheCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<CompressorRecipe> compressorRecipes = recipeManager.getAllRecipesFor(CompressorRecipe.Type.INSTANCE);
        registration.addRecipes(CompressingCategory.TYPE, compressorRecipes);
        List<LatheRecipe> latheRecipes = recipeManager.getAllRecipesFor(LatheRecipe.Type.INSTANCE);
        registration.addRecipes(LatheCategory.TYPE, latheRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
       registration.addRecipeClickArea(CompressorMachineScreen.class, 80, 30, 20, 27,
               CompressingCategory.TYPE);
        registration.addRecipeClickArea(LatheMachineScreen.class, 51, 32, 55, 17,
                LatheCategory.TYPE);
    }
}
