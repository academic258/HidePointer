package com.academic.hidepointer.mixin;

import com.academic.hidepointer.HidePointer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseHandlerMixin {
    @Shadow @Final private MinecraftClient client;
    
    // 防止过于频繁的中心重置
    private int resetCooldown = 0;
    
    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouse(CallbackInfo ci) {
        // 冷却计数
        if (resetCooldown > 0) {
            resetCooldown--;
        }
        
        // 基本安全检查
        if (this.client.getWindow() == null || this.client.player == null) {
            return;
        }
        
        // 只在实验模式开启时执行
        if (!HidePointer.experimentalMode) {
            return;
        }
        
        long windowHandle = this.client.getWindow().getHandle();
        
        // 检查窗口焦点和光标模式
        if (GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED) == 0) {
            return;
        }
        
        int cursorMode = GLFW.glfwGetInputMode(windowHandle, GLFW.GLFW_CURSOR);
        boolean cursorShouldBeHidden = (cursorMode == GLFW.GLFW_CURSOR_DISABLED);
        
        // 只在光标应隐藏时执行
        if (!cursorShouldBeHidden) {
            return;
        }
        
        // 限制重置频率（每5帧重置一次，避免卡顿）
        if (resetCooldown <= 0) {
            // 获取窗口中心坐标
            int windowWidth = this.client.getWindow().getWidth();
            int windowHeight = this.client.getWindow().getHeight();
            double centerX = windowWidth / 2.0;
            double centerY = windowHeight / 2.0;
            
            // 获取当前鼠标位置
            double[] xPos = new double[1];
            double[] yPos = new double[1];
            GLFW.glfwGetCursorPos(windowHandle, xPos, yPos);
            
            // 如果鼠标偏离中心超过一定距离，则重置
            double distance = Math.sqrt(
                Math.pow(xPos[0] - centerX, 2) + 
                Math.pow(yPos[0] - centerY, 2)
            );
            
            // 偏离超过50像素时重置
            if (distance > 50.0) {
                GLFW.glfwSetCursorPos(windowHandle, centerX, centerY);
                resetCooldown = 5; // 冷却5帧
            }
        }
    }
}
