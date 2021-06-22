package me.alpha432.oyvey.mixin.mixins;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.render.TexturedChams;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({RenderPlayer.class})
public class MixinRenderPlayer {
  @Inject(method = {"renderEntityName"}, at = {@At("HEAD")}, cancellable = true)
  public void renderEntityNameHook(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
    if (OyVey.moduleManager.isModuleEnabled("NameTags"))
      info.cancel(); 
  }
  
  @Overwrite
  public ResourceLocation getEntityTexture(AbstractClientPlayer entity) {
    if (OyVey.moduleManager.isModuleEnabled("TexturedChams")) {
      GL11.glColor4f(((Integer)TexturedChams.red.getValue()).intValue() / 255.0F, ((Integer)TexturedChams.green.getValue()).intValue() / 255.0F, ((Integer)TexturedChams.blue.getValue()).intValue() / 255.0F, ((Integer)TexturedChams.alpha.getValue()).intValue() / 255.0F);
      return new ResourceLocation("minecraft:steve_skin1.png");
    } 
    return entity.getLocationSkin();
  }
}
