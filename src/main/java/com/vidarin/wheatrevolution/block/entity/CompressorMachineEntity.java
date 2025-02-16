package com.vidarin.wheatrevolution.block.entity;

import com.vidarin.wheatrevolution.gui.menu.CompressorMachineMenu;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.CompressorRecipe;
import com.vidarin.wheatrevolution.registry.BlockEntityRegistry;
import com.vidarin.wheatrevolution.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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

public class CompressorMachineEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventoryHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    protected final ContainerData data;
    private int currentProgress = 0;
    private int maxProgress = 100;

    private boolean inCooldown = false;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final EnergyStorage energyStorage;

    public CompressorMachineEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.COMPRESSOR_MACHINE_ENTITY.get(), pos, state);

        this.energyStorage = new EnergyStorage(2048, 16) {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int value = super.receiveEnergy(maxReceive, simulate);
                if (!simulate) {
                    CompressorMachineEntity.this.setChanged();
                    assert CompressorMachineEntity.this.level != null;
                    CompressorMachineEntity.this.level.sendBlockUpdated(CompressorMachineEntity.this.worldPosition,
                            CompressorMachineEntity.this.level.getBlockState(CompressorMachineEntity.this.worldPosition),
                            CompressorMachineEntity.this.level.getBlockState(CompressorMachineEntity.this.worldPosition), 2);
                }
                return value;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                int value = super.extractEnergy(maxExtract, simulate);
                if (!simulate) {
                    CompressorMachineEntity.this.setChanged();
                    assert CompressorMachineEntity.this.level != null;
                    CompressorMachineEntity.this.level.sendBlockUpdated(CompressorMachineEntity.this.worldPosition,
                            CompressorMachineEntity.this.level.getBlockState(CompressorMachineEntity.this.worldPosition),
                            CompressorMachineEntity.this.level.getBlockState(CompressorMachineEntity.this.worldPosition), 2);
                }
                return value;
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> CompressorMachineEntity.this.currentProgress;
                    case 1 -> CompressorMachineEntity.this.maxProgress;
                    case 2 -> CompressorMachineEntity.this.energyStorage.getEnergyStored();
                    case 3 -> CompressorMachineEntity.this.energyStorage.getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> CompressorMachineEntity.this.currentProgress = value;
                    case 1 -> CompressorMachineEntity.this.maxProgress = value;
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

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventoryHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.energyStorage.deserializeNBT(nbt.get("energy_storage"));
        this.currentProgress = nbt.getInt("progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.compressor");
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

    public ItemStack getStackForRendering() {
        if (inventoryHandler.getStackInSlot(INPUT_SLOT).isEmpty())
            return inventoryHandler.getStackInSlot(OUTPUT_SLOT);
        return inventoryHandler.getStackInSlot(INPUT_SLOT);
    }

    public int getProgress() {
        return currentProgress;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CompressorMachineMenu(containerId, inventory, this, this.data);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (hasValidRecipe() && !inCooldown) {
            if (currentProgress == 1 && this.level != null) {
                this.level.playSound(null, this.getBlockPos(), SoundRegistry.COMPRESSOR_ACTIVE_SOUND.get(), SoundSource.BLOCKS, 0.1F, 1.0F);
            }
            progress();
            setChanged(level, blockPos, blockState);
            if (currentProgress >= maxProgress) {
                finishRecipe();
                inCooldown = true;
            }
        } else {
            if (currentProgress > 0)
                currentProgress -= 5;
            if (currentProgress <= 0 && inCooldown) {
                currentProgress = 0;
                inCooldown = false;
                if (this.level != null) {
                    this.level.playSound(null, this.getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.1F, 2.0F);
                }
            }
            if (this.level != null) {
                ((ServerLevel) this.level).getPlayers(serverPlayer -> true)
                        .forEach(serverPlayer -> serverPlayer.connection.send(new ClientboundStopSoundPacket(SoundRegistry.COMPRESSOR_ACTIVE_SOUND.get().getLocation(), SoundSource.BLOCKS)));
            }
        }
    }

    private void finishRecipe() {
        Optional<CompressorRecipe> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) {
            WheatRevolution.LOGGER.warn("Compressor tried to craft empty recipe!");
            return;
        }

        ItemStack result = recipe.get().getResultItem(null);

        this.inventoryHandler.extractItem(INPUT_SLOT, 1, false);

        this.inventoryHandler.setStackInSlot(OUTPUT_SLOT,
                new ItemStack(result.getItem(), this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));

        if (this.level != null) {
            this.level.playSound(null, this.getBlockPos(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 0.8F);
            this.level.playSound(null, this.getBlockPos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.05F, 1.0F);
        }
    }

    private void progress() {
        this.currentProgress++;
        this.energyStorage.extractEnergy(4, false);
    }

    private boolean hasValidRecipe() {
        Optional<CompressorRecipe> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) return false;

        ItemStack result = recipe.get().getResultItem(null);

        return canOutput(result.getCount(), result.getItem()) && this.energyStorage.getEnergyStored() >= 4;
    }

    private Optional<CompressorRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.inventoryHandler.getSlots());

        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            inventory.setItem(i, this.inventoryHandler.getStackInSlot(i));
        }

        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(CompressorRecipe.Type.INSTANCE, inventory, level);
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
