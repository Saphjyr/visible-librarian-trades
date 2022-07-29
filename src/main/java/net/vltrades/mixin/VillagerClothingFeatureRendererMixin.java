package net.vltrades.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.vltrades.EnchantmentManager;
import net.vltrades.IconSpec;
import net.vltrades.VisibleLibrarianTradesClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(VillagerClothingFeatureRenderer.class)
public abstract class VillagerClothingFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

	public VillagerClothingFeatureRendererMixin(FeatureRendererContext<T, M> context) {
		super(context);
	}
	
	@Inject(at = @At("TAIL"), method = "render")
	private void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo info) {
		if (((Entity)livingEntity).isInvisible()) {
            return;
        }
		EnchantmentManager enchantmentManager = VisibleLibrarianTradesClient.getEnchantmentManager();
		VillagerData villagerData = ((VillagerDataContainer)livingEntity).getVillagerData();
        VillagerProfession villagerProfession = villagerData.getProfession();
		EntityModel<T> entityModel = ((FeatureRenderer<T, M>)this).getContextModel();
		if (villagerProfession == VillagerProfession.LIBRARIAN && !((LivingEntity)livingEntity).isBaby()) {
			VillagerEntity villager = (VillagerEntity)livingEntity;
			IconSpec spec = enchantmentManager.getIcon(villager);
			if (spec != null) {
				// Render background 
				Identifier background = spec.background;
				VillagerClothingFeatureRenderer.renderModel(entityModel, background, matrixStack, vertexConsumerProvider, i, livingEntity, spec.red, spec.green, spec.blue);

				// Render Icon
				if (spec.logo != null) {
					VillagerClothingFeatureRenderer.renderModel(entityModel, spec.logo, matrixStack, vertexConsumerProvider, i, livingEntity, 1.0f, 1.0f, 1.0f);
				}
			}
		}
	}
}
