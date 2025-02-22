package com.vidarin.wheatrevolution.registry;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.mcreator.wheat_death_of_the_universe.init.WheatdeathoftheuniverseModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WheatRevolution.MODID);

    public static final RegistryObject<CreativeModeTab> MOD_MATERIALS = TABS.register("mod_materials",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ItemRegistry.STEEL_INGOT.get()))
                    .title(Component.translatable("creativetab.mod_materials"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ItemRegistry.STEEL_INGOT.get());
                        output.accept(ItemRegistry.STEEL_NUGGET.get());
                        output.accept(BlockRegistry.STEEL_BLOCK.get());

                        output.accept(ItemRegistry.STEEL_PLATE.get());
                        output.accept(ItemRegistry.STEEL_ROD.get());
                    })
                    .build());
    public static final RegistryObject<CreativeModeTab> MOD_COMPONENTS = TABS.register("mod_components",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ItemRegistry.SIMPLE_ELECTRONIC.get()))
                    .title(Component.translatable("creativetab.mod_components"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ItemRegistry.SIMPLE_ELECTRONIC.get());
                        output.accept(BlockRegistry.STEEL_CASING.get());

                        output.accept(ItemRegistry.BASIC_PISTON.get());
                        output.accept(ItemRegistry.BASIC_MOTOR.get());
                    })
                    .build());
    public static final RegistryObject<CreativeModeTab> MOD_MACHINES = TABS.register("mod_machines",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(BlockRegistry.COMPRESSOR_MACHINE.get()))
                    .title(Component.translatable("creativetab.mod_machines"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(BlockRegistry.COMPRESSOR_MACHINE.get());
                        output.accept(BlockRegistry.LATHE_MACHINE.get());
                        output.accept(BlockRegistry.ORE_FACTORY_MACHINE.get());
                        output.accept(BlockRegistry.ASSEMBLER_MACHINE.get());
                    })
                    .build());
    public static final RegistryObject<CreativeModeTab> MOD_MISC = TABS.register("mod_misc",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(WheatdeathoftheuniverseModItems.IMMACULATE.get()))
                    .title(Component.translatable("creativetab.mod_misc"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(WheatdeathoftheuniverseModItems.ERECTRONICS.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
