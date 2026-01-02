package com.academic.hidepointer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import com.mojang.brigadier.context.CommandContext;

public class HidePointer implements ClientModInitializer {
    // 标准模式开关
    public static boolean standardModeEnabled = false;
    // 旋转灵敏度 (标准值通常在0.1-0.2)
    public static double sensitivity = 0.15;
    
    // 调试信息
    public static int centerResetCount = 0;
    public static double lastRawDeltaX = 0;
    public static double lastRawDeltaY = 0;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // 标准模式切换指令
            dispatcher.register(ClientCommandManager.literal("stdcam")
                .executes(context -> {
                    standardModeEnabled = !standardModeEnabled;
                    String status = standardModeEnabled ? "§a标准模式启用" : "§c标准模式禁用";
                    context.getSource().sendFeedback(Text.literal(
                        "§7[HidePointer] " + status + " §7(灵敏度: §e" + sensitivity + "§7)"
                    ));
                    if (standardModeEnabled) {
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6严格按标准流程：锁定->读取->旋转->重置"));
                        centerResetCount = 0; // 重置计数
                    }
                    return 1;
                })
            );
            
            // 灵敏度精细调整
            dispatcher.register(ClientCommandManager.literal("stdsens")
                .then(ClientCommandManager.literal("vlow")
                    .executes(context -> { 
                        sensitivity = 0.08; 
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6标准模式灵敏度: §e" + sensitivity));
                        return 1; 
                    }))
                .then(ClientCommandManager.literal("low")
                    .executes(context -> { 
                        sensitivity = 0.12; 
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6标准模式灵敏度: §e" + sensitivity));
                        return 1; 
                    }))
                .then(ClientCommandManager.literal("medium")
                    .executes(context -> { 
                        sensitivity = 0.15; 
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6标准模式灵敏度: §e" + sensitivity));
                        return 1; 
                    }))
                .then(ClientCommandManager.literal("high")
                    .executes(context -> { 
                        sensitivity = 0.20; 
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6标准模式灵敏度: §e" + sensitivity));
                        return 1; 
                    }))
                .then(ClientCommandManager.literal("vhigh")
                    .executes(context -> { 
                        sensitivity = 0.25; 
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6标准模式灵敏度: §e" + sensitivity));
                        return 1; 
                    }))
            );
            
            // 调试信息
            dispatcher.register(ClientCommandManager.literal("stddebug")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal(
                        String.format("§7[HidePointer] 中心重置次数: %d | 最后增量: X=%.2f, Y=%.2f", 
                            centerResetCount, lastRawDeltaX, lastRawDeltaY)
                    ));
                    return 1;
                })
            );
        });
    }
}
