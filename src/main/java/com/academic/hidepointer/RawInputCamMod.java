package com.academic.hidepointer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class RawInputCamMod implements ClientModInitializer {
    // 核心开关：是否启用原始输入视角控制
    public static boolean rawInputEnabled = false;
    // 鼠标灵敏度乘数
    public static double sensitivity = 0.1;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // 主开关指令
            dispatcher.register(ClientCommandManager.literal("rawcam")
                .executes(context -> {
                    rawInputEnabled = !rawInputEnabled;
                    context.getSource().sendFeedback(Text.literal(
                        "§7[RawInputCam] " + (rawInputEnabled ? "§a启用" : "§c禁用") +
                        "§7。鼠标灵敏度: §e" + sensitivity
                    ));
                    if (rawInputEnabled) {
                        context.getSource().sendFeedback(Text.literal("§7[RawInputCam] §b移动鼠标以转动视角。光标将被限制在窗口内。"));
                    }
                    return 1;
                })
                // 修正：使用正确的参数类型 - 在1.21.4中可能是DoubleArgumentType
                .then(ClientCommandManager.argument("sensitivity", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(0.01, 5.0))
                    .executes(context -> {
                        sensitivity = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(context, "sensitivity");
                        context.getSource().sendFeedback(Text.literal("§7[RawInputCam] §6灵敏度已设置为: §e" + sensitivity));
                        return 1;
                    })
                )
            );
        });
    }
}
