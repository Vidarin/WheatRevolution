package com.vidarin.wheatrevolution.main;

import com.mojang.logging.LogUtils;
import com.vidarin.wheatrevolution.gui.screen.*;
import com.vidarin.wheatrevolution.recipe.RecipeHandler;
import com.vidarin.wheatrevolution.registry.*;
import com.vidarin.wheatrevolution.render.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(WheatRevolution.MODID)
@SuppressWarnings("deprecation")
public class WheatRevolution
{
    public static final String MODID = "wheatrevolution";

    public static final Logger LOGGER = LogUtils.getLogger();

    public WheatRevolution()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeTabs.register(modEventBus);

        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);

        SoundRegistry.SOUND_EVENTS.register(modEventBus);

        BlockEntityRegistry.register(modEventBus);
        GuiRegistry.register(modEventBus);

        RecipeHandler.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info("{}{}", Config.magicNumberIntroduction, Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            MenuScreens.register(GuiRegistry.COMPRESSOR_MACHINE_MENU.get(), CompressorMachineScreen::new);
            MenuScreens.register(GuiRegistry.LATHE_MACHINE_MENU.get(), LatheMachineScreen::new);
            MenuScreens.register(GuiRegistry.ORE_FACTORY_MACHINE_MENU.get(), OreFactoryMachineScreen::new);

            ItemBlockRenderTypes.setRenderLayer(BlockRegistry.COMPRESSOR_MACHINE.get(), RenderType.translucent());
        }

        @SubscribeEvent
        public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BlockEntityRegistry.COMPRESSOR_MACHINE_ENTITY.get(), CompressorMachineRenderer::new);
            event.registerBlockEntityRenderer(BlockEntityRegistry.LATHE_MACHINE_ENTITY.get(), LatheMachineRenderer::new);
            event.registerBlockEntityRenderer(BlockEntityRegistry.ORE_FACTORY_MACHINE_ENTITY.get(), OreFactoryMachineRenderer::new);
        }
    }
}
