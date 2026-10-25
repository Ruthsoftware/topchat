package com.topchat.mixin;

import com.topchat.TopChat;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * v3: Chat'i cizim matrisini kaydirarak degil, chat'in kullandigi "screenHeight"
 * degerini degistirerek yukari tasiyoruz. Hem cizim hem de tiklama/hover
 * (captureClickableText) ayni ozel metottan gectigi icin tiklanabilir alanlar
 * chat ile birlikte kayar.
 *
 * Hedef: ChatComponent icindeki ozel metot
 *   extractRenderState(ChatGraphicsAccess, int screenHeight, int ticks, DisplayMode)
 * Eger ilk parametrenin sinif adi farkliysa, hata mesajindaki gercek adi yaz.
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @ModifyVariable(
        method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private int topchat$moveChat(int screenHeight) {
        if (!TopChat.enabled) return screenHeight;

        Minecraft mc = Minecraft.getInstance();
        double scale = mc.options.chatScale().get();
        // vanilla formulu: yukseklik = oran * (180 - 20) + 20
        int h = (int) Math.floor(mc.options.chatHeightFocused().get() * (180 - 20) + 20);

        // Chat'in alt cizgisi vanilla'da screenHeight - 40. Ustu margin'e gelecek kadar yukari kaydir.
        float shiftUp = (screenHeight - 40) - (float) (h * scale) - TopChat.topMargin;
        if (shiftUp < 0f) shiftUp = 0f;

        // ChatScreen cizilirken tum ekran klavye kadar yukari kaydirildi, chat'i geri indir.
        float extra = TopChat.inChatScreen ? TopChat.currentKeyboardOffset : 0f;

        return Math.round(screenHeight - shiftUp + extra);
    }

    private static boolean topchat$configLoaded = false;

    // Gelen her chat mesaji buradan gecer. Yetkili sohbeti mesajini taniyip "ding" cal.
    @Inject(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD")
    )
    private void topchat$staffPing(Component message, MessageSignature signature, GuiMessageTag tag, CallbackInfo ci) {
        if (!topchat$configLoaded) {
            TopChat.loadConfig();
            topchat$configLoaded = true;
        }
        String text = message.getString();
        if (TopChat.logChat) {
            System.out.println("[TopChat] chat: " + text);
        }
        String lower = text.toLowerCase(java.util.Locale.ROOT);

        // 1) Yetkili sohbeti
        if (TopChat.staffSound) {
            for (String m : TopChat.staffMatch) {
                if (lower.contains(m)) {
                    topchat$ding();
                    return;
                }
            }
        }

        // 2) Bana gelen /msg ("nick -> sen mesaj"). Baskalarinin msg'lerinde (nickA -> nickB) calmaz.
        if (TopChat.msgSound && TopChat.msgPattern.matcher(text).find()) {
            topchat$ding();
        }
    }

    private static void topchat$ding() {
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, TopChat.staffPitch, TopChat.staffVolume));
    }
}
