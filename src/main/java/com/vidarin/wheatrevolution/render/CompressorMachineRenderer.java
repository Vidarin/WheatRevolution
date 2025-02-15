package com.vidarin.wheatrevolution.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vidarin.wheatrevolution.block.ModelBlock;
import com.vidarin.wheatrevolution.block.entity.CompressorMachineEntity;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.client.model.data.ModelData;

import java.util.Objects;

public class CompressorMachineRenderer implements BlockEntityRenderer<CompressorMachineEntity> {
    public CompressorMachineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CompressorMachineEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack stackToRender = entity.getStackForRendering();

        // Render item
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.15F, 0.5F);
        poseStack.scale(0.35F, 0.35F, 0.35F);
        poseStack.mulPose(Axis.XP.rotationDegrees(270));

        itemRenderer.renderStatic(stackToRender, ItemDisplayContext.FIXED, getLight(Objects.requireNonNull(entity.getLevel()), entity.getBlockPos()), OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.getLevel(), 1);

        poseStack.popPose();

        BakedModel pistonModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(
                BlockRegistry.MODEL_BLOCK.get().defaultBlockState().setValue(
                        ModelBlock.MODEL_TYPE, ModelBlock.ModelTypes.COMPRESSOR_PISTON_BASIC));

        // Render piston
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.45F - (0.35F * ((float) entity.getProgress() / 100)), 0.0F);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(Sheets.translucentCullBlockSheet()),
                null,
                pistonModel,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                Sheets.translucentCullBlockSheet()
        );

        poseStack.popPose();
    }

    private int getLight(Level level, BlockPos pos) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(blockLight, skyLight);
    }
}
