package net.vltrades;

import javax.annotation.Nullable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class IconSpec {
    public static String[] SUPPORTED = {"fire_aspect", "looting", "sharpness", "smite", "thorns", "bane_of_arthropods", "protection", "fire_protection", "feather_falling", "blast_protection", "projectile_protection", "respiration", "aqua_affinity", "depth_strider", "frost_walker", "soul_speed", "efficiency", "silk_touch", "unbreaking", "fortune"};
    public float red;
    public float green;
    public float blue;
    public Identifier logo;
    public Identifier background = new Identifier("vlt", "textures/entity/villager/enchant_icons/back.png");

    public void setColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static boolean isSupported(String key) {
        boolean result = false;
        int i = 0;
        while (!result && i<SUPPORTED.length) {
            if (SUPPORTED[i].equals(key)) {
                result = true;
            }
            i++;
        }
        return result;
    }

    public static IconSpec createIcon(@Nullable Pair<Enchantment,Integer> enchant) {
        if (enchant == null) return null;
        IconSpec spec = new IconSpec();
		if (enchant.getLeft().getMaxLevel() == enchant.getRight()) {
			spec.setColor(1.0f, 0.53f, 0.0f);
		}
		else {
			switch (enchant.getRight()) {
				case 1:
					spec.setColor(0.65f, 0.65f, 0.65f);
					break;
				case 2:
					spec.setColor(0.20f, 0.76f, 0.15f);
					break;
				case 3:
					spec.setColor(0.0f, 0.32f, 1.0f);
					break;
				case 4:
					spec.setColor(0.78f, 0.0f, 1.0f);
					break;
				case 5:
					spec.setColor(1.0f, 0.53f, 0.0f);
					break;
			
				default:
					break;
			}
		}
		String[] keys = enchant.getLeft().getTranslationKey().split("\\.");
		if (keys.length == 3 && IconSpec.isSupported(keys[2])) {
			spec.logo = new Identifier("vlt", "textures/entity/villager/enchant_icons/"+ keys[2] +".png");
		}
		else {
			spec.logo = new Identifier("vlt", "textures/entity/villager/enchant_icons/question.png");
		}
		
		return spec;
    }
}