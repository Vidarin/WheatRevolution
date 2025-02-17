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
    public OreFactoryMachineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(OreFactoryMachineEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // Render particles
        RandomSource random = Objects.requireNonNull(entity.getLevel()).random;
        if (entity.isRunning() && random.nextInt(Minecraft.getInstance().getFps()) > Minecraft.getInstance().getFps() - 2) {
            Minecraft.getInstance().particleEngine.createParticle(
                    ParticleTypes.FLAME,
                    entity.getBlockPos().getX() + 0.5F,
                    entity.getBlockPos().getY() + 0.15F,
                    entity.getBlockPos().getZ() + 0.5F,
                    random.nextFloat() * 0.05F - 0.1F,
                    0.1F,
                    random.nextFloat() * 0.05F - 0.1F
            );
            Minecraft.getInstance().particleEngine.createParticle(
                    ParticleTypes.SMOKE,
                    entity.getBlockPos().getX() + 0.5F,
                    entity.getBlockPos().getY() + 0.15F,
                    entity.getBlockPos().getZ() + 0.5F,
                    random.nextFloat() * 0.5F - 0.1F,
                    0.1F,
                    random.nextFloat() * 0.5F - 0.1F
            );
        }
    }

}
