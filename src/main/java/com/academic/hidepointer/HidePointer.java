package com.academic.hidepointer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HidePointer implements ClientModInitializer {
    // 实验模式开关
    public static boolean experimentalMode = false;
    
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("hidepointer")
                .executes(context -> {
                    return toggleCursorMode(context.getSource());
                })
            );
            
            // 状态查询指令
            dispatcher.register(ClientCommandManager.literal("hidepointerstatus")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal(
                        "§7[HidePointer] 实验模式: " + 
                        (experimentalMode ? "§a开启" : "§7关闭")
                    ));
                    return 1;
                })
            );
        });
    }

    private int toggleCursorMode(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source) {
        MinecraftClient client = MinecraftClient.getInstance();
        long window = client.getWindow().getHandle();
        
        // 切换实验模式
        experimentalMode = !experimentalMode;
        
        // 始终尝试标准API
        try {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        } catch (Exception e) {
            // 静默失败
        }
        
        // 反馈
        if (experimentalMode) {
            source.sendFeedback(Text.literal("§7[HidePointer] §a实验模式已开启。尝试光标居中融合。"));
            source.sendFeedback(Text.literal("§7[HidePointer] §e移动鼠标，观察光标是否更'跟手'。"));
        } else {
            source.sendFeedback(Text.literal("§7[HidePointer] §7实验模式已关闭。"));
        }
        
        return 1;
    }
}
