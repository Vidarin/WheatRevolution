package com.vidarin.wheatrevolution.util.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
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
}
