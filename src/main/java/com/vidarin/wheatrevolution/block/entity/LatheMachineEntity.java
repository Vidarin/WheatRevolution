package com.vidarin.wheatrevolution.block.entity;

import com.vidarin.wheatrevolution.gui.menu.LatheMachineMenu;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.LatheRecipe;
import com.vidarin.wheatrevolution.registry.BlockEntityRegistry;
import com.vidarin.wheatrevolution.registry.SoundRegistry;
import com.vidarin.wheatrevolution.util.TickScheduler;
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

public class LatheMachineEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventoryHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    protected final ContainerData data;
    private int currentProgress = 0;
    private int maxProgress = 100;

    private int rodPosition = 0;
    private boolean moveRodRight = true;

    private boolean running = false;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final EnergyStorage energyStorage;

    public LatheMachineEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.LATHE_MACHINE_ENTITY.get(), pos, state);

        this.energyStorage = new EnergyStorage(3072, 24) {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int value = super.receiveEnergy(maxReceive, simulate);
                if (!simulate) {
                    LatheMachineEntity.this.setChanged();
                    assert LatheMachineEntity.this.level != null;
                    LatheMachineEntity.this.level.sendBlockUpdated(LatheMachineEntity.this.worldPosition,
                            LatheMachineEntity.this.level.getBlockState(LatheMachineEntity.this.worldPosition),
                            LatheMachineEntity.this.level.getBlockState(LatheMachineEntity.this.worldPosition), 2);
                }
                return value;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                int value = super.extractEnergy(maxExtract, simulate);
                if (!simulate) {
                    LatheMachineEntity.this.setChanged();
                    assert LatheMachineEntity.this.level != null;
                    LatheMachineEntity.this.level.sendBlockUpdated(LatheMachineEntity.this.worldPosition,
                            LatheMachineEntity.this.level.getBlockState(LatheMachineEntity.this.worldPosition),
                            LatheMachineEntity.this.level.getBlockState(LatheMachineEntity.this.worldPosition), 2);
                }
                return value;
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> LatheMachineEntity.this.currentProgress;
                    case 1 -> LatheMachineEntity.this.maxProgress;
                    case 2 -> LatheMachineEntity.this.energyStorage.getEnergyStored();
                    case 3 -> LatheMachineEntity.this.energyStorage.getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> LatheMachineEntity.this.currentProgress = value;
                    case 1 -> LatheMachineEntity.this.maxProgress = value;
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
        nbt.putInt("rod_position", rodPosition);
        nbt.putBoolean("move_rod_right", moveRodRight);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventoryHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.energyStorage.deserializeNBT(nbt.get("energy_storage"));
        this.currentProgress = nbt.getInt("progress");
        this.rodPosition = nbt.getInt("rod_position");
        this.moveRodRight = nbt.getBoolean("move_rod_right");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.lathe");
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

    public @Nullable ItemStack getStackForRendering() {
        if (inventoryHandler.getStackInSlot(INPUT_SLOT).isEmpty())
            return null;
        return inventoryHandler.getStackInSlot(INPUT_SLOT);
    }

    public int getRodPosition() {
        return rodPosition;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LatheMachineMenu(containerId, inventory, this, this.data);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (hasValidRecipe()) {
            if (currentProgress == 1 && this.level != null && !running) {
                this.level.playSound(null, this.worldPosition, SoundRegistry.LATHE_START_SOUND.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
                running = true;
            }
            if (currentProgress % 20 == 0 && running && this.level != null)
                this.level.playSound(null, this.worldPosition, SoundRegistry.LATHE_ACTIVE_SOUND.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
            progress();
            setChanged(level, blockPos, blockState);
            if (currentProgress >= maxProgress) {
                finishRecipe();
                currentProgress = 0;
            }
        } else {
            currentProgress = 0;
            if (this.level != null) {
                TickScheduler.scheduleTask(() -> ((ServerLevel) this.level).getPlayers(serverPlayer -> true)
                        .forEach(serverPlayer -> serverPlayer.connection.send(new ClientboundStopSoundPacket(SoundRegistry.LATHE_ACTIVE_SOUND.get().getLocation(), SoundSource.BLOCKS))),
                        5
                );
                if (!hasValidRecipe() && running)
                    this.level.playSound(null, this.worldPosition, SoundRegistry.LATHE_STOP_SOUND.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
            }
            running = false;
        }
    }

    private void finishRecipe() {
        Optional<LatheRecipe> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) {
            WheatRevolution.LOGGER.warn("Lathe tried to craft empty recipe!");
            return;
        }

        ItemStack result = recipe.get().getResultItem(null);

        this.inventoryHandler.extractItem(INPUT_SLOT, 1, false);

        this.inventoryHandler.setStackInSlot(OUTPUT_SLOT,
                new ItemStack(result.getItem(), this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    private void progress() {
        this.currentProgress++;
        this.energyStorage.extractEnergy(6, false);

        if (moveRodRight) {
            rodPosition++;
            if (rodPosition >= 100) {
                moveRodRight = false;
            }
        } else {
            rodPosition--;
            if (rodPosition <= 0) {
                moveRodRight = true;
            }
        }
    }

    private boolean hasValidRecipe() {
        Optional<LatheRecipe> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) return false;

        ItemStack result = recipe.get().getResultItem(null);

        return canOutput(result.getCount(), result.getItem()) && this.energyStorage.getEnergyStored() >= 6;
    }

    private Optional<LatheRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.inventoryHandler.getSlots());

        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            inventory.setItem(i, this.inventoryHandler.getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(LatheRecipe.Type.INSTANCE, inventory, level);
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
