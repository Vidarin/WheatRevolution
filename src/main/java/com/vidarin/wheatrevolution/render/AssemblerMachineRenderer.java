package com.vidarin.wheatrevolution.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vidarin.wheatrevolution.block.AssemblerMachineBlock;
import com.vidarin.wheatrevolution.block.ModelBlock;
import com.vidarin.wheatrevolution.block.entity.AssemblerMachineEntity;
import com.vidarin.wheatrevolution.util.rendering.Animation;
import com.vidarin.wheatrevolution.util.rendering.RenderHelper;
import com.vidarin.wheatrevolution.util.rendering.UC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

public class AssemblerMachineRenderer implements BlockEntityRenderer<AssemblerMachineEntity> {
    @SuppressWarnings("unused")
    public AssemblerMachineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(AssemblerMachineEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Animation cogPath = Animation.create(true,
                new Animation.Keyframe(60),
                new Animation.Keyframe(60).withRotation(360, 0, 0)
        );

        BakedModel gearModel = RenderHelper.getModel(ModelBlock.ModelTypes.ASSEMBLER_GEAR);

        for (int cogId = 0; cogId < 7; cogId++) {
            poseStack.pushPose();

            switch (entity.getBlockState().getValue(AssemblerMachineBlock.FACING)) {
                case WEST:
                    poseStack.translate(UC.fromPixels(11), UC.fromPixels(6), UC.fromPixels(14) - (0.11F * cogId));
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                    break;
                case EAST:
                    poseStack.translate(UC.fromPixels(6), UC.fromPixels(6), UC.fromPixels(13) - (0.11F * cogId));
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                    break;
                case NORTH, UP, DOWN:
                    poseStack.translate(UC.fromPixels(14) - (0.11F * cogId), UC.fromPixels(6), UC.fromPixels(11));
                    break;
                case SOUTH:
                    poseStack.translate(UC.fromPixels(13) - (0.11F * cogId), UC.fromPixels(6), UC.fromPixels(6));
            }

            poseStack.scale(0.7F, 0.7F, 0.7F);

            Vec3 rotation = cogPath.frameAtProgress(entity.getProgress() / 30.0F).getRotation();

            poseStack.mulPose(Axis.XP.rotationDegrees((float) rotation.x + (15.0F * cogId)));

            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    poseStack.last(),
                    buffer.getBuffer(Sheets.translucentCullBlockSheet()),
                    null,
                    gearModel,
                    1.0F, 1.0F, 1.0F,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    Sheets.translucentCullBlockSheet()
            );

            poseStack.popPose();
        }
    }
}
 