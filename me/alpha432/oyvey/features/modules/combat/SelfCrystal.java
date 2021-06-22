package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.entity.player.EntityPlayer;

public class SelfCrystal extends Module {
  public SelfCrystal() {
    super("SelfCrystal", "Best module", Module.Category.COMBAT, true, true, false);
  }
  
  public void onTick() {
    if (PhobosCrystal.getInstance().isEnabled())
      PhobosCrystal.target = (EntityPlayer)mc.player; 
  }
}
