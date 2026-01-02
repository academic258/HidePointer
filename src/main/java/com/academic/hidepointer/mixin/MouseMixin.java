package com.academic.hidepointer.mixin;

import com.academic.hidepointer.RawInputCamMod;
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
        if (!RawInputCamMod.rawInputEnabled || client.getWindow() == null || client.player == null) {
            return;
        }

        long window = client.getWindow().getHandle();
        // 1. 获取当前绝对光标位置
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        GLFW.glfwGetCursorPos(window, xPos, yPos);
        double currentX = xPos[0];
        double currentY = yPos[0];

        // 2. 如果是第一帧，初始化位置
        if (lastCursorX == 0 && lastCursorY == 0) {
            lastCursorX = currentX;
            lastCursorY = currentY;
        }

        // 3. 计算原始增量 (delta)
        double deltaX = currentX - lastCursorX;
        double deltaY = currentY - lastCursorY;

        // 4. 存储增量，供后续旋转使用（替换引擎原本的计算）
        this.cursorDeltaX = deltaX * RawInputCamMod.sensitivity;
        this.cursorDeltaY = deltaY * RawInputCamMod.sensitivity;

        // 5. 【关键】尝试将光标“困”在窗口中央区域
        int width = client.getWindow().getWidth();
        int height = client.getWindow().getHeight();
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        // 如果光标跑出中央区域，把它拉回来
        double boundary = 100.0; // 距离中心100像素的边界
        if (Math.abs(currentX - centerX) > boundary || Math.abs(currentY - centerY) > boundary) {
            GLFW.glfwSetCursorPos(window, centerX, centerY);
            currentX = centerX;
            currentY = centerY;
        }

        // 6. 记录当前位置供下一帧使用
        lastCursorX = currentX;
        lastCursorY = currentY;
    }

    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void onUpdateMouseTail(CallbackInfo ci) {
        // 每帧结束时，不清零增量（如果需要，但我们已替换）
    }
}
