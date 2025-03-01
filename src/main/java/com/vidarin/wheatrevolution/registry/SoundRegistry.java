package com.vidarin.wheatrevolution.registry;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, WheatRevolution.MODID);

    public static final RegistryObject<SoundEvent> COMPRESSOR_ACTIVE_SOUND = registerSound("compressor");

    public static final RegistryObject<SoundEvent> LATHE_START_SOUND = registerSound("lathe_start");
    public static final RegistryObject<SoundEvent> LATHE_STOP_SOUND = registerSound("lathe_stop");
    public static final RegistryObject<SoundEvent> LATHE_ACTIVE_SOUND = registerSound("lathe_running");

    public static final RegistryObject<SoundEvent> ASSEMBLER_SOUND = registerSound("assembler"); // Credits to GregTech

    public static final RegistryObject<SoundEvent> CHEMICAL_REACTOR_SOUND = registerSound("chemical_reactor");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(WheatRevolution.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
