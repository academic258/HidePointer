package com.academic.hidepointer.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // 这个注入点确保旋转在渲染前已经应用
    @Inject(method = "renderWorld", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"
    ))
    private void onBeforeCameraUpdate(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        // 这里可以确保鼠标输入已经被处理
        // 在标准模式中，MouseMixin已经设置了cursorDeltaX/Y
    }
}
