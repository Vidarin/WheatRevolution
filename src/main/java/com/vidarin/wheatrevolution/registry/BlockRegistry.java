package com.vidarin.wheatrevolution.registry;

import com.vidarin.wheatrevolution.block.*;
import com.vidarin.wheatrevolution.item.BlockItemDescription;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WheatRevolution.MODID);

    /* SIMPLE */
    public static final RegistryObject<Block> STEEL_BLOCK = registerBlockSimple("steel_block",
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(6.0F, 8.0F));

    public static final RegistryObject<Block> STEEL_CASING = registerBlockSimple("steel_casing",
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(7.0F, 9.0F));

    /* MACHINES */
    public static final RegistryObject<Block> COMPRESSOR_MACHINE = registerBlock("compressor",
            () -> new CompressorMachineBlock(BlockBehaviour.Properties.copy(STEEL_BLOCK.get()).noOcclusion()));

    public static final RegistryObject<Block> LATHE_MACHINE = registerBlock("lathe",
            () -> new LatheMachineBlock(BlockBehaviour.Properties.copy(STEEL_BLOCK.get()).noOcclusion()));

    public static final RegistryObject<Block> ORE_FACTORY_MACHINE = BLOCKS.register("ore_factory",
            () -> new OreFactoryMachineBlock(BlockBehaviour.Properties.copy(STEEL_BLOCK.get()).noOcclusion()));
    public static final RegistryObject<Item> ORE_FACTORY_ITEM = ItemRegistry.ITEMS.register("ore_factory",
            () -> new BlockItemDescription(ORE_FACTORY_MACHINE.get(), new Item.Properties(), "ore_factory"));

    public static final RegistryObject<Block> ASSEMBLER_MACHINE = registerBlock("assembler",
            () -> new AssemblerMachineBlock(BlockBehaviour.Properties.copy(STEEL_BLOCK.get()).noOcclusion()));

    /* OTHER */
    public static final RegistryObject<Block> MODEL_BLOCK = registerBlock("model_block", ModelBlock::new);

    private static RegistryObject<Block> registerBlockSimple(String name, BlockBehaviour.Properties properties) {
        return registerBlock(name, () -> new Block(properties));
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> object = BLOCKS.register(name, block);
        registerBlockItem(name, object);
        return object;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
