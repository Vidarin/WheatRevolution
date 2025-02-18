package com.vidarin.wheatrevolution.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vidarin.wheatrevolution.block.LatheMachineBlock;
import com.vidarin.wheatrevolution.block.ModelBlock;
import com.vidarin.wheatrevolution.block.entity.LatheMachineEntity;
import com.vidarin.wheatrevolution.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.client.model.data.ModelData;

import java.util.Objects;

public class LatheMachineRenderer implements BlockEntityRenderer<LatheMachineEntity> {
    private float rotation;

    public LatheMachineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(LatheMachineEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack stackToRender = entity.getStackForRendering();

        boolean facingWE = entity.getBlockState().getValue(LatheMachineBlock.FACING) == Direction.WEST || entity.getBlockState().getValue(LatheMachineBlock.FACING) == Direction.EAST;

        // Render item
        if (stackToRender != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.75F, 0.5F);
            poseStack.scale(0.5F, 0.5F, 0.5F);

            poseStack.mulPose(facingWE ? Axis.YP.rotationDegrees(90) : Axis.YP.rotationDegrees(0));

            if (rotation >= 360.0F)
                rotation = 0.0F;

            if (entity.getCurrentProgress() > 0)
                poseStack.mulPose(Axis.XP.rotationDegrees(rotation));

            rotation += 180.0F / (Minecraft.getInstance().getFps() + 1);

            itemRenderer.renderStatic(
                    stackToRender,
                    ItemDisplayContext.FIXED,
                    getLight(Objects.requireNonNull(entity.getLevel()), entity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY,
                    poseStack, buffer,
                    entity.getLevel(),
                    1);

            poseStack.popPose();
        }

        BakedModel rodModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(
                BlockRegistry.MODEL_BLOCK.get().defaultBlockState().setValue(
                        ModelBlock.MODEL_TYPE, ModelBlock.ModelTypes.LATHE_ROD));

        // Render rod
        poseStack.pushPose();
        if (facingWE)
            poseStack.translate(0.0F, 0.45F, 0.1F + ((float) entity.getRodPosition() / 125));
        else {
            poseStack.translate(0.1F + ((float) entity.getRodPosition() / 125), 0.45F, 1.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(Sheets.translucentCullBlockSheet()),
                null,
                rodModel,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                Sheets.translucentCullBlockSheet()
        );

        poseStack.popPose();

        // Add particles
        try {
            RandomSource random = Objects.requireNonNull(entity.getLevel()).random;
            if (entity.getCurrentProgress() > 0 && random.nextInt(Minecraft.getInstance().getFps()) > Minecraft.getInstance().getFps() - 5 &&
                entity.getRodPosition() > 25 && entity.getRodPosition() < 75) {
                Minecraft.getInstance().particleEngine.createParticle(
                        ParticleTypes.LAVA,
                        entity.getBlockPos().getX() + 0.5F,
                        entity.getBlockPos().getY() + 0.6F,
                        entity.getBlockPos().getZ() + 0.5F,
                        random.nextFloat() - 0.5F,
                        0.75F,
                        random.nextFloat() - 0.5F
                );
            }
        } catch (IllegalArgumentException ignored) {}
    }

    private int getLight(Level level, BlockPos pos) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(blockLight, skyLight);
    }
}
