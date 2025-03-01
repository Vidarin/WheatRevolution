package com.vidarin.wheatrevolution.block.entity;

import com.vidarin.wheatrevolution.gui.menu.AssemblerMachineMenu;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.AssemblerRecipe;
import com.vidarin.wheatrevolution.registry.BlockEntityRegistry;
import com.vidarin.wheatrevolution.registry.SoundRegistry;
import com.vidarin.wheatrevolution.util.CountIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AssemblerMachineEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventoryHandler = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int OUTPUT_SLOT = 6;

    protected final ContainerData data;
    private int currentProgress = 0;
    private int maxProgress = 100;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final EnergyStorage energyStorage;

    public AssemblerMachineEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ASSEMBLER_MACHINE_ENTITY.get(), pos, state);

        this.energyStorage = new EnergyStorage(4096, 32) {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int value = super.receiveEnergy(maxReceive, simulate);
                if (!simulate) {
                    AssemblerMachineEntity.this.setChanged();
                    assert AssemblerMachineEntity.this.level != null;
                    AssemblerMachineEntity.this.level.sendBlockUpdated(AssemblerMachineEntity.this.worldPosition,
                            AssemblerMachineEntity.this.level.getBlockState(AssemblerMachineEntity.this.worldPosition),
                            AssemblerMachineEntity.this.level.getBlockState(AssemblerMachineEntity.this.worldPosition), 2);
                }
                return value;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                int value = super.extractEnergy(maxExtract, simulate);
                if (!simulate) {
                    AssemblerMachineEntity.this.setChanged();
                    assert AssemblerMachineEntity.this.level != null;
                    AssemblerMachineEntity.this.level.sendBlockUpdated(AssemblerMachineEntity.this.worldPosition,
                            AssemblerMachineEntity.this.level.getBlockState(AssemblerMachineEntity.this.worldPosition),
                            AssemblerMachineEntity.this.level.getBlockState(AssemblerMachineEntity.this.worldPosition), 2);
                }
                return value;
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> AssemblerMachineEntity.this.currentProgress;
                    case 1 -> AssemblerMachineEntity.this.maxProgress;
                    case 2 -> AssemblerMachineEntity.this.energyStorage.getEnergyStored();
                    case 3 -> AssemblerMachineEntity.this.energyStorage.getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> AssemblerMachineEntity.this.currentProgress = value;
                    case 1 -> AssemblerMachineEntity.this.maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", inventoryHandler.serializeNBT());
        nbt.put("energy_storage", energyStorage.serializeNBT());
        nbt.putInt("progress", currentProgress);
        nbt.putInt("max_progress", maxProgress);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventoryHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.energyStorage.deserializeNBT(nbt.get("energy_storage"));
        this.currentProgress = nbt.getInt("progress");
        this.maxProgress = nbt.getInt("max_progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.assembler");
    }

    public void dropItems() {
        SimpleContainer inventory = new SimpleContainer(inventoryHandler.getSlots());
        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            inventory.setItem(i, inventoryHandler.getStackInSlot(i));
        }

        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return lazyItemHandler.cast();
        else if (cap == ForgeCapabilities.ENERGY)
            return LazyOptional.of(() -> energyStorage).cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> inventoryHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public int getProgress() {
        return currentProgress;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AssemblerMachineMenu(containerId, inventory, this, this.data);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (hasValidRecipe()) {
            if (currentProgress % 40 == 0 && this.level != null) {
                this.level.playSound(null, this.worldPosition, SoundRegistry.ASSEMBLER_SOUND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            progress();
            setChanged(level, blockPos, blockState);
            if (currentProgress >= maxProgress) {
                finishRecipe();
                currentProgress = 0;
            }
        } else {
            currentProgress = 0;
            if (this.level != null) {
                ((ServerLevel) this.level).getPlayers(serverPlayer -> true)
                        .forEach(serverPlayer -> serverPlayer.connection.send(new ClientboundStopSoundPacket(SoundRegistry.ASSEMBLER_SOUND.get().getLocation(), SoundSource.BLOCKS)));
            }
        }
    }

    private void finishRecipe() {
        Optional<AssemblerRecipe> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) {
            WheatRevolution.LOGGER.warn("Assembler tried to craft empty recipe!");
            return;
        }

        ItemStack result = recipe.get().getResultItem(null);

        extractItems(recipe.get());

        this.inventoryHandler.setStackInSlot(OUTPUT_SLOT,
                new ItemStack(result.getItem(), this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));

        if (this.level != null) {
            ((ServerLevel) this.level).getPlayers(serverPlayer -> true)
                    .forEach(serverPlayer -> serverPlayer.connection.send(new ClientboundStopSoundPacket(SoundRegistry.ASSEMBLER_SOUND.get().getLocation(), SoundSource.BLOCKS)));
        }
    }

    private void extractItems(AssemblerRecipe recipe) {
        for (CountIngredient countIngredient : recipe.getInputs()) {
            int remaining = countIngredient.count();
            for (int slot = 0; slot < 6 && remaining > 0; slot++) {
                ItemStack stack = inventoryHandler.getStackInSlot(slot);
                if (countIngredient.ingredient().test(stack)) {
                    int available = stack.getCount();
                    if (available > 0) {
                        int toExtract = Math.min(available, remaining);
                        inventoryHandler.extractItem(slot, toExtract, false);
                        remaining -= toExtract;
                    }
                }
            }
        }
    }


    private void progress() {
        this.currentProgress++;
        this.energyStorage.extractEnergy(8, false);
    }

    private boolean hasValidRecipe() {
        Optional<AssemblerRecipe> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) return false;

        ItemStack result = recipe.get().getResultItem(null);

        this.maxProgress = recipe.get().getTime();

        return canOutput(result.getCount(), result.getItem()) && this.energyStorage.getEnergyStored() >= 4;
    }

    private Optional<AssemblerRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.inventoryHandler.getSlots());

        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            inventory.setItem(i, this.inventoryHandler.getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(AssemblerRecipe.Type.INSTANCE, inventory, level);
    }

    private boolean canOutput(int count, Item item) {
        boolean doesOutputHaveTheRightItem = this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).is(item);
        return doesOutputHaveTheRightItem && this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}
