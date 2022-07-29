package net.vltrades.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Pair;
import net.minecraft.village.TradeOffer;
import net.vltrades.EnchantmentManager;
import net.vltrades.VisibleLibrarianTradesClient;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientPlayPacketListener {

	@Inject(at = @At("HEAD"), method = "onSetTradeOffers", cancellable = true)
    public void onSetTradeOffers(SetTradeOffersS2CPacket packet, CallbackInfo ci) {
		boolean bookFound = false;
		EnchantmentManager enchantmentManager = VisibleLibrarianTradesClient.getEnchantmentManager();
        if (enchantmentManager.isWaitingForPacket()) {
			for (TradeOffer offer : packet.getOffers()) {
				ItemStack stack = offer.getSellItem();
				if (stack.getItem() instanceof EnchantedBookItem) {
					Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
					Enchantment enchant = enchants.keySet().iterator().next();
					Pair<Enchantment,Integer> pair = new Pair<Enchantment,Integer>(enchant, enchants.get(enchant));
					enchantmentManager.addEnchantToCurrentVillager(pair);
					bookFound = true;
				}
			}

			// The librarian is not selling an enchanted book
			if (!bookFound) {
				enchantmentManager.addEnchantToCurrentVillager(null);
			}

			// Update the job sites once all the villager's trades are tracked
			if (enchantmentManager.isTrackingDone()) {
				VisibleLibrarianTradesClient.lecternManager.updateAllJobSites();
			}
        }
    }
    
    @Inject(at = @At("HEAD"), method = "onOpenScreen", cancellable = true)
    public void onOpenScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
		EnchantmentManager enchantmentManager = VisibleLibrarianTradesClient.getEnchantmentManager();

        var type = packet.getScreenHandlerType();
        if (enchantmentManager.isWaitingForPacket() && type == ScreenHandlerType.MERCHANT) {
            ClientPlayNetworking.getSender().sendPacket(new CloseHandledScreenC2SPacket(packet.getSyncId()));
            ci.cancel();
        }
    }
	

	@Inject(at = @At("HEAD"), method = "onEntityStatus")
    public void onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && packet.getStatus() == 14) {
			Entity entity = packet.getEntity(client.world);
			if (entity instanceof VillagerEntity) {
				EnchantmentManager enchantmentManager = VisibleLibrarianTradesClient.getEnchantmentManager();
				enchantmentManager.queueVillager((VillagerEntity)entity);
			}
		}
    }
}