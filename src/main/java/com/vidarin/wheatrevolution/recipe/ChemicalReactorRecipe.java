package com.vidarin.wheatrevolution.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vidarin.wheatrevolution.block.entity.ChemicalReactorMachineEntity;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.util.CountIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChemicalReactorRecipe implements Recipe<ChemicalReactorMachineEntity> {
    private final NonNullList<CountIngredient> itemInputs;
    private final NonNullList<FluidStack> fluidInputs;
    private final ItemStack itemOutput;
    private final FluidStack fluidOutput;
    private final ResourceLocation id;
    private final int time;

    public ChemicalReactorRecipe(NonNullList<CountIngredient> itemInputs, NonNullList<FluidStack> fluidInputs, ItemStack itemOutput, FluidStack fluidOutput, ResourceLocation id, int time) {
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.itemOutput = itemOutput;
        this.fluidOutput = fluidOutput;
        this.id = id;
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public NonNullList<CountIngredient> getItemInputs() {
        return itemInputs;
    }

    public NonNullList<FluidStack> getFluidInputs() {
        return fluidInputs;
    }

    public FluidStack getFluidOutput() {
        return fluidOutput.copy();
    }

    @Override
    public boolean matches(ChemicalReactorMachineEntity reactor, Level level) {
        if (level.isClientSide()) {
            return false;
        }
        if (!itemInputs.isEmpty()) {
            for (CountIngredient ci : itemInputs) {
                int totalCount = 0;
                for (int slot = 0; slot < 2; slot++) {
                    ItemStack stack = reactor.getInventory().getStackInSlot(slot);
                    if (ci.ingredient().test(stack)) {
                        totalCount += stack.getCount();
                    }
                }
                if (totalCount < ci.count()) {
                    return false;
                }
            }
        }
        if (!fluidInputs.isEmpty()) {
            for (FluidStack required : fluidInputs) {
                boolean found = false;
                for (FluidStack tankFluid : reactor.getFluidTanks()) {
                    if (tankFluid.isFluidEqual(required) && tankFluid.getAmount() >= required.getAmount()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (CountIngredient ci : itemInputs) {
            ingredients.add(ci.ingredient());
        }
        return ingredients;
    }

    @Override
    public ItemStack assemble(ChemicalReactorMachineEntity reactor, RegistryAccess registryAccess) {
        return itemOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(@Nullable RegistryAccess registryAccess) {
        return itemOutput.copy();
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
    public static class Type implements RecipeType<ChemicalReactorRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "chemical_reactor";
    }

    @SuppressWarnings("unused")
    public static class Serializer implements RecipeSerializer<ChemicalReactorRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(WheatRevolution.MODID, "chemical_reactor");

        @Override
        public ChemicalReactorRecipe fromJson(ResourceLocation recipeId, JsonObject jsonObject) {
            ItemStack outputItem = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "item_output"));
            // fluid_output is an array: first element fluid id, second element amount
            JsonObject fluidOutputObject = GsonHelper.getAsJsonObject(jsonObject, "fluid_output");
            Fluid fluidType = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidOutputObject.get("fluid").getAsString()));
            int fluidAmount = fluidOutputObject.get("amount").getAsInt();
            FluidStack outputFluid = new FluidStack(Objects.requireNonNull(fluidType), fluidAmount);
            int time = GsonHelper.getAsInt(jsonObject, "time", 100);

            JsonArray itemInputsArray = GsonHelper.getAsJsonArray(jsonObject, "item_inputs");
            NonNullList<CountIngredient> itemInputs = NonNullList.create();
            for (int i = 0; i < itemInputsArray.size(); i++) {
                JsonObject obj = itemInputsArray.get(i).getAsJsonObject();
                Ingredient ingredient = Ingredient.fromJson(obj.get("ingredient"));
                int count = GsonHelper.getAsInt(obj, "count", 1);
                itemInputs.add(new CountIngredient(ingredient, count));
            }

            JsonArray fluidInputsArray = GsonHelper.getAsJsonArray(jsonObject, "fluid_inputs");
            NonNullList<FluidStack> fluidInputs = NonNullList.create();
            for (int i = 0; i < fluidInputsArray.size(); i++) {
                JsonObject obj = fluidInputsArray.get(i).getAsJsonObject();
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(obj, "fluid")));
                int amount = GsonHelper.getAsInt(obj, "amount", 1000);
                if (fluid != null && amount > 0)
                    fluidInputs.add(new FluidStack(fluid, amount));
            }

            return new ChemicalReactorRecipe(itemInputs, fluidInputs, outputItem, outputFluid, recipeId, time);
        }

        @Override
        public @Nullable ChemicalReactorRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
            int itemInputSize = buf.readInt();
            NonNullList<CountIngredient> itemInputs = NonNullList.create();
            for (int i = 0; i < itemInputSize; i++) {
                int count = buf.readInt();
                Ingredient ingredient = Ingredient.fromNetwork(buf);
                itemInputs.add(new CountIngredient(ingredient, count));
            }

            int fluidInputSize = buf.readInt();
            NonNullList<FluidStack> fluidInputs = NonNullList.create();
            for (int i = 0; i < fluidInputSize; i++) {
                fluidInputs.add(FluidStack.readFromPacket(buf));
            }

            ItemStack outputItem = buf.readItem();
            FluidStack outputFluid = FluidStack.readFromPacket(buf);
            int time = buf.readInt();

            return new ChemicalReactorRecipe(itemInputs, fluidInputs, outputItem, outputFluid, recipeId, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ChemicalReactorRecipe recipe) {
            buf.writeInt(recipe.itemInputs.size());
            for (CountIngredient ci : recipe.itemInputs) {
                buf.writeInt(ci.count());
                ci.ingredient().toNetwork(buf);
            }

            buf.writeInt(recipe.fluidInputs.size());
            for (FluidStack fluid : recipe.fluidInputs) {
                fluid.writeToPacket(buf);
            }

            buf.writeItemStack(recipe.itemOutput, false);
            recipe.fluidOutput.writeToPacket(buf);
            buf.writeInt(recipe.time);
        }
    }
}

