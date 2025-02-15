package com.vidarin.wheatrevolution.registry;

import com.vidarin.wheatrevolution.block.entity.CompressorMachineEntity;
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

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
