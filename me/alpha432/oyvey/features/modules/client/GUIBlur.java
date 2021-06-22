package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class GUIBlur extends Module {
  final Minecraft mc;
  
  public GUIBlur() {
    super("GUIBlur", "nigga", Module.Category.CLIENT, true, true, false);
    this.mc = Minecraft.getMinecraft();
  }
  
  public void onDisable() {
    if (this.mc.world != null)
      Util.mc.entityRenderer.getShaderGroup().deleteShaderGroup(); 
  }
  
  public void onUpdate() {
    if (this.mc.world != null)
      if (ClickGui.getInstance().isEnabled() || this.mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainer || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiChat || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiConfirmOpenLink || this.mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiEditSign || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiGameOver || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiOptions || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiIngameMenu || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiVideoSettings || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiScreenOptionsSounds || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiControls || this.mc.currentScreen instanceof net.minecraft.client.gui.GuiCustomizeSkin || this.mc.currentScreen instanceof net.minecraftforge.fml.client.GuiModList) {
        if (OpenGlHelper.shadersSupported && Util.mc.getRenderViewEntity() instanceof net.minecraft.entity.player.EntityPlayer) {
          if (Util.mc.entityRenderer.getShaderGroup() != null)
            Util.mc.entityRenderer.getShaderGroup().deleteShaderGroup(); 
          try {
            Util.mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
          } catch (Exception e) {
            e.printStackTrace();
          } 
        } else if (Util.mc.entityRenderer.getShaderGroup() != null && Util.mc.currentScreen == null) {
          Util.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        } 
      } else if (Util.mc.entityRenderer.getShaderGroup() != null) {
        Util.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
      }  
  }
}
