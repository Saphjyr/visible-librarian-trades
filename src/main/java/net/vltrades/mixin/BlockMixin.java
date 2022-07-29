package net.vltrades.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vltrades.VisibleLibrarianTradesClient;

@Environment(EnvType.CLIENT)
@Mixin(Block.class)
public class BlockMixin {
	@Inject(at = @At("HEAD"), method = "onPlaced")
	private void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo info) {
		if (itemStack.getItem().equals(Items.LECTERN)) {
            VisibleLibrarianTradesClient.enchantmentManager.setToBeCleaned();
        }
	}
}
