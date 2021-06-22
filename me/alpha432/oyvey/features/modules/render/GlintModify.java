package me.alpha432.oyvey.features.modules.render;

import java.awt.Color;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class GlintModify extends Module {
  public Setting<Integer> red = register(new Setting("Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  public Setting<Integer> green = register(new Setting("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  public Setting<Integer> blue = register(new Setting("Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  public Setting<Boolean> rainbow = register(new Setting("Rainbow", Boolean.valueOf(false)));
  
  public GlintModify() {
    super("GlintModify", "Changes the enchant glint color", Module.Category.RENDER, true, true, true);
  }
  
  public static Color getColor(long offset, float fade) {
    if (!((Boolean)((GlintModify)OyVey.moduleManager.getModuleT(GlintModify.class)).rainbow.getValue()).booleanValue())
      return new Color(((Integer)((GlintModify)OyVey.moduleManager.getModuleT(GlintModify.class)).red.getValue()).intValue(), ((Integer)((GlintModify)OyVey.moduleManager.getModuleT(GlintModify.class)).green.getValue()).intValue(), ((Integer)((GlintModify)OyVey.moduleManager.getModuleT(GlintModify.class)).blue.getValue()).intValue()); 
    float hue = (float)(System.nanoTime() + offset) / 1.0E10F % 1.0F;
    long color = Long.parseLong(Integer.toHexString(Integer.valueOf(Color.HSBtoRGB(hue, 1.0F, 1.0F)).intValue()), 16);
    Color c = new Color((int)color);
    return new Color(c.getRed() / 255.0F * fade, c.getGreen() / 255.0F * fade, c.getBlue() / 255.0F * fade, c.getAlpha() / 255.0F);
  }
  
  public void onUpdate() {
    if (((Boolean)this.rainbow.getValue()).booleanValue())
      cycleRainbow(); 
  }
  
  public void cycleRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.red.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.green.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.blue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
}
