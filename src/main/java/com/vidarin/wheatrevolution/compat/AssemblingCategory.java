package com.vidarin.wheatrevolution.compat;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.AssemblerRecipe;
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

public class AssemblingCategory implements IRecipeCategory<AssemblerRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(WheatRevolution.MODID, "assembling");
    public static final ResourceLocation TEXTURE = new ResourceLocation(WheatRevolution.MODID, "textures/gui/assembler_gui.png");

    public static final RecipeType<AssemblerRecipe> TYPE = new RecipeType<>(UID, AssemblerRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public AssemblingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 80);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.ASSEMBLER_MACHINE.get()));
    }

    @Override
    public RecipeType<AssemblerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.assembler");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AssemblerRecipe recipe, IFocusGroup focusGroup) {
        int listLength = recipe.getIngredients().size();

        if (listLength >= 1)
            builder.addSlot(RecipeIngredientRole.INPUT, 12, 22).addItemStack(getStackForSlot(0, recipe));

        if (listLength >= 2)
            builder.addSlot(RecipeIngredientRole.INPUT, 32, 22).addItemStack(getStackForSlot(1, recipe));

        if (listLength >= 3)
            builder.addSlot(RecipeIngredientRole.INPUT, 52, 22).addItemStack(getStackForSlot(2, recipe));

        if (listLength >= 4)
            builder.addSlot(RecipeIngredientRole.INPUT, 12, 42).addItemStack(getStackForSlot(3, recipe));

        if (listLength >= 5)
            builder.addSlot(RecipeIngredientRole.INPUT, 32, 42).addItemStack(getStackForSlot(4, recipe));

        if (listLength >= 6)
            builder.addSlot(RecipeIngredientRole.INPUT, 52, 42).addItemStack(getStackForSlot(5, recipe));


        builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 33).addItemStack(recipe.getResultItem(null));
    }

    private ItemStack getStackForSlot(int ingredient, AssemblerRecipe recipe) {
        return new ItemStack(
                recipe.getIngredients().get(ingredient).getItems()[0].getItem(),
                recipe.getInputs().get(ingredient).count()
        );
    }
}
