package com.vidarin.wheatrevolution.gui.menu;

import com.vidarin.wheatrevolution.block.entity.OreFactoryMachineEntity;
import com.vidarin.wheatrevolution.registry.BlockEntityRegistry;
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
import java.util.Optional;

public class OreFactoryMachineMenu extends AbstractContainerMenu {
    public final OreFactoryMachineEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public OreFactoryMachineMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, Objects.requireNonNull(inventory.player.level().getBlockEntity(extraData.readBlockPos())), new SimpleContainerData(6));
    }

    public OreFactoryMachineMenu(int containerId, Inventory inventory, BlockEntity blockEntity, ContainerData data) {
        super(GuiRegistry.ORE_FACTORY_MACHINE_MENU.get(), containerId);
        checkContainerSize(inventory, 9);
        Optional<OreFactoryMachineEntity> entity = inventory.player.level().getBlockEntity(blockEntity.getBlockPos(), BlockEntityRegistry.ORE_FACTORY_MACHINE_ENTITY.get());
        if (entity.isPresent())
            this.blockEntity = entity.get();
        else
            throw new IllegalArgumentException("Ore Factory Menu called from invalid block entity!");
        this.level = inventory.player.level();
        this.data = data;

        addPlayerSlots(inventory);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 33, 15));
            this.addSlot(new SlotItemHandler(iItemHandler, 1, 63, 15));
            this.addSlot(new SlotItemHandler(iItemHandler, 2, 93, 15));
            this.addSlot(new SlotItemHandler(iItemHandler, 3, 123, 15));

            this.addSlot(new SlotItemHandler(iItemHandler, 4, 33, 55));
            this.addSlot(new SlotItemHandler(iItemHandler, 5, 63, 55));
            this.addSlot(new SlotItemHandler(iItemHandler, 6, 93, 55));
            this.addSlot(new SlotItemHandler(iItemHandler, 7, 123, 55));

            this.addSlot(new SlotItemHandler(iItemHandler, 8, 153, 59));
        });

        addDataSlots(data);
    }

    public boolean isCrafting(int processId) {
        return data.get(processId) > 0;
    }

    public int getProgress(int processId) {
        int progress = data.get(processId);
        int maxProgress = data.get(4);

        return maxProgress != 0 && progress != 0 ? progress * 18 / maxProgress : 0;
    }

    public int getHeatLevel() {
        int heatLevel = data.get(5);

        return heatLevel != 0 ? heatLevel * 46 / 100 : 0;
    }

    // CREDIT TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 36;
    private static final int TE_SLOT_COUNT = 8;

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
                player, BlockRegistry.ORE_FACTORY_MACHINE.get());
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
