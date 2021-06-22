package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;

public class Speed extends Module {
  public Speed() {
    super("Speed", "placeholder", Module.Category.MOVEMENT, false, true, false);
  }
  
  public String getDisplayInfo() {
    return "Strafe";
  }
}
