package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class NoHitBox extends Module {
  private static NoHitBox INSTANCE = new NoHitBox();
  
  public Setting<Boolean> pickaxe = register(new Setting("Pickaxe", Boolean.valueOf(true)));
  
  public Setting<Boolean> crystal = register(new Setting("Crystal", Boolean.valueOf(true)));
  
  public Setting<Boolean> gapple = register(new Setting("Gapple", Boolean.valueOf(false)));
  
  public NoHitBox() {
    super("NoHitBox", "NoHitBox.", Module.Category.MISC, false, true, false);
    setInstance();
  }
  
  public static NoHitBox getINSTANCE() {
    if (INSTANCE == null)
      INSTANCE = new NoHitBox(); 
    return INSTANCE;
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
}
