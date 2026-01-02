package com.academic.hidepointer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

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
                .then(ClientCommandManager.argument("sensitivity", net.minecraft.command.argument.FloatArgumentType.floatArg(0.01f, 5.0f))
                    .executes(context -> {
                        sensitivity = context.getArgument("sensitivity", Float.class);
                        context.getSource().sendFeedback(Text.literal("§7[RawInputCam] §6灵敏度已设置为: §e" + sensitivity));
                        return 1;
                    })
                )
            );
        });
    }
}
