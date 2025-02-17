package com.vidarin.wheatrevolution.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vidarin.wheatrevolution.gui.menu.OreFactoryMachineMenu;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OreFactoryMachineScreen extends AbstractContainerScreen<OreFactoryMachineMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(WheatRevolution.MODID, "textures/gui/ore_factory_gui.png");

    public OreFactoryMachineScreen(OreFactoryMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y, 0);
        renderProgressArrow(guiGraphics, x, y, 1);
        renderProgressArrow(guiGraphics, x, y, 2);
        renderProgressArrow(guiGraphics, x, y, 3);

        renderHeatBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y, int processId) {
        if (menu.isCrafting(processId))
            guiGraphics.blit(TEXTURE, x + 37 + (30 * processId), y + 34, 176, 0, 8, menu.getProgress(processId));
        if (menu.isCrafting(processId)) WheatRevolution.LOGGER.info("rendered progress arrow!");
    }

    private void renderHeatBar(GuiGraphics guiGraphics, int x, int y) {
        if (menu.getHeatLevel() > 0) {
            guiGraphics.blit(TEXTURE, x + 159, y + 10, 190, 0, 4, menu.getHeatLevel());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
