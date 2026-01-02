package com.academic.hidepointer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class HidePointer implements ClientModInitializer {
    public static boolean rawInputEnabled = false;
    public static double sensitivity = 0.002; // 更科学的灵敏度范围
    
    // 调试统计
    public static double lastDeltaX = 0;
    public static double lastDeltaY = 0;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("rawcam")
                .executes(context -> {
                    rawInputEnabled = !rawInputEnabled;
                    String status = rawInputEnabled ? "§a启用" : "§c禁用";
                    context.getSource().sendFeedback(Text.literal(
                        "§7[HidePointer] " + status + " §7(灵敏度: §e" + sensitivity + "§7)"
                    ));
                    if (rawInputEnabled) {
                        context.getSource().sendFeedback(Text.literal("§7[HidePointer] §b测试：缓慢移动鼠标看视角是否跟手"));
                    }
                    return 1;
                })
            );
            
            // 精细灵敏度调整
            for (double sens : new double[]{0.0005, 0.001, 0.002, 0.004, 0.008}) {
                final double finalSens = sens;
                dispatcher.register(ClientCommandManager.literal("sens" + (int)(sens*10000))
                    .executes(context -> {
                        sensitivity = finalSens;
                        context.getSource().sendFeedback(Text.literal(
                            "§7[HidePointer] §6灵敏度: §e" + sensitivity
                        ));
                        return 1;
                    })
                );
            }
            
            // 调试信息
            dispatcher.register(ClientCommandManager.literal("rawcamdebug")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal(
                        String.format("§7[HidePointer] 最后增量: X=%.3f, Y=%.3f", lastDeltaX, lastDeltaY)
                    ));
                    return 1;
                })
            );
        });
    }
}
