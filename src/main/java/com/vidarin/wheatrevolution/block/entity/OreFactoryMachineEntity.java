package com.vidarin.wheatrevolution.block.entity;

import com.vidarin.wheatrevolution.block.OreFactoryMachineBlock;
import com.vidarin.wheatrevolution.gui.menu.OreFactoryMachineMenu;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.OreFactoryRecipe;
import com.vidarin.wheatrevolution.recipe.adapter.OreFactoryBlastRecipeAdapter;
import com.vidarin.wheatrevolution.registry.BlockEntityRegistry;
import net.mcreator.wheat_death_of_the_universe.network.WheatdeathoftheuniverseModVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class OreFactoryMachineEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventoryHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if (!level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT_SLOT_1 = 0;
    private static final int INPUT_SLOT_2 = 1;
    private static final int INPUT_SLOT_3 = 2;
    private static final int INPUT_SLOT_4 = 3;

    private static final int OUTPUT_SLOT_1 = 4;
    private static final int OUTPUT_SLOT_2 = 5;
    private static final int OUTPUT_SLOT_3 = 6;
    private static final int OUTPUT_SLOT_4 = 7;

    private static final int FUEL_SLOT = 8;

    protected final ContainerData data;

    private float currentProgress1 = 0.0F;
    private float currentProgress2 = 0.0F;
    private float currentProgress3 = 0.0F;
    private float currentProgress4 = 0.0F;

    private float maxProgress = 100.0F;

    private float heatLevel = 0.0F;

    private int ticksUntilSound = 0;

    private boolean shouldUnlit = false;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public OreFactoryMachineEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ORE_FACTORY_MACHINE_ENTITY.get(), pos, state);

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> Math.round(OreFactoryMachineEntity.this.currentProgress1);
                    case 1 -> Math.round(OreFactoryMachineEntity.this.currentProgress2);
                    case 2 -> Math.round(OreFactoryMachineEntity.this.currentProgress3);
                    case 3 -> Math.round(OreFactoryMachineEntity.this.currentProgress4);
                    case 4 -> Math.round(OreFactoryMachineEntity.this.maxProgress);
                    case 5 -> Math.round(OreFactoryMachineEntity.this.heatLevel);
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> OreFactoryMachineEntity.this.currentProgress1 = value;
                    case 1 -> OreFactoryMachineEntity.this.currentProgress2 = value;
                    case 2 -> OreFactoryMachineEntity.this.currentProgress3 = value;
                    case 3 -> OreFactoryMachineEntity.this.currentProgress4 = value;
                    case 4 -> OreFactoryMachineEntity.this.maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", inventoryHandler.serializeNBT());
        nbt.putFloat("progress_1", currentProgress1);
        nbt.putFloat("progress_2", currentProgress2);
        nbt.putFloat("progress_3", currentProgress3);
        nbt.putFloat("progress_4", currentProgress4);
        nbt.putFloat("heat", heatLevel);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventoryHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.currentProgress1 = nbt.getFloat("progress_1");
        this.currentProgress2 = nbt.getFloat("progress_2");
        this.currentProgress3 = nbt.getFloat("progress_3");
        this.currentProgress4 = nbt.getFloat("progress_4");
        this.heatLevel = nbt.getFloat("heat");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.ore_factory");
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

    public boolean isRunning() {
        return currentProgress1 > 0 || currentProgress2 > 0 || currentProgress3 > 0 || currentProgress4 > 0;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new OreFactoryMachineMenu(containerId, inventory, this, this.data);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (!isRunning())
            shouldUnlit = true;

        // Process recipes
        for (int processId = 0; processId < 4; processId++) {
            if (hasValidRecipe(processId)) {
                progress(processId);
                if (processId == 3) {
                    setChanged(level, blockPos, blockState);
                }
                if (isFinished(processId)) {
                    finishRecipe(processId);
                    resetProgress(processId);
                }
            } else {
                resetProgress(processId);
            }
        }

        if (isRunning())
            shouldUnlit = false;

        // Update heat level
        if (heatLevel > 0.0F) {
            if (isRunning())
                heatLevel -= 0.02F;
            else
                heatLevel -= 0.1F;
        }
        if (!inventoryHandler.getStackInSlot(FUEL_SLOT).isEmpty()) {
            if (AbstractFurnaceBlockEntity.isFuel(inventoryHandler.getStackInSlot(FUEL_SLOT)) && heatLevel < 100.0F) {
                heatLevel += (float) ForgeHooks.getBurnTime(inventoryHandler.getStackInSlot(FUEL_SLOT), RecipeType.BLASTING) / 360.0F;
                inventoryHandler.extractItem(FUEL_SLOT, 1, false);
                if (this.level != null)
                    this.level.playSound(null, this.worldPosition, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.2F, 0.8F);
            }
        }
        if (heatLevel > 100.0F)
            heatLevel = 100.0F;
        if (heatLevel < 0.0F)
            heatLevel = 0.0F;

        // Add pollution
        if (isRunning() && this.level != null) {
            WheatdeathoftheuniverseModVariables.WorldVariables.get(this.level).Temperature += 0.005D;
        }

        // Handle sounds
        if (this.isRunning() && this.level != null) {
            if (ticksUntilSound <= 0) {
                this.level.playSound(null, this.worldPosition, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 1.0F);
                ticksUntilSound = 20;
            } else {
                ticksUntilSound--;
            }
        }

        setLit();
    }


    private void resetProgress(int processId) {
        switch (processId) {
            case 0 -> currentProgress1 = 0.0F;
            case 1 -> currentProgress2 = 0.0F;
            case 2 -> currentProgress3 = 0.0F;
            case 3 -> currentProgress4 = 0.0F;
        }
    }

    private boolean isFinished(int processId) {
        return switch (processId) {
            case 0 -> currentProgress1 >= maxProgress;
            case 1 -> currentProgress2 >= maxProgress;
            case 2 -> currentProgress3 >= maxProgress;
            case 3 -> currentProgress4 >= maxProgress;
            default -> throw new IllegalStateException("Unexpected value: " + processId);
        };
    }

    private void finishRecipe(int processId) {
        int outputSlot = switch (processId) {
            case 0 -> OUTPUT_SLOT_1;
            case 1 -> OUTPUT_SLOT_2;
            case 2 -> OUTPUT_SLOT_3;
            case 3 -> OUTPUT_SLOT_4;
            default -> throw new IllegalStateException("Unexpected value: " + processId);
        };

        int inputSlot = switch (processId) {
            case 0 -> INPUT_SLOT_1;
            case 1 -> INPUT_SLOT_2;
            case 2 -> INPUT_SLOT_3;
            case 3 -> INPUT_SLOT_4;
            default -> throw new IllegalStateException("Unexpected value: " + processId);
        };

        Optional<OreFactoryRecipe> recipe = getCurrentRecipe(processId);

        if (recipe.isEmpty()) {
            WheatRevolution.LOGGER.warn("Ore Factory tried to craft empty recipe!");
            return;
        }

        ItemStack result = recipe.get().getResultItem(null);

        this.inventoryHandler.extractItem(inputSlot, 1, false);

        this.inventoryHandler.setStackInSlot(outputSlot,
                new ItemStack(result.getItem(), this.inventoryHandler.getStackInSlot(outputSlot).getCount() + result.getCount()));
    }

    private void progress(int processId) {
        switch (processId) {
            case 0 -> this.currentProgress1 += 0.033F * heatLevel - 0.5F;
            case 1 -> this.currentProgress2 += 0.033F * heatLevel - 0.5F;
            case 2 -> this.currentProgress3 += 0.033F * heatLevel - 0.5F;
            case 3 -> this.currentProgress4 += 0.033F * heatLevel - 0.5F;
        }
    }

    private boolean hasValidRecipe(int processId) {
        Optional<OreFactoryRecipe> recipe = getCurrentRecipe(processId);

        if (recipe.isEmpty()) return false;

        ItemStack result = recipe.get().getResultItem(null);

        return canOutput(result.getCount(), result.getItem(), processId);
    }

    private Optional<OreFactoryRecipe> getCurrentRecipe(int processId) {
        SimpleContainer inventory = new SimpleContainer(2);

        switch (processId) {
            case 0:
                inventory.setItem(0, inventoryHandler.getStackInSlot(INPUT_SLOT_1));
                inventory.setItem(1, inventoryHandler.getStackInSlot(OUTPUT_SLOT_1));
                break;
            case 1:
                inventory.setItem(0, inventoryHandler.getStackInSlot(INPUT_SLOT_2));
                inventory.setItem(1, inventoryHandler.getStackInSlot(OUTPUT_SLOT_2));
                break;
            case 2:
                inventory.setItem(0, inventoryHandler.getStackInSlot(INPUT_SLOT_3));
                inventory.setItem(1, inventoryHandler.getStackInSlot(OUTPUT_SLOT_3));
                break;
            case 3:
                inventory.setItem(0, inventoryHandler.getStackInSlot(INPUT_SLOT_4));
                inventory.setItem(1, inventoryHandler.getStackInSlot(OUTPUT_SLOT_4));
                break;
        }

        assert this.level != null;

        Optional<OreFactoryRecipe> oreFactoryRecipe = this.level.getRecipeManager()
                .getRecipeFor(OreFactoryRecipe.Type.INSTANCE, inventory, level);
        if (oreFactoryRecipe.isPresent()) {
            return oreFactoryRecipe;
        } else {
            Optional<BlastingRecipe> blastingRecipe = this.level.getRecipeManager()
                    .getRecipeFor(RecipeType.BLASTING, inventory, level);
            if (blastingRecipe.isPresent()) {
                return Optional.of(new OreFactoryBlastRecipeAdapter(blastingRecipe.get()));
            }
        }
        return Optional.empty();
    }

    private boolean canOutput(int count, Item item, int processId) {
        int outputSlot = switch (processId) {
            case 0 -> OUTPUT_SLOT_1;
            case 1 -> OUTPUT_SLOT_2;
            case 2 -> OUTPUT_SLOT_3;
            case 3 -> OUTPUT_SLOT_4;
            default -> throw new IllegalStateException("Unexpected value: " + processId);
        };

        boolean doesOutputHaveTheRightItem = this.inventoryHandler.getStackInSlot(outputSlot).isEmpty() || this.inventoryHandler.getStackInSlot(outputSlot).is(item);
        return doesOutputHaveTheRightItem && this.inventoryHandler.getStackInSlot(outputSlot).getCount() + count <= this.inventoryHandler.getStackInSlot(outputSlot).getMaxStackSize();
    }

    public void setLit() {
        boolean isLit = this.getBlockState().getValue(OreFactoryMachineBlock.LIT);

        if (this.level != null) {
            if (!isLit && this.isRunning()) {
                this.level.setBlock(this.worldPosition,
                        this.getBlockState().setValue(OreFactoryMachineBlock.LIT, true),
                        3);
                this.level.sendBlockUpdated(this.worldPosition,
                        this.getBlockState(),
                        this.getBlockState().setValue(OreFactoryMachineBlock.LIT, false),
                        3);
            }
            if (isLit && shouldUnlit) {
                this.level.setBlock(this.worldPosition,
                        this.getBlockState().setValue(OreFactoryMachineBlock.LIT, false),
                        3);
                this.level.sendBlockUpdated(this.worldPosition,
                        this.getBlockState(),
                        this.getBlockState().setValue(OreFactoryMachineBlock.LIT, false),
                        3);
                shouldUnlit = false;
            }
        }
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
