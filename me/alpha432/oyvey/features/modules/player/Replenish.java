package me.alpha432.oyvey.features.modules.player;

import java.util.ArrayList;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Replenish extends Module {
  private final Setting<Integer> delay = register(new Setting("Delay", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(10)));
  
  private final Setting<Integer> gapStack = register(new Setting("GapStack", Integer.valueOf(1), Integer.valueOf(50), Integer.valueOf(64)));
  
  private final Setting<Integer> xpStackAt = register(new Setting("XPStack", Integer.valueOf(1), Integer.valueOf(50), Integer.valueOf(64)));
  
  private final Timer timer = new Timer();
  
  private final ArrayList<Item> Hotbar = new ArrayList<>();
  
  public Replenish() {
    super("Replenish", "Replenishes your hotbar", Module.Category.PLAYER, false, true, false);
  }
  
  public void onEnable() {
    if (fullNullCheck())
      return; 
    this.Hotbar.clear();
    for (int l_I = 0; l_I < 9; l_I++) {
      ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
      if (!l_Stack.isEmpty() && !this.Hotbar.contains(l_Stack.getItem())) {
        this.Hotbar.add(l_Stack.getItem());
      } else {
        this.Hotbar.add(Items.AIR);
      } 
    } 
  }
  
  public void onUpdate() {
    if (mc.currentScreen != null)
      return; 
    if (!this.timer.passedMs((((Integer)this.delay.getValue()).intValue() * 1000)))
      return; 
    for (int l_I = 0; l_I < 9; ) {
      if (!RefillSlotIfNeed(l_I)) {
        l_I++;
        continue;
      } 
      this.timer.reset();
      return;
    } 
  }
  
  private boolean RefillSlotIfNeed(int p_Slot) {
    ItemStack l_Stack = mc.player.inventory.getStackInSlot(p_Slot);
    if (l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR)
      return false; 
    if (!l_Stack.isStackable())
      return false; 
    if (l_Stack.getCount() >= l_Stack.getMaxStackSize())
      return false; 
    if (l_Stack.getItem().equals(Items.GOLDEN_APPLE) && l_Stack.getCount() >= ((Integer)this.gapStack.getValue()).intValue())
      return false; 
    if (l_Stack.getItem().equals(Items.EXPERIENCE_BOTTLE) && l_Stack.getCount() > ((Integer)this.xpStackAt.getValue()).intValue())
      return false; 
    for (int l_I = 9; l_I < 36; ) {
      ItemStack l_Item = mc.player.inventory.getStackInSlot(l_I);
      if (l_Item.isEmpty() || !CanItemBeMergedWith(l_Stack, l_Item)) {
        l_I++;
        continue;
      } 
      mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_I, 0, ClickType.QUICK_MOVE, (EntityPlayer)mc.player);
      mc.playerController.updateController();
      return true;
    } 
    return false;
  }
  
  private boolean CanItemBeMergedWith(ItemStack p_Source, ItemStack p_Target) {
    return (p_Source.getItem() == p_Target.getItem() && p_Source.getDisplayName().equals(p_Target.getDisplayName()));
  }
}
