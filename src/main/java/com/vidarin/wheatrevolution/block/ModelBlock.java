package com.vidarin.wheatrevolution.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

// This is a block for storing models for rendering and is not meant to be placed.

public class ModelBlock extends Block {
    public static final EnumProperty<ModelTypes> MODEL_TYPE = EnumProperty.create("model", ModelTypes.class);

    public ModelBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.DIRT).noLootTable());
        this.registerDefaultState(this.getStateDefinition().any().setValue(MODEL_TYPE, ModelTypes.NO_MODEL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODEL_TYPE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(MODEL_TYPE, ModelTypes.NO_MODEL);
    }

    public enum ModelTypes implements StringRepresentable {
        NO_MODEL,
        COMPRESSOR_PISTON_BASIC,
        LATHE_ROD;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
