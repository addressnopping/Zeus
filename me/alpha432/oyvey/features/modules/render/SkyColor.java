package me.alpha432.oyvey.features.modules.render;

import java.awt.Color;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SkyColor extends Module {
  private Setting<Integer> red = register(new Setting("Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private Setting<Integer> green = register(new Setting("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private Setting<Integer> blue = register(new Setting("Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private Setting<Boolean> rainbow = register(new Setting("Rainbow", Boolean.valueOf(true)));
  
  private static SkyColor INSTANCE = new SkyColor();
  
  public SkyColor() {
    super("SkyColor", "Changes the color of the fog", Module.Category.RENDER, false, true, false);
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  public static SkyColor getInstance() {
    if (INSTANCE == null)
      INSTANCE = new SkyColor(); 
    return INSTANCE;
  }
  
  @SubscribeEvent
  public void fogColors(EntityViewRenderEvent.FogColors event) {
    event.setRed(((Integer)this.red.getValue()).intValue() / 255.0F);
    event.setGreen(((Integer)this.green.getValue()).intValue() / 255.0F);
    event.setBlue(((Integer)this.blue.getValue()).intValue() / 255.0F);
  }
  
  @SubscribeEvent
  public void fog_density(EntityViewRenderEvent.FogDensity event) {
    event.setDensity(0.0F);
    event.setCanceled(true);
  }
  
  public void onEnable() {
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  public void onDisable() {
    MinecraftForge.EVENT_BUS.unregister(this);
  }
  
  public void onUpdate() {
    if (((Boolean)this.rainbow.getValue()).booleanValue())
      doRainbow(); 
  }
  
  public void doRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.red.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.green.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.blue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
}
