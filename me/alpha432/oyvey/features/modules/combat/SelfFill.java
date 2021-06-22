package me.alpha432.oyvey.features.modules.combat;

import java.lang.reflect.Field;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.movement.ReverseStep;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.manager.Mapping;
import me.alpha432.oyvey.util.InventoryUtil;
import me.alpha432.oyvey.util.WorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;

public class SelfFill extends Module {
  private BlockPos playerPos;
  
  private final Setting<Boolean> timerfill = register(new Setting("TimerFill", Boolean.valueOf(false)));
  
  private final Setting<Boolean> toggleRStep = register(new Setting("ToggleRStep", Boolean.valueOf(true)));
  
  public SelfFill() {
    super("SelfFill", "SelfFills yourself in a hole.", Module.Category.COMBAT, true, true, true);
  }
  
  public void onEnable() {
    if (((Boolean)this.timerfill.getValue()).booleanValue())
      setTimer(50.0F); 
    if (((Boolean)this.toggleRStep.getValue()).booleanValue()) {
      OyVey.moduleManager.getModuleByName("ReverseStep").isEnabled();
      ReverseStep.getInstance().disable();
    } 
    this.playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
    if (mc.world.getBlockState(this.playerPos).getBlock().equals(Blocks.OBSIDIAN)) {
      disable();
      return;
    } 
    mc.player.jump();
  }
  
  public void onDisable() {
    if (((Boolean)this.toggleRStep.getValue()).booleanValue()) {
      OyVey.moduleManager.getModuleByName("ReverseStep").isDisabled();
      ReverseStep.getInstance().enable();
    } 
    setTimer(1.0F);
  }
  
  public void onUpdate() {
    if (nullCheck())
      return; 
    if (mc.player.posY > this.playerPos.getY() + 1.04D) {
      WorldUtil.placeBlock(this.playerPos, InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN));
      mc.player.jump();
      disable();
    } 
  }
  
  private void setTimer(float value) {
    try {
      Field timer = Minecraft.class.getDeclaredField(Mapping.timer);
      timer.setAccessible(true);
      Field tickLength = Timer.class.getDeclaredField(Mapping.tickLength);
      tickLength.setAccessible(true);
      tickLength.setFloat(timer.get(mc), 50.0F / value);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public String getDisplayInfo() {
    if (((Boolean)this.timerfill.getValue()).booleanValue())
      return "Timer"; 
    return "Burrow";
  }
}
