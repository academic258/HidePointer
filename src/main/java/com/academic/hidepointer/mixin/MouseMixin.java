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

    private double lastCursorX = 0;
    private double lastCursorY = 0;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouseHead(CallbackInfo ci) {
        // 只在我们的模式启用且游戏就绪时工作
        if (!HidePointer.rawInputEnabled || client.getWindow() == null || client.player == null) {
            // 重置记录，防止模式切换时出现跳跃
            if (!HidePointer.rawInputEnabled) {
                lastCursorX = 0;
                lastCursorY = 0;
            }
            return;
        }

        long window = client.getWindow().getHandle();
        
        // 1. 获取当前绝对光标位置
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        GLFW.glfwGetCursorPos(window, xPos, yPos);
        double currentX = xPos[0];
        double currentY = yPos[0];

        // 2. 初始化第一帧位置
        if (lastCursorX == 0 && lastCursorY == 0) {
            lastCursorX = currentX;
            lastCursorY = currentY;
            return; // 第一帧不计算增量
        }

        // 3. 计算原始增量 (delta)
        double deltaX = currentX - lastCursorX;
        double deltaY = currentY - lastCursorY;

        // 4. 应用灵敏度并存储增量（关键步骤：替换引擎原本的计算）
        this.cursorDeltaX = deltaX * HidePointer.sensitivity;
        this.cursorDeltaY = deltaY * HidePointer.sensitivity;

        // 5. 【核心】将光标"困"在窗口中央区域
        int width = client.getWindow().getWidth();
        int height = client.getWindow().getHeight();
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        // 边界值：距离中心100像素
        double boundary = 100.0;
        boolean shouldRecenter = false;
        
        // 检查是否超出边界
        if (Math.abs(currentX - centerX) > boundary) {
            shouldRecenter = true;
        }
        if (Math.abs(currentY - centerY) > boundary) {
            shouldRecenter = true;
        }
        
        // 如果超出边界，将其拉回中心
        if (shouldRecenter) {
            GLFW.glfwSetCursorPos(window, centerX, centerY);
            currentX = centerX;
            currentY = centerY;
        }

        // 6. 记录当前位置供下一帧使用
        lastCursorX = currentX;
        lastCursorY = currentY;
    }
}
