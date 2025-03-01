package com.vidarin.wheatrevolution.util.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vidarin.wheatrevolution.block.ModelBlock;
import com.vidarin.wheatrevolution.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class RenderHelper {
    public static void translateFrame(Animation.Keyframe frame, PoseStack poseStack) {
        Vec3 posVec = frame.getPosition();
        Vector3f rotVec = frame.getRotation().toVector3f();
        Vector3f scaleVec = frame.getScale().toVector3f();

        poseStack.translate(posVec.x, posVec.y, posVec.z);

        if (frame.getScale() != Vec3.ZERO) {
            poseStack.scale(scaleVec.x, scaleVec.y, scaleVec.z);
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(rotVec.x));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotVec.y));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotVec.z));
    }

    public static BakedModel getModel(ModelBlock.ModelTypes type) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(
                BlockRegistry.MODEL_BLOCK.get().defaultBlockState().setValue(
                        ModelBlock.MODEL_TYPE, type));
    }

    public static void drawVertex(VertexConsumer builder, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight, int color) {
        builder.vertex(poseStack.last().pose(), x, y, z)
                .color(color)
                .uv(u, v)
                .uv2(packedLight)
                .normal(1, 0, 0)
                .endVertex();
    }

    public static void drawQuad(VertexConsumer builder, PoseStack poseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, int packedLight, int color) {
        drawVertex(builder, poseStack, x0, y0, z0, u0, v0, packedLight, color);
        drawVertex(builder, poseStack, x0, y1, z1, u0, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y1, z1, u1, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y0, z0, u1, v0, packedLight, color);
    }
}
