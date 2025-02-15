package com.vidarin.wheatrevolution.registry;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WheatRevolution.MODID);

    public static final RegistryObject<Item> STEEL_INGOT = registerSimple("steel_ingot");
    public static final RegistryObject<Item> STEEL_NUGGET = registerSimple("steel_nugget");
    public static final RegistryObject<Item> STEEL_PLATE = registerSimple("steel_plate");

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static RegistryObject<Item> registerSimple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }
}
