package com.vidarin.wheatrevolution.registry;

import com.vidarin.wheatrevolution.block.entity.*;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WheatRevolution.MODID);

    public static final RegistryObject<BlockEntityType<CompressorMachineEntity>> COMPRESSOR_MACHINE_ENTITY = BLOCK_ENTITIES.register(
      "compressor_block_entity",
            () -> BlockEntityType.Builder.of(CompressorMachineEntity::new, BlockRegistry.COMPRESSOR_MACHINE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<LatheMachineEntity>> LATHE_MACHINE_ENTITY = BLOCK_ENTITIES.register(
      "lathe_machine_entity",
            () -> BlockEntityType.Builder.of(LatheMachineEntity::new, BlockRegistry.LATHE_MACHINE.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<OreFactoryMachineEntity>> ORE_FACTORY_MACHINE_ENTITY = BLOCK_ENTITIES.register(
            "ore_factory_block_entity",
            () -> BlockEntityType.Builder.of(OreFactoryMachineEntity::new, BlockRegistry.ORE_FACTORY_MACHINE.get()).build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
