package com.vidarin.wheatrevolution.datagen.loot;

import com.vidarin.wheatrevolution.registry.BlockRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(BlockRegistry.STEEL_BLOCK.get());
        dropSelf(BlockRegistry.STEEL_CASING.get());
        dropSelf(BlockRegistry.COMPRESSOR_MACHINE.get());
        dropSelf(BlockRegistry.LATHE_MACHINE.get());
        dropSelf(BlockRegistry.ORE_FACTORY_MACHINE.get());
        dropSelf(BlockRegistry.ASSEMBLER_MACHINE.get());
        dropSelf(BlockRegistry.CHEMICAL_REACTOR_MACHINE.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return BlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }

    private void dropItem(Block block, Item item) {
        this.add(block,
                blk -> createSilkTouchDispatchTable(block,
                        this.applyExplosionDecay(block, LootItem.lootTableItem(item))));
    }
}
