package com.vidarin.wheatrevolution.registry;

import com.vidarin.wheatrevolution.gui.menu.*;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GuiRegistry {
    public static final DeferredRegister<MenuType<?>> GUIS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, WheatRevolution.MODID);

    public static final RegistryObject<MenuType<CompressorMachineMenu>> COMPRESSOR_MACHINE_MENU = registerMenuType("compressor_menu", CompressorMachineMenu::new);
    public static final RegistryObject<MenuType<LatheMachineMenu>> LATHE_MACHINE_MENU = registerMenuType("lathe_menu", LatheMachineMenu::new);
    public static final RegistryObject<MenuType<OreFactoryMachineMenu>> ORE_FACTORY_MACHINE_MENU = registerMenuType("ore_factory_menu", OreFactoryMachineMenu::new);
    public static final RegistryObject<MenuType<AssemblerMachineMenu>> ASSEMBLER_MACHINE_MENU = registerMenuType("assembler_menu", AssemblerMachineMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return GUIS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        GUIS.register(eventBus);
    }
}
