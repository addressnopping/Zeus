package me.alpha432.oyvey.mixin.mixins;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.client.FontMod;
import me.alpha432.oyvey.features.modules.client.NickHider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FontRenderer.class})
public abstract class MixinFontRenderer {
  @Shadow
  protected abstract int renderString(String paramString, float paramFloat1, float paramFloat2, int paramInt, boolean paramBoolean);
  
  @Shadow
  protected abstract void renderStringAtPos(String paramString, boolean paramBoolean);
  
  @Redirect(method = {"renderString(Ljava/lang/String;FFIZ)I"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;renderStringAtPos(Ljava/lang/String;Z)V"))
  public void renderStringAtPosHook(FontRenderer renderer, String text, boolean shadow) {
    if (NickHider.getInstance().isOn()) {
      renderStringAtPos(text.replace(Minecraft.getMinecraft().getSession().getUsername(), (NickHider.getInstance()).NameString.getValueAsString()), shadow);
    } else {
      renderStringAtPos(text, shadow);
    } 
  }
  
  @Inject(method = {"drawString(Ljava/lang/String;FFIZ)I"}, at = {@At("HEAD")}, cancellable = true)
  public void renderStringHook(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> info) {
    if (FontMod.getInstance().isOn() && ((Boolean)((FontMod)OyVey.moduleManager.getModuleT(FontMod.class)).customAll.getValue()).booleanValue() && OyVey.textManager != null) {
      float result = OyVey.textManager.drawString(text, x, y, color, dropShadow);
      info.setReturnValue(Integer.valueOf((int)result));
    } 
  }
}
