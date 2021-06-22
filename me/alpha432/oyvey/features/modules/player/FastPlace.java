package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.InventoryUtil;
import net.minecraft.item.ItemExpBottle;

public class FastPlace extends Module {
  public FastPlace() {
    super("FastEXP", "Fast everything.", Module.Category.PLAYER, true, true, false);
  }
  
  public void onUpdate() {
    if (fullNullCheck())
      return; 
    if (InventoryUtil.holdingItem(ItemExpBottle.class))
      mc.rightClickDelayTimer = 0; 
  }
}
