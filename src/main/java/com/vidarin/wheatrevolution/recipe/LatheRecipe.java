package com.vidarin.wheatrevolution.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LatheRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;
    private final ResourceLocation id;

    public LatheRecipe(NonNullList<Ingredient> inputs, ItemStack output, ResourceLocation id) {
        this.inputs = inputs;
        this.output = output;
        this.id = id;
    }

    @Override
    public boolean matches(SimpleContainer simpleContainer, Level level) {
        if (level.isClientSide()) {
            return false;
        }


        return inputs.get(0).test(simpleContainer.getItem(0));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    @Override
    public ItemStack assemble(SimpleContainer simpleContainer, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(@Nullable RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @SuppressWarnings("unused")
    public static class Type implements RecipeType<LatheRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "lathe";
    }

    @SuppressWarnings("unused")
    public static class Serializer implements RecipeSerializer<LatheRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(WheatRevolution.MODID, "lathe");

        @Override
        public LatheRecipe fromJson(ResourceLocation recipeId, JsonObject jsonObject) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(jsonObject, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            return new LatheRecipe(inputs, output, recipeId);
        }

        @Override
        public @Nullable LatheRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            inputs.replaceAll(ignored -> Ingredient.fromNetwork(buf));

            ItemStack output = buf.readItem();

            return new LatheRecipe(inputs, output, recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, LatheRecipe recipe) {
            buf.writeInt(recipe.inputs.size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buf);
            }

            buf.writeItemStack(recipe.getResultItem(null), false);
        }
    }
}
