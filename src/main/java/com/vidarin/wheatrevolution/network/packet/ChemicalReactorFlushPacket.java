package com.vidarin.wheatrevolution.network.packet;

import com.vidarin.wheatrevolution.block.entity.ChemicalReactorMachineEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChemicalReactorFlushPacket(BlockPos pos) {
    public void encoder(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static ChemicalReactorFlushPacket decoder(FriendlyByteBuf buf) {
        final BlockPos blockPos = buf.readBlockPos();
        return new ChemicalReactorFlushPacket(blockPos);
    }

    public static void handle(ChemicalReactorFlushPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                Level level = sender.level();
                BlockEntity be = level.getBlockEntity(msg.pos);
                if (be instanceof ChemicalReactorMachineEntity chemicalReactor) {
                    chemicalReactor.flush();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
