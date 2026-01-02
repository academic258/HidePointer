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
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;
    
    // 新增：中心位置跟踪
    private double virtualCenterX = 0;
    private double virtualCenterY = 0;
    private boolean needsInitialCenter = true;
    
    // 新增：平滑处理
    private double smoothedDeltaX = 0;
    private double smoothedDeltaY = 0;
    private static final double SMOOTH_FACTOR = 0.3;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouseHead(CallbackInfo ci) {
        if (!HidePointer.rawInputEnabled || client.getWindow() == null || client.player == null) {
            needsInitialCenter = true; // 重置
            return;
        }

        long window = client.getWindow().getHandle();
        int width = client.getWindow().getWidth();
        int height = client.getWindow().getHeight();
        
        // 初始化虚拟中心（仅第一次）
        if (needsInitialCenter) {
            virtualCenterX = width / 2.0;
            virtualCenterY = height / 2.0;
            needsInitialCenter = false;
            
            // 尝试将实际光标移到中心（不一定成功，但尝试）
            GLFW.glfwSetCursorPos(window, virtualCenterX, virtualCenterY);
        }
        
        // 获取当前实际光标位置
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        GLFW.glfwGetCursorPos(window, xPos, yPos);
        double currentX = xPos[0];
        double currentY = yPos[0];
        
        // === 核心修正：计算相对于虚拟中心的即时偏移 ===
        double rawDeltaX = currentX - virtualCenterX;
        double rawDeltaY = currentY - virtualCenterY;
        
        // 调试记录
        HidePointer.lastDeltaX = rawDeltaX;
        HidePointer.lastDeltaY = rawDeltaY;
        
        // 平滑处理（减少抖动）
        smoothedDeltaX = smoothedDeltaX * (1 - SMOOTH_FACTOR) + rawDeltaX * SMOOTH_FACTOR;
        smoothedDeltaY = smoothedDeltaY * (1 - SMOOTH_FACTOR) + rawDeltaY * SMOOTH_FACTOR;
        
        // === 关键：立即应用旋转，无延迟 ===
        // 这里直接修改游戏用于旋转的delta值
        this.cursorDeltaX = smoothedDeltaX * HidePointer.sensitivity;
        this.cursorDeltaY = smoothedDeltaY * HidePointer.sensitivity;
        
        // === 将虚拟中心向鼠标方向移动，模拟"光标跟随" ===
        // 这解决了"鼠标不动还转"的问题
        double followSpeed = 0.7; // 跟随速度（0-1）
        virtualCenterX = virtualCenterX * followSpeed + currentX * (1 - followSpeed);
        virtualCenterY = virtualCenterY * followSpeed + currentY * (1 - followSpeed);
        
        // === 尝试将实际光标拉向虚拟中心（不一定成功）===
        // 如果系统允许，这会让光标更接近中心
        double pullStrength = 0.3; // 拉回力度
        double targetX = currentX * (1 - pullStrength) + virtualCenterX * pullStrength;
        double targetY = currentY * (1 - pullStrength) + virtualCenterY * pullStrength;
        
        // 检查是否显著偏离
        double distance = Math.sqrt(
            Math.pow(currentX - virtualCenterX, 2) + 
            Math.pow(currentY - virtualCenterY, 2)
        );
        
        // 只有偏离较大时才尝试拉回，避免抖动
        if (distance > 50.0) {
            GLFW.glfwSetCursorPos(window, targetX, targetY);
        }
    }
    
    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void onUpdateMouseTail(CallbackInfo ci) {
        // 可选：在帧结束后重置delta，防止累积
        if (!HidePointer.rawInputEnabled) {
            smoothedDeltaX = 0;
            smoothedDeltaY = 0;
        }
    }
}
