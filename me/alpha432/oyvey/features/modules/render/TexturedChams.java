package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class TexturedChams extends Module {
  public static Setting<Integer> red;
  
  public static Setting<Integer> green;
  
  public static Setting<Integer> blue;
  
  public static Setting<Integer> alpha;
  
  public TexturedChams() {
    super("TexturedChams", "hi yes", Module.Category.RENDER, true, true, true);
    red = register(new Setting("Red", Integer.valueOf(168), Integer.valueOf(0), Integer.valueOf(255)));
    green = register(new Setting("Green", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255)));
    blue = register(new Setting("Blue", Integer.valueOf(232), Integer.valueOf(0), Integer.valueOf(255)));
    alpha = register(new Setting("Alpha", Integer.valueOf(150), Integer.valueOf(0), Integer.valueOf(255)));
  }
}
