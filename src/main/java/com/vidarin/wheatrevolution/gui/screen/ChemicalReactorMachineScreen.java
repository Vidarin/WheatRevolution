package com.vidarin.wheatrevolution.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vidarin.wheatrevolution.gui.menu.ChemicalReactorMachineMenu;
import com.vidarin.wheatrevolution.gui.slot.FluidSlot;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Optional;

public class ChemicalReactorMachineScreen extends AbstractContainerScreen<ChemicalReactorMachineMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(WheatRevolution.MODID, "textures/gui/chemical_reactor_gui.png");

    public ChemicalReactorMachineScreen(ChemicalReactorMachineMenu menu, Inventory inventory, Component title) {
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

        renderFluidTankContents(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderFluidTankContents(GuiGraphics guiGraphics, int x, int y) {
        FluidTank inputTank1 = this.menu.blockEntity.getFluidTank(0);
        FluidTank inputTank2 = this.menu.blockEntity.getFluidTank(1);
        FluidTank outputTank = this.menu.blockEntity.getFluidTank(2);

        assert inputTank1 != null && inputTank2 != null && outputTank != null;

        FluidStack inputStack1 = inputTank1.getFluid();
        FluidStack inputStack2 = inputTank2.getFluid();
        FluidStack outputStack = outputTank.getFluid();
        for (int i = 0; i < 3; i++) {
            FluidStack fluid = switch (i) {
                case 0 -> inputStack1;
                case 1 -> inputStack2;
                case 2 -> outputStack;
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };
            FluidTank tank = switch (i) {
                case 0 -> inputTank1;
                case 1 -> inputTank2;
                case 2 -> outputTank;
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };

            if (fluid.isEmpty() || this.minecraft == null)
                continue;

            int renderHeight = (int) (16 * ((float) tank.getFluidAmount() / (float) tank.getCapacity()));
            IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
            ResourceLocation fluidTextureLocation = fluidTypeExtensions.getStillTexture(fluid);
            TextureAtlasSprite fluidSprite =
                    this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTextureLocation);

            int tintColor = fluidTypeExtensions.getTintColor(fluid);

            float alpha = ((tintColor >> 24) & 0xFF) / 255f;
            float red = ((tintColor >> 16) & 0xFF) / 255f;
            float green = ((tintColor >> 8) & 0xFF) / 255f;
            float blue = (tintColor & 0xFF) / 255f;

            guiGraphics.setColor(red, green, blue, alpha);

            int xPos = switch (i) {
                case 0 -> 20;
                case 1 -> 40;
                case 2 -> 120;
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };
            int yPos = switch (i) {
                case 0, 1 -> 45;
                case 2 -> 36;
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };

            guiGraphics.blit(x + xPos, y + yPos + (16 - renderHeight), 0, 16, renderHeight, fluidSprite);

            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        if (menu.getScaledEnergy() > 0)
            guiGraphics.blit(TEXTURE, x + 165, y + 10 + (64 - menu.getScaledEnergy()), 190, 0, 4, menu.getScaledEnergy());
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting())
            guiGraphics.blit(TEXTURE, x + 61, y + 37, 176, 64, menu.getProgress(), 14);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        for(int slotId = 3; slotId < this.menu.slots.size(); ++slotId) {
            Slot slot = this.menu.slots.get(slotId);

            if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot instanceof FluidSlot) {
                this.hoveredSlot = slot;
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(29, 63, 17, 4, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.chemical_reactor.flush_button"), mouseX, mouseY);
        }

        if (this.hoveredSlot instanceof FluidSlot fluidSlot) {
            FluidTank fluidTank = this.menu.blockEntity.getFluidTank(fluidSlot.getIndex() - 3);
            if (fluidTank != null) {
                if (fluidTank.getFluid() != FluidStack.EMPTY) {
                    String fluidName = fluidTank.getFluid().getFluid().getFluidType().getDescriptionId(fluidTank.getFluid());
                    int fluidAmount = fluidTank.getFluidAmount();
                    guiGraphics.renderTooltip(this.font,
                            List.of(Component.translatable(fluidName),
                                    Component.literal(fluidAmount + "mb")),
                            Optional.empty(), mouseX, mouseY);
                }
            }
        } else super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(29, 63, 17, 4, mouseX, mouseY)) {
            this.menu.flush();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
