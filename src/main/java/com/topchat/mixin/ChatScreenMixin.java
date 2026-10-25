package com.topchat.mixin;

import com.topchat.TopChat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    // Chat her acildiginda ayar dosyasini yeniden oku
    @Inject(method = "init", at = @At("HEAD"))
    private void topchat$reload(CallbackInfo ci) {
        TopChat.loadConfig();
    }

    // Yazi kutusunu (ve arka planini) klavyenin ustune kaydir
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void topchat$pre(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!TopChat.enabled) return;
        Screen self = (Screen) (Object) this;
        float off = TopChat.keyboardOffset(self.height);
        TopChat.currentKeyboardOffset = off;
        TopChat.inChatScreen = true;
        graphics.pose().pushMatrix();
        graphics.pose().translate(0f, -off);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void topchat$post(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!TopChat.enabled) return;
        graphics.pose().popMatrix();
        TopChat.inChatScreen = false;
        TopChat.currentKeyboardOffset = 0f;
    }
}
