package com.vidarin.wheatrevolution.item;

import net.mcreator.wheat_death_of_the_universe.entity.BlackholeprojEntity;
import net.mcreator.wheat_death_of_the_universe.init.WheatdeathoftheuniverseModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class NullifierItem extends Item {
    public NullifierItem() {
        super(new Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("mode")) {
            tag.putString("mode", "beam");
        }

        if (player.isShiftKeyDown() && !level.isClientSide()) {
            String mode = tag.getString("mode");
            switch (mode) {
                case "beam" -> tag.putString("mode", "arc");
                case "arc" -> tag.putString("mode", "burst");
                case "burst" -> tag.putString("mode", "beam");
            }

            Component modeName = Component.translatable("item.wheatrevolution.nullifier.mode." + tag.getString("mode"));
            player.displayClientMessage(modeName, false);

            level.playSound(null, player.blockPosition(),
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(
                            new ResourceLocation("wheatdeathoftheuniverse:buttonslow"))),
                    SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else if (!level.isClientSide() && player instanceof ServerPlayer) {
            String mode = tag.getString("mode");
            Vec3 look = player.getViewVector(1.0F);
            Vec3 worldUp = new Vec3(0, 1, 0);

            if (mode.equals("beam")) {
                for (int i = 0; i <= 20; i++) {
                    BlackholeprojEntity.shoot(level, player, level.getRandom(), 10, 1, 5);
                }
            } else if (mode.equals("arc")) {
                Vec3 right = worldUp.cross(look).normalize();
                for (int i = 0; i <= 30; i++) {
                    BlackholeprojEntity projectile = new BlackholeprojEntity(WheatdeathoftheuniverseModEntities.BLACKHOLEPROJ.get(), player, level);
                    float spread = -15.0F + i; // Spread from -15° to +15°
                    Vec3 direction = look.add(right.scale(spread * 0.1F)).normalize();

                    projectile.shoot(direction.x, direction.y, direction.z, 20.0F, 0.0F);
                    projectile.setSilent(true);
                    projectile.setCritArrow(false);
                    projectile.setBaseDamage(1);
                    projectile.setKnockback(5);
                    level.addFreshEntity(projectile);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(
                                new ResourceLocation("entity.arrow.shoot"))),
                        SoundSource.PLAYERS, 1.0F, 1.0F / (level.random.nextFloat() * 0.5F + 1.0F) + 10.0F / 2.0F);
            } else { // Burst
                Vec3 right = worldUp.cross(look).normalize();
                Vec3 up = look.cross(right).normalize();
                for (int i = 0; i <= 5; i++) {
                    for (int j = 0; j <= 5; j++) {
                        BlackholeprojEntity projectile = new BlackholeprojEntity(WheatdeathoftheuniverseModEntities.BLACKHOLEPROJ.get(), player, level);
                        float xOffset = (j - 2) * 5.0F; // Horizontal spread
                        float yOffset = (i - 2) * 5.0F; // Vertical spread
                        Vec3 direction = look.add(right.scale(xOffset * 0.1F))
                                .add(up.scale(yOffset * 0.1F))
                                .normalize();

                        projectile.shoot(direction.x, direction.y, direction.z, 20.0F, 0.0F);
                        projectile.setSilent(true);
                        projectile.setCritArrow(false);
                        projectile.setBaseDamage(1);
                        projectile.setKnockback(5);
                        level.addFreshEntity(projectile);
                    }
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(
                                new ResourceLocation("entity.arrow.shoot"))),
                        SoundSource.PLAYERS, 1.0F, 1.0F / (level.random.nextFloat() * 0.5F + 1.0F) + 10.0F / 2.0F);
            }
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.wheatrevolution.nullifier.desc"));
        if (stack.getTag() != null) {
            if (!stack.getTag().getString("mode").isEmpty()) {
                tooltip.add(Component.translatable("item.wheatrevolution.nullifier.mode." + stack.getTag().getString("mode")));
            } else tooltip.add(Component.translatable("item.wheatrevolution.nullifier.mode.beam"));
        } else tooltip.add(Component.translatable("item.wheatrevolution.nullifier.mode.beam"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}