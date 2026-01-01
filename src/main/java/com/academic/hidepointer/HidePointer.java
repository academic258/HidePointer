package com.academic.hidepointer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HidePointer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 注册客户端指令 /hidepointer 和它的别名 /hp
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("hidepointer")
                .executes(context -> {
                    return executeHidePointer(context.getSource());
                })
            );
            dispatcher.register(ClientCommandManager.literal("hp")
                .executes(context -> {
                    return executeHidePointer(context.getSource());
                })
            );
        });
    }

    private int executeHidePointer(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source) {
        MinecraftClient client = MinecraftClient.getInstance();
        long window = client.getWindow().getHandle();

        // 核心操作：尝试通过GLFW禁用光标（将其锁定并隐藏）
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        // 向玩家发送反馈
        source.sendFeedback(Text.literal("§7[HidePointer] §f已尝试锁定/隐藏鼠标指针。"));
        return 1;
    }
}
