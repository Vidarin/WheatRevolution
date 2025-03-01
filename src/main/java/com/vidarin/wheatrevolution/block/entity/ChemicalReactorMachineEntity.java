package com.vidarin.wheatrevolution.block.entity;

import com.vidarin.wheatrevolution.gui.menu.ChemicalReactorMachineMenu;
import com.vidarin.wheatrevolution.main.WheatRevolution;
import com.vidarin.wheatrevolution.recipe.ChemicalReactorRecipe;
import com.vidarin.wheatrevolution.registry.BlockEntityRegistry;
import com.vidarin.wheatrevolution.registry.SoundRegistry;
import com.vidarin.wheatrevolution.util.CountIngredient;
import net.mcreator.wheat_death_of_the_universe.network.WheatdeathoftheuniverseModVariables;
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
import net.minecraft.world.Container;
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
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ChemicalReactorMachineEntity extends BlockEntity implements MenuProvider, Container {
    private final ItemStackHandler inventoryHandler = new ItemStackHandler(6) {
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
    private static final int OUTPUT_SLOT = 2;

    private static final int FLUID_INPUT_SLOT_1 = 3;
    private static final int FLUID_INPUT_SLOT_2 = 4;
    private static final int FLUID_OUTPUT_SLOT = 5;

    protected final ContainerData data;
    private int currentProgress = 0;
    private int maxProgress = 100;

    private int soundProgress = 0;

    private boolean shouldFlush = false;
    private boolean shouldStopSound = false;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private final EnergyStorage energyStorage;
    private final FluidTank fluidInput1 = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            ChemicalReactorMachineEntity.this.setChanged();
            if (ChemicalReactorMachineEntity.this.level != null)
                ChemicalReactorMachineEntity.this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };
    private final FluidTank fluidInput2 = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            ChemicalReactorMachineEntity.this.setChanged();
            if (ChemicalReactorMachineEntity.this.level != null)
                ChemicalReactorMachineEntity.this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    private final FluidTank fluidOutput = new FluidTank(8000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            ChemicalReactorMachineEntity.this.setChanged();
            if (ChemicalReactorMachineEntity.this.level != null)
                ChemicalReactorMachineEntity.this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    private LazyOptional<FluidTank> lazyFluidTankI1 = LazyOptional.empty();
    private LazyOptional<FluidTank> lazyFluidTankI2 = LazyOptional.empty();
    private LazyOptional<FluidTank> lazyFluidTankO  = LazyOptional.empty();

    public ChemicalReactorMachineEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CHEMICAL_REACTOR_ENTITY.get(), pos, state);

        this.energyStorage = new EnergyStorage(2048, 16) {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int value = super.receiveEnergy(maxReceive, simulate);
                if (!simulate) {
                    ChemicalReactorMachineEntity.this.setChanged();
                    assert ChemicalReactorMachineEntity.this.level != null;
                    ChemicalReactorMachineEntity.this.level.sendBlockUpdated(ChemicalReactorMachineEntity.this.worldPosition,
                            ChemicalReactorMachineEntity.this.level.getBlockState(ChemicalReactorMachineEntity.this.worldPosition),
                            ChemicalReactorMachineEntity.this.level.getBlockState(ChemicalReactorMachineEntity.this.worldPosition), 2);
                }
                return value;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                int value = super.extractEnergy(maxExtract, simulate);
                if (!simulate) {
                    ChemicalReactorMachineEntity.this.setChanged();
                    assert ChemicalReactorMachineEntity.this.level != null;
                    ChemicalReactorMachineEntity.this.level.sendBlockUpdated(ChemicalReactorMachineEntity.this.worldPosition,
                            ChemicalReactorMachineEntity.this.level.getBlockState(ChemicalReactorMachineEntity.this.worldPosition),
                            ChemicalReactorMachineEntity.this.level.getBlockState(ChemicalReactorMachineEntity.this.worldPosition), 2);
                }
                return value;
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> ChemicalReactorMachineEntity.this.currentProgress;
                    case 1 -> ChemicalReactorMachineEntity.this.maxProgress;
                    case 2 -> ChemicalReactorMachineEntity.this.energyStorage.getEnergyStored();
                    case 3 -> ChemicalReactorMachineEntity.this.energyStorage.getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> ChemicalReactorMachineEntity.this.currentProgress = value;
                    case 1 -> ChemicalReactorMachineEntity.this.maxProgress = value;
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
        nbt.put("fluid_input_1", fluidInput1.writeToNBT(new CompoundTag()));
        nbt.put("fluid_input_2", fluidInput2.writeToNBT(new CompoundTag()));
        nbt.put("fluid_output", fluidOutput.writeToNBT(new CompoundTag()));
        nbt.putInt("progress", currentProgress);
        nbt.putInt("max_progress", maxProgress);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventoryHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.energyStorage.deserializeNBT(nbt.get("energy_storage"));
        this.fluidInput1.readFromNBT(nbt.getCompound("fluid_input_1"));
        this.fluidInput2.readFromNBT(nbt.getCompound("fluid_input_2"));
        this.fluidOutput.readFromNBT(nbt.getCompound("fluid_output"));
        this.currentProgress = nbt.getInt("progress");
        this.maxProgress = nbt.getInt("max_progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.chemical_reactor");
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
        else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidTankO.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> inventoryHandler);
        lazyFluidTankI1 = LazyOptional.of(() -> fluidInput1);
        lazyFluidTankI2 = LazyOptional.of(() -> fluidInput2);
        lazyFluidTankO  = LazyOptional.of(() -> fluidOutput);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidTankI1.invalidate();
        lazyFluidTankI2.invalidate();
        lazyFluidTankO.invalidate();
    }

    public ItemStackHandler getInventory() {
        return inventoryHandler;
    }

    public java.util.List<FluidStack> getFluidTanks() {
        // Return the fluids in the input tanks (the recipe will check these)
        return java.util.List.of(fluidInput1.getFluid(), fluidInput2.getFluid());
    }


    public @Nullable FluidTank getFluidTank(int id) {
        if (id == 0)
            return fluidInput1;
        if (id == 1)
            return fluidInput2;
        if (id == 2)
            return fluidOutput;
        return null;
    }

    public @Nullable FluidStack getFluidForRendering() {
        if (!fluidOutput.isEmpty())
            return fluidOutput.getFluid();
        if (!fluidInput1.isEmpty())
            return fluidInput1.getFluid();
        if (!fluidInput2.isEmpty())
            return fluidInput2.getFluid();
        else return null;
    }

    public int getProgress() {
        return currentProgress;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ChemicalReactorMachineMenu(containerId, inventory, this, this.data);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        extractFluids();

        if (shouldFlush) {
            this.fluidInput1.setFluid(FluidStack.EMPTY);
            this.fluidInput2.setFluid(FluidStack.EMPTY);
            this.fluidOutput.setFluid(FluidStack.EMPTY);
            shouldFlush = false;

            this.setChanged();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                this.level.playSound(null, this.worldPosition, SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                WheatdeathoftheuniverseModVariables.WorldVariables.get(this.level).Temperature += 10;
            }
        }

        if (soundProgress >= 137)
            soundProgress = 0;

        if (hasValidRecipe()) {
            progress();
            if (soundProgress % 137 == 1 && this.level != null) {
                this.level.playSound(null, this.worldPosition, SoundRegistry.CHEMICAL_REACTOR_SOUND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                shouldStopSound = false;
            }
            setChanged(level, blockPos, blockState);
            if (currentProgress >= maxProgress) {
                finishRecipe();
                currentProgress = 0;
            }
        } else {
            currentProgress = 0;
            if (this.level != null && shouldStopSound) {
                ((ServerLevel) this.level).getPlayers(serverPlayer -> true)
                        .forEach(serverPlayer -> serverPlayer.connection.send(new ClientboundStopSoundPacket(SoundRegistry.CHEMICAL_REACTOR_SOUND.get().getLocation(), SoundSource.BLOCKS)));
                soundProgress = 0;
            }
            shouldStopSound = true;
        }
    }

    private void extractFluids() {
        // Extract fluid from items into input tanks
        for (int fluidInputSlot = FLUID_INPUT_SLOT_1; fluidInputSlot <= FLUID_INPUT_SLOT_2; fluidInputSlot++) {
            FluidTank tank = switch (fluidInputSlot) {
                case FLUID_INPUT_SLOT_1 -> fluidInput1;
                case FLUID_INPUT_SLOT_2 -> fluidInput2;
                default -> throw new IllegalStateException("Unexpected value: " + fluidInputSlot);
            };
            ItemStack fluidItemStack = inventoryHandler.getStackInSlot(fluidInputSlot);

            if (this.level != null && !fluidItemStack.isEmpty() && tank.getFluidAmount() < tank.getCapacity()) {
                Optional<FluidStack> fluidOpt = FluidUtil.getFluidContained(fluidItemStack);
                if (fluidOpt.isPresent() && !fluidOpt.get().isEmpty()) {
                    if(!tank.getFluid().isFluidEqual(fluidOpt.get()) && !tank.isEmpty())
                        return;

                    FluidActionResult result = FluidUtil.tryEmptyContainer(fluidItemStack, tank, tank.getSpace(), null, true);
                    if (result.isSuccess()) {
                        inventoryHandler.setStackInSlot(fluidInputSlot, result.getResult());
                    } else {
                        inventoryHandler.setStackInSlot(fluidInputSlot, ItemStack.EMPTY);
                    }
                }
            }
        }

        // Extract fluid from output tank into item
        ItemStack fluidItemStack = inventoryHandler.getStackInSlot(FLUID_OUTPUT_SLOT);
        if (this.level != null && !fluidItemStack.isEmpty() && fluidOutput.getFluidAmount() > 0) {
            if (fluidItemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                if (FluidUtil.getFluidContained(fluidItemStack).isPresent())
                    if (!fluidOutput.getFluid().isFluidEqual(FluidUtil.getFluidContained(fluidItemStack).get()) && FluidUtil.getFluidContained(fluidItemStack).isEmpty())
                        return;
                FluidActionResult result = FluidUtil.tryFillContainer(fluidItemStack, fluidOutput, Integer.MAX_VALUE, null, true);
                if (result.isSuccess()) {
                    inventoryHandler.setStackInSlot(FLUID_OUTPUT_SLOT, result.getResult());
                }
            }
        }
    }

    private void finishRecipe() {
        Optional<ChemicalReactorRecipe> recipeOpt = getCurrentRecipe();
        if (recipeOpt.isEmpty()) {
            WheatRevolution.LOGGER.warn("Chemical Reactor tried to craft empty recipe!");
            return;
        }
        ChemicalReactorRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.getResultItem(null);
        FluidStack fluidResult = recipe.getFluidOutput();
        FluidStack fluidResultCopy = fluidResult.copy();
        extractItems(recipe);
        extractFluidsForRecipe(recipe);
        this.inventoryHandler.setStackInSlot(OUTPUT_SLOT,
                new ItemStack(result.getItem(), this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));
        if (fluidResultCopy.getFluid() == fluidOutput.getFluid().getFluid()) {
            int amount = fluidResult.getAmount();
            fluidResultCopy.setAmount(fluidOutput.getFluidAmount() + amount);
            if (fluidResultCopy.getAmount() > fluidOutput.getCapacity()) {
                fluidResultCopy.setAmount(fluidOutput.getCapacity());
            }
        } else if (this.level != null && (!fluidOutput.isEmpty() || fluidOutput.getFluidAmount() > 0)) {
            // Because the chemical reactor dumps some chemicals
            WheatdeathoftheuniverseModVariables.WorldVariables.get(this.level).Temperature += 5.0;
        }
        fluidResult = fluidResultCopy;
        this.fluidOutput.setFluid(fluidResult);
        currentProgress = 0;
    }

    private void extractItems(ChemicalReactorRecipe recipe) {
        for (CountIngredient countIngredient : recipe.getItemInputs()) {
            int remaining = countIngredient.count();
            for (int slot = 0; slot < 2 && remaining > 0; slot++) {
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

    private void extractFluidsForRecipe(ChemicalReactorRecipe recipe) {
        for (FluidStack required : recipe.getFluidInputs()) {
            int remaining = required.getAmount();
            remaining = drainFromTank(fluidInput1, required, remaining);
            remaining = drainFromTank(fluidInput2, required, remaining);
        }
    }

    private int drainFromTank(FluidTank tank, FluidStack required, int amountToDrain) {
        if (tank.getFluid().isFluidEqual(required)) {
            int drained = tank.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE).getAmount();
            return amountToDrain - drained;
        }
        return amountToDrain;
    }


    private void progress() {
        this.currentProgress++;
        this.soundProgress++;
        this.energyStorage.extractEnergy(4, false);
    }

    private boolean hasValidRecipe() {
        Optional<ChemicalReactorRecipe> recipeOpt = getCurrentRecipe();
        if (recipeOpt.isEmpty()) return false;

        ChemicalReactorRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.getResultItem(null);
        this.maxProgress = recipe.getTime();
        return canOutput(result.getCount(), result.getItem()) && this.energyStorage.getEnergyStored() >= 4;
    }


    private Optional<ChemicalReactorRecipe> getCurrentRecipe() {
        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(ChemicalReactorRecipe.Type.INSTANCE, this, level);
    }


    private boolean canOutput(int count, Item item) {
        boolean doesOutputHaveTheRightItem = this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).is(item);
        return doesOutputHaveTheRightItem && this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.inventoryHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    public void flush() {
        this.shouldFlush = true;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            if (inventoryHandler.getStackInSlot(i) != ItemStack.EMPTY)
                return false;
        }

        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        return inventoryHandler.getStackInSlot(i);
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        inventoryHandler.extractItem(i, count, false);
        this.setChanged();
        if (this.level != null)
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        return inventoryHandler.getStackInSlot(i);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        inventoryHandler.extractItem(i, 1, false);
        return inventoryHandler.getStackInSlot(i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        inventoryHandler.setStackInSlot(i, itemStack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            inventoryHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
