package com.academic.hidepointer.mixin;

import com.academic.hidepointer.HidePointer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    // 这个Mixin确保旋转是帧率无关的
    @Inject(method = "update", at = @At("HEAD"))
    private void onCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, 
                               boolean inverseView, float tickDelta, CallbackInfo ci) {
        // 可以在这里添加帧率无关的旋转调整
    }
}
