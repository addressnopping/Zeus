package me.alpha432.oyvey.features.modules.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.player.XCarry;
import me.alpha432.oyvey.features.setting.Bind;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.DamageUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.InventoryUtil;
import me.alpha432.oyvey.util.MathUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class AutoArmor extends Module {
  private final Setting<Integer> delay = register(new Setting("Delay", Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(500)));
  
  private final Setting<Boolean> mendingTakeOff = register(new Setting("AutoMend", Boolean.valueOf(false)));
  
  private final Setting<Integer> closestEnemy = register(new Setting("Enemy", Integer.valueOf(8), Integer.valueOf(1), Integer.valueOf(20), v -> ((Boolean)this.mendingTakeOff.getValue()).booleanValue()));
  
  private final Setting<Integer> helmetThreshold = register(new Setting("Helmet%", Integer.valueOf(80), Integer.valueOf(1), Integer.valueOf(100), v -> ((Boolean)this.mendingTakeOff.getValue()).booleanValue()));
  
  private final Setting<Integer> chestThreshold = register(new Setting("Chest%", Integer.valueOf(80), Integer.valueOf(1), Integer.valueOf(100), v -> ((Boolean)this.mendingTakeOff.getValue()).booleanValue()));
  
  private final Setting<Integer> legThreshold = register(new Setting("Legs%", Integer.valueOf(80), Integer.valueOf(1), Integer.valueOf(100), v -> ((Boolean)this.mendingTakeOff.getValue()).booleanValue()));
  
  private final Setting<Integer> bootsThreshold = register(new Setting("Boots%", Integer.valueOf(80), Integer.valueOf(1), Integer.valueOf(100), v -> ((Boolean)this.mendingTakeOff.getValue()).booleanValue()));
  
  private final Setting<Boolean> curse = register(new Setting("CurseOfBinding", Boolean.valueOf(false)));
  
  private final Setting<Integer> actions = register(new Setting("Actions", Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(12)));
  
  private final Setting<Bind> elytraBind = register(new Setting("Elytra", new Bind(-1)));
  
  private final Setting<Boolean> tps = register(new Setting("TpsSync", Boolean.valueOf(true)));
  
  private final Setting<Boolean> updateController = register(new Setting("Update", Boolean.valueOf(true)));
  
  private final Setting<Boolean> shiftClick = register(new Setting("ShiftClick", Boolean.valueOf(false)));
  
  private final Timer timer = new Timer();
  
  private final Timer elytraTimer = new Timer();
  
  private final Queue<InventoryUtil.Task> taskList = new ConcurrentLinkedQueue<>();
  
  private final List<Integer> doneSlots = new ArrayList<>();
  
  private boolean elytraOn = false;
  
  public AutoArmor() {
    super("AutoArmor", "Puts Armor on for you.", Module.Category.COMBAT, true, true, false);
  }
  
  @SubscribeEvent
  public void onKeyInput(InputEvent.KeyInputEvent event) {
    if (Keyboard.getEventKeyState() && !(mc.currentScreen instanceof me.alpha432.oyvey.features.gui.OyVeyGui) && ((Bind)this.elytraBind.getValue()).getKey() == Keyboard.getEventKey())
      this.elytraOn = !this.elytraOn; 
  }
  
  public void onLogin() {
    this.timer.reset();
    this.elytraTimer.reset();
  }
  
  public void onDisable() {
    this.taskList.clear();
    this.doneSlots.clear();
    this.elytraOn = false;
  }
  
  public void onLogout() {
    this.taskList.clear();
    this.doneSlots.clear();
  }
  
  public void onTick() {
    if (fullNullCheck() || (mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainer && !(mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiInventory)))
      return; 
    if (this.taskList.isEmpty()) {
      if (((Boolean)this.mendingTakeOff.getValue()).booleanValue() && InventoryUtil.holdingItem(ItemExpBottle.class) && mc.gameSettings.keyBindUseItem.isKeyDown() && (isSafe() || EntityUtil.isSafe((Entity)mc.player, 1, false, true))) {
        ItemStack itemStack1 = mc.player.inventoryContainer.getSlot(5).getStack();
        int helmDamage;
        if (!itemStack1.isEmpty && (helmDamage = DamageUtil.getRoundedDamage(itemStack1)) >= ((Integer)this.helmetThreshold.getValue()).intValue())
          takeOffSlot(5); 
        ItemStack chest2 = mc.player.inventoryContainer.getSlot(6).getStack();
        int chestDamage;
        if (!chest2.isEmpty && (chestDamage = DamageUtil.getRoundedDamage(chest2)) >= ((Integer)this.chestThreshold.getValue()).intValue())
          takeOffSlot(6); 
        ItemStack legging2 = mc.player.inventoryContainer.getSlot(7).getStack();
        int leggingDamage;
        if (!legging2.isEmpty && (leggingDamage = DamageUtil.getRoundedDamage(legging2)) >= ((Integer)this.legThreshold.getValue()).intValue())
          takeOffSlot(7); 
        ItemStack feet2 = mc.player.inventoryContainer.getSlot(8).getStack();
        int bootDamage;
        if (!feet2.isEmpty && (bootDamage = DamageUtil.getRoundedDamage(feet2)) >= ((Integer)this.bootsThreshold.getValue()).intValue())
          takeOffSlot(8); 
        return;
      } 
      ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();
      int slot4;
      if (helm.getItem() == Items.AIR && (slot4 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.HEAD, ((Boolean)this.curse.getValue()).booleanValue(), XCarry.getInstance().isOn())) != -1)
        getSlotOn(5, slot4); 
      ItemStack chest;
      if ((chest = mc.player.inventoryContainer.getSlot(6).getStack()).getItem() == Items.AIR) {
        if (this.taskList.isEmpty())
          if (this.elytraOn && this.elytraTimer.passedMs(500L)) {
            int elytraSlot = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.getInstance().isOn());
            if (elytraSlot != -1) {
              if ((elytraSlot < 5 && elytraSlot > 1) || !((Boolean)this.shiftClick.getValue()).booleanValue()) {
                this.taskList.add(new InventoryUtil.Task(elytraSlot));
                this.taskList.add(new InventoryUtil.Task(6));
              } else {
                this.taskList.add(new InventoryUtil.Task(elytraSlot, true));
              } 
              if (((Boolean)this.updateController.getValue()).booleanValue())
                this.taskList.add(new InventoryUtil.Task()); 
              this.elytraTimer.reset();
            } 
          } else {
            int slot3;
            if (!this.elytraOn && (slot3 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.CHEST, ((Boolean)this.curse.getValue()).booleanValue(), XCarry.getInstance().isOn())) != -1)
              getSlotOn(6, slot3); 
          }  
      } else if (this.elytraOn && chest.getItem() != Items.ELYTRA && this.elytraTimer.passedMs(500L)) {
        if (this.taskList.isEmpty()) {
          int slot3 = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.getInstance().isOn());
          if (slot3 != -1) {
            this.taskList.add(new InventoryUtil.Task(slot3));
            this.taskList.add(new InventoryUtil.Task(6));
            this.taskList.add(new InventoryUtil.Task(slot3));
            if (((Boolean)this.updateController.getValue()).booleanValue())
              this.taskList.add(new InventoryUtil.Task()); 
          } 
          this.elytraTimer.reset();
        } 
      } else if (!this.elytraOn && chest.getItem() == Items.ELYTRA && this.elytraTimer.passedMs(500L) && this.taskList.isEmpty()) {
        int slot3 = InventoryUtil.findItemInventorySlot((Item)Items.DIAMOND_CHESTPLATE, false, XCarry.getInstance().isOn());
        if (slot3 == -1 && (slot3 = InventoryUtil.findItemInventorySlot((Item)Items.IRON_CHESTPLATE, false, XCarry.getInstance().isOn())) == -1 && (slot3 = InventoryUtil.findItemInventorySlot((Item)Items.GOLDEN_CHESTPLATE, false, XCarry.getInstance().isOn())) == -1 && (slot3 = InventoryUtil.findItemInventorySlot((Item)Items.CHAINMAIL_CHESTPLATE, false, XCarry.getInstance().isOn())) == -1)
          slot3 = InventoryUtil.findItemInventorySlot((Item)Items.LEATHER_CHESTPLATE, false, XCarry.getInstance().isOn()); 
        if (slot3 != -1) {
          this.taskList.add(new InventoryUtil.Task(slot3));
          this.taskList.add(new InventoryUtil.Task(6));
          this.taskList.add(new InventoryUtil.Task(slot3));
          if (((Boolean)this.updateController.getValue()).booleanValue())
            this.taskList.add(new InventoryUtil.Task()); 
        } 
        this.elytraTimer.reset();
      } 
      int slot2;
      ItemStack legging;
      if ((legging = mc.player.inventoryContainer.getSlot(7).getStack()).getItem() == Items.AIR && (slot2 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.LEGS, ((Boolean)this.curse.getValue()).booleanValue(), XCarry.getInstance().isOn())) != -1)
        getSlotOn(7, slot2); 
      int slot;
      ItemStack feet;
      if ((feet = mc.player.inventoryContainer.getSlot(8).getStack()).getItem() == Items.AIR && (slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.FEET, ((Boolean)this.curse.getValue()).booleanValue(), XCarry.getInstance().isOn())) != -1)
        getSlotOn(8, slot); 
    } 
    if (this.timer.passedMs((int)(((Integer)this.delay.getValue()).intValue() * (((Boolean)this.tps.getValue()).booleanValue() ? OyVey.serverManager.getTpsFactor() : 1.0F)))) {
      if (!this.taskList.isEmpty())
        for (int i = 0; i < ((Integer)this.actions.getValue()).intValue(); i++) {
          InventoryUtil.Task task = this.taskList.poll();
          if (task != null)
            task.run(); 
        }  
      this.timer.reset();
    } 
  }
  
  public String getDisplayInfo() {
    if (this.elytraOn)
      return "Elytra"; 
    return null;
  }
  
  private void takeOffSlot(int slot) {
    if (this.taskList.isEmpty()) {
      int target = -1;
      for (Iterator<Integer> iterator = InventoryUtil.findEmptySlots(XCarry.getInstance().isOn()).iterator(); iterator.hasNext(); ) {
        int i = ((Integer)iterator.next()).intValue();
        if (this.doneSlots.contains(Integer.valueOf(target)))
          continue; 
        target = i;
        this.doneSlots.add(Integer.valueOf(i));
      } 
      if (target != -1) {
        if ((target < 5 && target > 0) || !((Boolean)this.shiftClick.getValue()).booleanValue()) {
          this.taskList.add(new InventoryUtil.Task(slot));
          this.taskList.add(new InventoryUtil.Task(target));
        } else {
          this.taskList.add(new InventoryUtil.Task(slot, true));
        } 
        if (((Boolean)this.updateController.getValue()).booleanValue())
          this.taskList.add(new InventoryUtil.Task()); 
      } 
    } 
  }
  
  private void getSlotOn(int slot, int target) {
    if (this.taskList.isEmpty()) {
      this.doneSlots.remove(Integer.valueOf(target));
      if ((target < 5 && target > 0) || !((Boolean)this.shiftClick.getValue()).booleanValue()) {
        this.taskList.add(new InventoryUtil.Task(target));
        this.taskList.add(new InventoryUtil.Task(slot));
      } else {
        this.taskList.add(new InventoryUtil.Task(target, true));
      } 
      if (((Boolean)this.updateController.getValue()).booleanValue())
        this.taskList.add(new InventoryUtil.Task()); 
    } 
  }
  
  private boolean isSafe() {
    EntityPlayer closest = EntityUtil.getClosestEnemy(((Integer)this.closestEnemy.getValue()).intValue());
    if (closest == null)
      return true; 
    return (mc.player.getDistanceSq((Entity)closest) >= MathUtil.square(((Integer)this.closestEnemy.getValue()).intValue()));
  }
}
