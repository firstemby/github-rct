package com.gitlab.srcmc.rctmod.world.items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries.Items;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.ModClient;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.gitlab.srcmc.rctmod.network.TrainerTargetPayload;

import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;

public class TrainerCard extends Item {
    public static final int SYNC_INTERVAL_TICKS = 5;

    public static boolean has(Player player) {
        var tc = Items.TRAINER_CARD.get();
        return player.getInventory().contains(stack -> stack.is(tc));
    }

    private Map<UUID, Integer> playerInvTickCounts = new HashMap<>();

    public TrainerCard() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || (ModCommon.player != null && TargetArrowRenderer.getInstance().hasTarget() && ModCommon.player.get().getInventory().contains(stack));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean bl) {
        if(!level.isClientSide) {
            if(entity.tickCount % SYNC_INTERVAL_TICKS == 0) {
                if((entity instanceof ServerPlayer player) && entity.tickCount != this.playerInvTickCounts.getOrDefault(player, -1)) {
                    this.playerInvTickCounts.put(player.getUUID(), entity.tickCount);
                    var tpd = RCTMod.getInstance().getTrainerManager().getData(player);
                    var ts = RCTMod.getInstance().getTrainerSpawner();

                    if(tpd.isSeriesCompleted() || tpd.isInEmptySeries()) {
                        var closestTA = ts.getTASpawns().stream()
                            .sorted((t1, t2) ->  Double.compare(
                                t1.level().dimension().location().equals(player.level().dimension().location()) ? t1.distanceToSqr(player) : Double.MAX_VALUE,
                                t2.level().dimension().location().equals(player.level().dimension().location()) ? t2.distanceToSqr(player) : Double.MAX_VALUE))
                            .findFirst();

                        if(closestTA.isPresent()) {
                            var t = closestTA.get();
                            NetworkManager.sendToPlayer(player, new TrainerTargetPayload(
                                t.position().x,
                                t.position().y,
                                t.position().z,
                                !t.level().dimension().location().equals(player.level().dimension().location())));                            
                        }
                    } else {
                        var ps = PlayerState.get(player);
                        var keyTrainer = ts.getSpawns()
                            .stream().filter(t -> t.couldBattleAgainst(player) && ps.isKeyTrainer(t.getTrainerId()))
                            .sorted((t1, t2) ->  Double.compare(
                                t1.level().dimension().location().equals(player.level().dimension().location()) ? t1.distanceToSqr(player) : Double.MAX_VALUE,
                                t2.level().dimension().location().equals(player.level().dimension().location()) ? t2.distanceToSqr(player) : Double.MAX_VALUE))
                            .findFirst();

                        if(keyTrainer.isPresent()) {
                            var t = keyTrainer.get();
                            NetworkManager.sendToPlayer(player, new TrainerTargetPayload(
                                t.position().x,
                                t.position().y,
                                t.position().z,
                                !t.level().dimension().location().equals(player.level().dimension().location())));
                        }
                    }
                }
            }
        }

        super.inventoryTick(stack, level, entity, i, bl);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if(level.isClientSide) {
            var otherHand = interactionHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            var isPlacingBlock = false;

            if(player.getItemInHand(otherHand).getItem() instanceof BlockItem) {
                var hit = Item.getPlayerPOVHitResult(level, player, Fluid.NONE);
                isPlacingBlock = hit.getType() == HitResult.Type.BLOCK;
            }

            if(!isPlacingBlock && !player.isUsingItem() && !(player.getItemInHand(otherHand).getItem() instanceof ShieldItem)) {
                ModClient.SCREENS.openTrainerCardScreen();
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
}
