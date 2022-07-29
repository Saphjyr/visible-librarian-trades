package net.vltrades;

import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.EntityList;
import net.minecraft.entity.passive.VillagerEntity;

public class EnchantmentManager {

    private int clock;
	private EntityList villagerList = new EntityList();
	private final HashMap<VillagerEntity,Pair<Enchantment,Integer>> enchantments = new HashMap<VillagerEntity,Pair<Enchantment,Integer>>();
    private final HashMap<VillagerEntity,IconSpec> icons = new HashMap<VillagerEntity,IconSpec>();
	private final HashMap<VillagerEntity,Integer> statuses = new HashMap<VillagerEntity,Integer>();
	private final Queue<VillagerEntity> queue = new ConcurrentLinkedQueue<VillagerEntity>();
	private VillagerEntity currentVillager = null;
	private VillagerEntity oldCurrent = null;
    private Boolean toBeCleaned = false;

    public EnchantmentManager() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
			clientTick(client);
		});
    }

    public void updateVillager(VillagerEntity villager, MinecraftClient client) {
		if (villager.distanceTo(client.player) >= 4) {
			return;
		}
		this.currentVillager = villager;

        // Check if player have a villager egg in hand
		Hand playerHand = client.player.getActiveHand();
		if (!(client.player.getStackInHand(playerHand).getItem() == Items.VILLAGER_SPAWN_EGG)) {

            // Send interact packet
			PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.interact(villager, client.player.isSneaking(), playerHand) ;
			client.player.networkHandler.sendPacket(packet);
		} else {
			queueVillager(villager);
		}
	}

	public void queueVillager(VillagerEntity villager) {
		if (!this.queue.contains(villager)) {
			this.queue.offer(villager);
		}
	}

    public Boolean isTrackingDone() {
        return queue.isEmpty();
    }

    public Boolean isWaitingForPacket() {
        return currentVillager != null;
    }

    public void addEnchantToCurrentVillager(@Nullable Pair<Enchantment,Integer> enchants) {
        this.enchantments.put(this.currentVillager, enchants);
        this.statuses.put(this.currentVillager, 1);
        this.icons.put(this.currentVillager, IconSpec.createIcon(enchants));
        this.currentVillager = null;
    }

    public Pair<Enchantment,Integer> getEnchant(VillagerEntity villager) {
        return this.enchantments.get(villager);
    }

    public IconSpec getIcon(VillagerEntity villager) {
        return this.icons.get(villager);
    }

    private void clientTick(MinecraftClient client) {
        // Every 2 second : Register all visible vilagers
        if (this.clock >= 39) {
            if (client.world != null) {
                
                // Clear current villager if no response have been recieved
                if (this.currentVillager != null && this.currentVillager == this.oldCurrent) {
                    this.currentVillager = null;
                }
                this.oldCurrent = this.currentVillager;

                // List all Villagers
                this.villagerList = new EntityList();
                for (Entity e : client.world.getEntities()) {
                    if (e instanceof VillagerEntity) {
                        VillagerEntity v = (VillagerEntity)e;
                        if (!v.isBaby()) villagerList.add(v);
                    }
                }
            }
            else {
                this.reset();
            }
            this.clock = 0;
        }

        // Every second : Check if new villagers in range of interraction
        if (this.clock%20==0) {
            if (client.world != null) {
                this.villagerList.forEach((entity -> {
                    if  (entity != null) {
                        if (entity instanceof VillagerEntity && entity.distanceTo(client.player) < 4) {
                            VillagerEntity villager = (VillagerEntity)entity;
                            if (villager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN) {
                                enchantments.putIfAbsent(villager, null);
                                statuses.putIfAbsent(villager, 0);
                            }
                        }
                    }
                }));

                if (!this.isWaitingForPacket()) {
                    for (VillagerEntity villager : this.enchantments.keySet()) {
                        if (statuses.get(villager).equals(0) && villager.distanceTo(client.player) < 4) {
                            queue.offer(villager);
                            break;
                        }
                    }
                }
            }
        }

        // Every tick
        this.clock++;
        if (this.currentVillager == null) {
            VillagerEntity current;
            if((current = queue.poll())!= null) {
                updateVillager(current, client);
            }
            if (this.toBeCleaned) {
                clean();
                this.toBeCleaned = false;
            }
        }
    }

    public void reset() {
        this.statuses.clear();
        this.enchantments.clear();
    }

    private void clean() {


        this.enchantments.keySet().removeIf(villager -> !villager.isAlive());
        this.statuses.keySet().removeIf(villager -> !villager.isAlive());

        this.enchantments.keySet().removeIf(villager -> villager.getVillagerData().getProfession() != VillagerProfession.LIBRARIAN);
        this.statuses.keySet().removeIf(villager -> villager.getVillagerData().getProfession() != VillagerProfession.LIBRARIAN);

        
    }

    public void setToBeCleaned() {
        this.toBeCleaned = true;
    }

    public Set<VillagerEntity> getTrackedVillagers() {
        return this.enchantments.keySet();
    }
}
