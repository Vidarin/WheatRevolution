package com.vidarin.wheatrevolution.compat;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.LatheRecipe;
import com.vidarin.wheatrevolution.registry.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class LatheCategory implements IRecipeCategory<LatheRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(WheatRevolution.MODID, "lathe");
    public static final ResourceLocation TEXTURE = new ResourceLocation(WheatRevolution.MODID, "textures/gui/lathe_gui.png");

    public static final RecipeType<LatheRecipe> TYPE = new RecipeType<>(UID, LatheRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public LatheCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 80);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.LATHE_MACHINE.get()));
    }

    @Override
    public RecipeType<LatheRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.lathe");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LatheRecipe recipe, IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 33).addIngredients(recipe.getIngredients().get(0));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 33).addItemStack(recipe.getResultItem(null));
    }
}
