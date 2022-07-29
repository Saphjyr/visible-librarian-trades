package net.vltrades;

import java.util.HashMap;
import java.util.Optional;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class LecternManager {
    private final HashMap<LecternBlockEntity,Optional<String>> names = new HashMap<LecternBlockEntity,Optional<String>>();
    private final HashMap<LecternBlockEntity,Optional<Boolean>> maxed = new HashMap<LecternBlockEntity,Optional<Boolean>>();

    private Integer clock;

    public LecternManager() {
        this.clock = 0;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
			clientTick(client);
		});
    }

    public void updateAllJobSites() {
        for (LecternBlockEntity lectern : names.keySet()) {
            updateOne(lectern);
        }
    }

    public void updateOne(LecternBlockEntity lectern) {

        EnchantmentManager enchantmentManager = VisibleLibrarianTradesClient.getEnchantmentManager();
        
        float minDist = 2.5F;
        float currDist;
        VillagerEntity closestVillager = null;
        for (VillagerEntity villager : enchantmentManager.getTrackedVillagers()) {
            currDist = distance(lectern, villager);
            if (currDist < minDist) {
                closestVillager = villager;
                minDist = currDist;
            }
        }

        // If no close villager found
        if (closestVillager == null) {
            this.names.put(lectern, Optional.empty());
            this.maxed.put(lectern, Optional.empty());
        }

        // If a close villager is found
        else {
            Pair<Optional<String>, Optional<Boolean>> textData = enchantToText(enchantmentManager.getEnchant(closestVillager));
            this.names.put(lectern, textData.getLeft());
            this.maxed.put(lectern, textData.getRight());
        }

    }

    public Pair<String, Boolean> getTextOfLectern(LecternBlockEntity lectern) {
        Optional<String> text = this.names.get(lectern);
        Optional<Boolean> isMaxed = this.maxed.get(lectern);
        if (text == null || isMaxed == null) {
            updateOne(lectern);
            text = this.names.get(lectern);
            isMaxed = this.maxed.get(lectern);
        }
        if (text.isPresent() && isMaxed.isPresent()) return new Pair<String, Boolean>(text.get(), isMaxed.get()) ;
        else return null;
    }

    private static float distance(LecternBlockEntity lectern, VillagerEntity villager) {
        BlockPos pos = lectern.getPos();
        float dx = (float)(pos.getX() - villager.getX());
        float dy = (float)(pos.getY() - villager.getY());
        float dz = (float)(pos.getZ() - villager.getZ());
        return MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static Pair<Optional<String>, Optional<Boolean>> enchantToText(Pair<Enchantment,Integer> enchant) {
        if (enchant == null) {
            String res = Text.translatable(Items.BOOKSHELF.getTranslationKey()).getString();
            if (res.length() > 15) {
                res = res.substring(0, 12) + "..";
            }
            return new Pair<Optional<String>, Optional<Boolean>>(Optional.of(res), Optional.of(false));
        }
        String res = Text.translatable(enchant.getLeft().getTranslationKey()).getString();
        if (res.length() > 15) {
            res = res.substring(0, 12) + "..";
        }
        if (enchant.getLeft().getMaxLevel() != 1) {
            res += " ";
            res += intToText(enchant.getRight()).getString();
        }
        return new Pair<Optional<String>, Optional<Boolean>>(Optional.of(res), Optional.of(enchant.getLeft().getMaxLevel() == enchant.getRight()));
    }

    private static Text intToText(Integer num) {
        if (0 < num && num <= 10) {
            String key = "enchantment.level." + Integer.toString(num);
            return Text.translatable(key);
        }
        else {
            return Text.of(Integer.toString(num));
        }
    }

    private void clientTick(MinecraftClient client) {
        if (client.world != null) {
            if (clock >= 39) {
                updateAllJobSites();
                clock = 0;
            }
            clock ++;
        }
        else {
            if (!this.names.isEmpty()) {
                this.reset();
            }
        }
    }

    public void reset() {
        this.names.clear();
        this.maxed.clear();
        this.clock = 0;
    }
}
