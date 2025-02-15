package com.vidarin.wheatrevolution.datagen;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, WheatRevolution.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        itemGenerated(ItemRegistry.STEEL_INGOT);
        itemGenerated(ItemRegistry.STEEL_NUGGET);
        itemGenerated(ItemRegistry.STEEL_PLATE);
    }

    private ItemModelBuilder itemGenerated(RegistryObject<Item> item) {
        return withExistingParent(
                item.getId().getPath(),
                new ResourceLocation("item/generated")).texture(
                        "layer0",
                             new ResourceLocation(WheatRevolution.MODID, "item/" + item.getId().getPath())
        );
    }
}
