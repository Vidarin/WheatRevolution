package com.vidarin.wheatrevolution.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.util.CountIngredient;
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

public class AssemblerRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<CountIngredient> inputs;
    private final ItemStack output;
    private final ResourceLocation id;
    private final int time;

    public AssemblerRecipe(NonNullList<CountIngredient> inputs, ItemStack output, ResourceLocation id, int time) {
        this.inputs = inputs;
        this.output = output;
        this.id = id;
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public NonNullList<CountIngredient> getInputs() {
        return inputs;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        if (level.isClientSide()) {
            return false;
        }
        for (CountIngredient countIngredient : inputs) {
            int totalCount = 0;
            for (int slot = 0; slot < 6; slot++) {
                ItemStack stack = container.getItem(slot);
                if (countIngredient.ingredient().test(stack)) {
                    totalCount += stack.getCount();
                }
            }
            if (totalCount < countIngredient.count()) {
                return false;
            }
        }
        return true;
    }


    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (CountIngredient countIngredient : inputs) {
            ingredients.add(countIngredient.ingredient());
        }
        return ingredients;
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
    public static class Type implements RecipeType<AssemblerRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "assembling";
    }

    @SuppressWarnings("unused")
    public static class Serializer implements RecipeSerializer<AssemblerRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(WheatRevolution.MODID, "assembling");

        @Override
        public AssemblerRecipe fromJson(ResourceLocation recipeId, JsonObject jsonObject) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
            int time = GsonHelper.getAsInt(jsonObject, "time", 100);

            JsonArray ingredientsArray = GsonHelper.getAsJsonArray(jsonObject, "ingredients");
            NonNullList<CountIngredient> inputs = NonNullList.create();
            for (int i = 0; i < ingredientsArray.size(); i++) {
                JsonObject ingredientObj = ingredientsArray.get(i).getAsJsonObject();
                Ingredient ingredient = Ingredient.fromJson(ingredientObj.get("ingredient"));
                int count = GsonHelper.getAsInt(ingredientObj, "count", 1);
                inputs.add(new CountIngredient(ingredient, count));
            }
            return new AssemblerRecipe(inputs, output, recipeId, time);
        }

        @Override
        public @Nullable AssemblerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
            int size = buf.readInt();
            NonNullList<CountIngredient> inputs = NonNullList.create();
            for (int i = 0; i < size; i++) {
                int count = buf.readInt();
                Ingredient ingredient = Ingredient.fromNetwork(buf);
                inputs.add(new CountIngredient(ingredient, count));
            }
            ItemStack output = buf.readItem();
            int time = buf.readInt();
            return new AssemblerRecipe(inputs, output, recipeId, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AssemblerRecipe recipe) {
            buf.writeInt(recipe.inputs.size());
            for (CountIngredient countIngredient : recipe.inputs) {
                buf.writeInt(countIngredient.count());
                countIngredient.ingredient().toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(null), false);
            buf.writeInt(recipe.time);
        }
    }
}
