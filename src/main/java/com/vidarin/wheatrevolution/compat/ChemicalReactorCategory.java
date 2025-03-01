package com.vidarin.wheatrevolution.compat;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.ChemicalReactorRecipe;
import com.vidarin.wheatrevolution.registry.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
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

public class ChemicalReactorCategory implements IRecipeCategory<ChemicalReactorRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(WheatRevolution.MODID, "chemical_reactor");
    public static final ResourceLocation TEXTURE = new ResourceLocation(WheatRevolution.MODID, "textures/gui/chemical_reactor_gui.png");

    public static final RecipeType<ChemicalReactorRecipe> TYPE = new RecipeType<>(UID, ChemicalReactorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ChemicalReactorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 80);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.CHEMICAL_REACTOR_MACHINE.get()));
    }

    @Override
    public RecipeType<ChemicalReactorRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.chemical_reactor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalReactorRecipe recipe, IFocusGroup focusGroup) {
        if (!recipe.getIngredients().isEmpty())
            builder.addSlot(RecipeIngredientRole.INPUT, 20, 25).addItemStack(
                    new ItemStack(recipe.getIngredients().get(0).getItems()[0].getItem(), recipe.getItemInputs().get(0).count())
            );

        if (recipe.getIngredients().size() >= 2)
            builder.addSlot(RecipeIngredientRole.INPUT, 40, 25).addItemStack(
                    new ItemStack(recipe.getIngredients().get(1).getItems()[0].getItem(), recipe.getItemInputs().get(1).count())
            );

        if (!recipe.getFluidInputs().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 20, 45)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.getFluidInputs().get(0))
                    .addTooltipCallback(((iRecipeSlotView, tooltip) ->
                            tooltip.add(Component.literal(recipe.getFluidInputs().get(0).getAmount() + "mb"))));
        }

        if (recipe.getFluidInputs().size() >= 2) {
            builder.addSlot(RecipeIngredientRole.INPUT, 40, 45)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.getFluidInputs().get(1))
                    .addTooltipCallback(((iRecipeSlotView, tooltip) ->
                            tooltip.add(Component.literal(recipe.getFluidInputs().get(1).getAmount() + "mb"))));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 36).addItemStack(recipe.getResultItem(null));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 36)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.getFluidOutput())
                .addTooltipCallback(((iRecipeSlotView, tooltip) ->
                        tooltip.add(Component.literal(recipe.getFluidOutput().getAmount() + "mb"))));
    }
}
