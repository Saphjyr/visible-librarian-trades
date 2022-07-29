package net.vltrades.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.LecternBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3f;
import net.vltrades.VisibleLibrarianTradesClient;

@Environment(EnvType.CLIENT)
@Mixin(LecternBlockEntityRenderer.class)
public abstract class LecternBlockEntityRendererMixin implements BlockEntityRenderer<LecternBlockEntity> {
	
	@Inject(at = @At("HEAD"), method = "render")
	private void render(LecternBlockEntity lecternBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo info) {
		Pair<String, Boolean> text = VisibleLibrarianTradesClient.lecternManager.getTextOfLectern(lecternBlockEntity);
		if (text != null) {
			String s = text.getLeft();
			int color = 0x000000;
			int outline = 0xffffff;
			if (text.getRight()) {
				color = 0xffffff;
				outline = 0xff8800;
			}
			BlockState blockState = lecternBlockEntity.getCachedState();

			matrixStack.push();

			matrixStack.translate(0.5, 0.5, 0.5);
            float h = -blockState.get(LecternBlock.FACING).asRotation();
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(h));
            matrixStack.translate(0.0, 0.40, 0.350);
			//matrixStack.translate(0.0, 0.8333333432674408, 0);
			matrixStack.scale(0.010416667f, -0.010416667f, 0.010416667f);
			matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(67.5F));
			
			MinecraftClient client = MinecraftClient.getInstance();
			
			float q = -client.textRenderer.getWidth(s) / 2;
			client.textRenderer.drawWithOutline(OrderedText.styledForwardsVisitedString(s, Style.EMPTY), q, (2 * 10 - 20), color, outline, matrixStack.peek().getPositionMatrix(), vertexConsumerProvider, i);

			matrixStack.pop();
		}
	}
}
