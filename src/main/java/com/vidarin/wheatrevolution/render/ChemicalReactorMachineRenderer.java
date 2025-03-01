package com.vidarin.wheatrevolution.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vidarin.wheatrevolution.block.ModelBlock;
import com.vidarin.wheatrevolution.block.entity.ChemicalReactorMachineEntity;
import com.vidarin.wheatrevolution.util.rendering.RenderHelper;
import com.vidarin.wheatrevolution.util.rendering.UC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalReactorMachineRenderer implements BlockEntityRenderer<ChemicalReactorMachineEntity> {
    private float rotation;

    @SuppressWarnings("unused")
    public ChemicalReactorMachineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ChemicalReactorMachineEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel rotorModel = RenderHelper.getModel(ModelBlock.ModelTypes.CHEMICAL_REACTOR_ROTOR);

        // Render rotor
        poseStack.pushPose();

        poseStack.translate(0.5F, UC.fromPixels(6), 0.5F);

        if (rotation >= 360.0F)
            rotation = 0.0F;

        if (entity.getProgress() > 0)
            rotation += 180.0F / (Minecraft.getInstance().getFps() + 1);

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(Sheets.translucentCullBlockSheet()),
                null,
                rotorModel,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                Sheets.translucentCullBlockSheet()
        );

        poseStack.popPose();

        // Render fluids
        FluidStack stack = entity.getFluidForRendering();
        Level level = entity.getLevel();
        BlockPos pos = entity.getBlockPos();

        if (stack == null)
            return;

        if (stack.isEmpty() || level == null)
            return;

        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(stack.getFluid());
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(stack);

        if (stillTexture == null)
            return;

        FluidState state = stack.getFluid().defaultFluidState();

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int tintColor = fluidTypeExtensions.getTintColor(state, level, pos);

        float height = (((float) stack.getAmount() / 4000.0F * 0.625f) + 0.25F);

        VertexConsumer builder = buffer.getBuffer(ItemBlockRenderTypes.getRenderLayer(state));

        RenderHelper.drawQuad(builder, poseStack, UC.fromPixels(2.01), height, UC.fromPixels(2.01), UC.fromPixels(13.99), height, UC.fromPixels(13.99),
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, tintColor);

        RenderHelper.drawQuad(builder, poseStack, UC.fromPixels(2.01), UC.fromPixels(5.01), UC.fromPixels(2.01), UC.fromPixels(13.99), height, UC.fromPixels(2.01),
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, tintColor);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(-1f, 0, -1.5f);
        RenderHelper.drawQuad(builder, poseStack, 0.25f, UC.fromPixels(5.01), UC.fromPixels(13.99), UC.fromPixels(13.99), height, UC.fromPixels(13.99),
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, tintColor);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.translate(-1f, 0, 0);
        RenderHelper.drawQuad(builder, poseStack, UC.fromPixels(2.01), UC.fromPixels(5.01), UC.fromPixels(2.01), UC.fromPixels(13.99), height, UC.fromPixels(2.01),
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, tintColor);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(90));
        poseStack.translate(0, 0, -1f);
        RenderHelper.drawQuad(builder, poseStack, UC.fromPixels(2.01), UC.fromPixels(5.01), UC.fromPixels(2.01), UC.fromPixels(13.99), height, UC.fromPixels(2.01),
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, tintColor);
        poseStack.popPose();
    }
}
