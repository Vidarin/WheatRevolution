package com.vidarin.wheatrevolution.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vidarin.wheatrevolution.gui.menu.LatheMachineMenu;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LatheMachineScreen extends AbstractContainerScreen<LatheMachineMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(WheatRevolution.MODID, "textures/gui/lathe_gui.png");

    public LatheMachineScreen(LatheMachineMenu menu, Inventory inventory, Component title) {
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

        renderEnergyBar(guiGraphics, x, y);
        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        if (menu.getScaledEnergy() > 0)
            guiGraphics.blit(TEXTURE, x + 165, y + 10 + (64 - menu.getScaledEnergy()), 190, 0, 4, menu.getScaledEnergy());
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting())
            guiGraphics.blit(TEXTURE, x + 51, y + 34, 176, 64, menu.getProgress(), 14);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
