package com.vidarin.wheatrevolution.datagen;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.registry.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, WheatRevolution.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        // Tool levels
        this.tag(BlockTags.NEEDS_STONE_TOOL).add(
                BlockRegistry.STEEL_BLOCK.get()
        );

        this.tag(BlockTags.NEEDS_IRON_TOOL).add(
                BlockRegistry.STEEL_CASING.get(),

                BlockRegistry.COMPRESSOR_MACHINE.get(),
                BlockRegistry.LATHE_MACHINE.get(),
                BlockRegistry.ORE_FACTORY_MACHINE.get()
        );

        this.tag(BlockTags.NEEDS_DIAMOND_TOOL);

        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL);


        // Mineables
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                BlockRegistry.STEEL_BLOCK.get(),
                BlockRegistry.STEEL_CASING.get(),

                BlockRegistry.COMPRESSOR_MACHINE.get(),
                BlockRegistry.LATHE_MACHINE.get(),
                BlockRegistry.ORE_FACTORY_MACHINE.get()
        );

        this.tag(BlockTags.MINEABLE_WITH_AXE);

        this.tag(BlockTags.MINEABLE_WITH_SHOVEL);

        this.tag(BlockTags.MINEABLE_WITH_HOE);
    }
}
