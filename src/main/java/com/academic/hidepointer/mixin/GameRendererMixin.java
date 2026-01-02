package com.academic.hidepointer.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // 修正方法签名：参数必须与目标方法 renderWorld 完全一致
    @Inject(method = "renderWorld", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"
    ))
    private void onBeforeCameraUpdate(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        // 注意：这里不需要做任何事情，只是为了确保注入点存在
        // MouseMixin 已经处理了旋转逻辑
    }
}
