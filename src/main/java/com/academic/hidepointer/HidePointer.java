package com.academic.hidepointer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class HidePointer implements ClientModInitializer {
    // 核心开关：是否启用原始输入视角控制
    public static boolean rawInputEnabled = false;
    // 鼠标灵敏度
    public static double sensitivity = 0.15;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // 主开关指令
            dispatcher.register(ClientCommandManager.literal("rawcam")
                .executes(context -> {
                    rawInputEnabled = !rawInputEnabled;
                    String status = rawInputEnabled ? "§a启用" : "§c禁用";
                    context.getSource().sendFeedback(Text.literal(
                        "§7[HidePointer] " + status + " §7(灵敏度: §e" + sensitivity + "§7)"
                    ));
                    if (rawInputEnabled) {
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §b移动鼠标以转动视角。"));
                    }
                    return 1;
                })
            );
            
            // 灵敏度调整指令（三档）
            dispatcher.register(ClientCommandManager.literal("rawcamsens")
                .then(ClientCommandManager.literal("high")
                    .executes(context -> {
                        sensitivity = 0.25;
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6高灵敏度已设置"));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("medium")
                    .executes(context -> {
                        sensitivity = 0.15;
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6中灵敏度已设置"));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("low")
                    .executes(context -> {
                        sensitivity = 0.08;
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §6低灵敏度已设置"));
                        return 1;
                    })
                )
            );
        });
    }
}
