package com.vidarin.wheatrevolution.gui.menu;

import com.vidarin.wheatrevolution.block.entity.CompressorMachineEntity;
import com.vidarin.wheatrevolution.registry.BlockRegistry;
import com.vidarin.wheatrevolution.registry.GuiRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Objects;

public class CompressorMachineMenu extends AbstractContainerMenu {
    public final CompressorMachineEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public CompressorMachineMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, Objects.requireNonNull(inventory.player.level().getBlockEntity(extraData.readBlockPos())), new SimpleContainerData(4));
    }

    public CompressorMachineMenu(int containerId, Inventory inventory, BlockEntity blockEntity, ContainerData data) {
        super(GuiRegistry.COMPRESSOR_MACHINE_MENU.get(), containerId);
        checkContainerSize(inventory, 2);
        this.blockEntity = (CompressorMachineEntity) blockEntity;
        this.level = inventory.player.level();
        this.data = data;

        addPlayerSlots(inventory);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 80, 11));
            this.addSlot(new SlotItemHandler(iItemHandler, 1, 80, 59));
        });

        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);

        return maxProgress != 0 && progress != 0 ? progress * 26 / maxProgress : 0;
    }

    public int getScaledEnergy() {
        int energy = data.get(2);
        int maxEnergy = data.get(3);

        return maxEnergy != 0 && energy != 0 ? energy * 64 / maxEnergy : 0;
    }

    // CREDIT TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 36;
    private static final int TE_SLOT_COUNT = 2;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < 36) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }


    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, BlockRegistry.COMPRESSOR_MACHINE.get());
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
