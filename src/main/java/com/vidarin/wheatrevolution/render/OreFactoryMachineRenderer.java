package com.vidarin.wheatrevolution.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vidarin.wheatrevolution.block.entity.OreFactoryMachineEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

import java.util.Objects;

public class OreFactoryMachineRenderer implements BlockEntityRenderer<OreFactoryMachineEntity> {
    @SuppressWarnings("unused")
    public OreFactoryMachineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(OreFactoryMachineEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // Render particles
        try {
            RandomSource random = Objects.requireNonNull(entity.getLevel()).random;
            if (entity.isRunning() && random.nextInt(Minecraft.getInstance().getFps()) > Minecraft.getInstance().getFps() - 5) {
                Minecraft.getInstance().particleEngine.createParticle(
                        ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                        entity.getBlockPos().getX() + 0.5F,
                        entity.getBlockPos().getY() + 2.0F,
                        entity.getBlockPos().getZ() + 0.5F,
                        0.0F, 0.2F, 0.0F
                );
            }
        } catch (IllegalArgumentException ignored) {}
    }
}
