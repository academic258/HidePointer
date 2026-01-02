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
    
    // 跟踪状态
    private boolean wasModeEnabled = false;
    private double lastKnownX = 0;
    private double lastKnownY = 0;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouseHead(CallbackInfo ci) {
        MinecraftClient mc = this.client;
        
        // 安全检查
        if (mc.getWindow() == null || mc.player == null) {
            return;
        }
        
        long window = mc.getWindow().getHandle();
        boolean modeEnabled = HidePointer.standardModeEnabled;
        
        // 模式切换处理
        if (modeEnabled != wasModeEnabled) {
            wasModeEnabled = modeEnabled;
            if (!modeEnabled) {
                // 模式关闭：恢复光标可见性（尝试）
                try {
                    GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                } catch (Exception ignored) {}
                return;
            }
        }
        
        // === 核心：标准模式处理 ===
        if (!modeEnabled) {
            return;
        }
        
        // 确保游戏窗口有焦点
        if (GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == 0) {
            return;
        }
        
        int windowWidth = mc.getWindow().getWidth();
        int windowHeight = mc.getWindow().getHeight();
        double centerX = windowWidth / 2.0;
        double centerY = windowHeight / 2.0;
        
        // === 步骤1: 获取当前实际光标位置 ===
        double[] currentXArr = new double[1];
        double[] currentYArr = new double[1];
        GLFW.glfwGetCursorPos(window, currentXArr, currentYArr);
        double currentX = currentXArr[0];
        double currentY = currentYArr[0];
        
        // === 步骤2: 计算相对于中心的偏移量 ===
        // 这是最关键的变量：从中心到当前位置的向量
        double rawDeltaX = currentX - centerX;
        double rawDeltaY = currentY - centerY;
        
        // 存储用于调试
        HidePointer.lastRawDeltaX = rawDeltaX;
        HidePointer.lastRawDeltaY = rawDeltaY;
        
        // === 步骤3: 应用旋转 ===
        // 直接修改游戏用于旋转的delta值，无延迟
        this.cursorDeltaX = rawDeltaX * HidePointer.sensitivity;
        this.cursorDeltaY = rawDeltaY * HidePointer.sensitivity;
        
        // === 步骤4: 重置光标到中心 ===
        // 注意：这个调用可能被系统限制，但必须尝试
        try {
            GLFW.glfwSetCursorPos(window, centerX, centerY);
            HidePointer.centerResetCount++; // 计数成功重置次数
        } catch (Exception e) {
            // 系统可能不允许频繁设置光标位置
            // 这是PojavLauncher环境的主要限制点
        }
        
        // 记录最后已知位置
        lastKnownX = centerX; // 理论上光标现在在中心
        lastKnownY = centerY;
    }
    
    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void onUpdateMouseTail(CallbackInfo ci) {
        // 可选：在帧结束时可以做一些清理
        if (!HidePointer.standardModeEnabled) {
            // 确保delta被清除，避免残留
            this.cursorDeltaX = 0;
            this.cursorDeltaY = 0;
        }
    }
}
