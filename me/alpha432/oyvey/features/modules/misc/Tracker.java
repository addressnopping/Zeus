package me.alpha432.oyvey.features.modules.misc;

import java.util.Objects;
import me.alpha432.oyvey.event.events.DeathEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.HUD;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Tracker extends Module {
  private static Tracker instance;
  
  private EntityPlayer trackedPlayer;
  
  private int usedExp = 0;
  
  private int usedStacks = 0;
  
  public Tracker() {
    super("Tracker", "Tracks players in 1v1s.", Module.Category.MISC, true, true, false);
    instance = this;
  }
  
  public static Tracker getInstance() {
    if (instance == null)
      instance = new Tracker(); 
    return instance;
  }
  
  public void onUpdate() {
    if (this.trackedPlayer == null) {
      this.trackedPlayer = EntityUtil.getClosestEnemy(1000.0D);
    } else if (this.usedStacks != this.usedExp / 64) {
      this.usedStacks = this.usedExp / 64;
      Command.sendMessage(TextUtil.coloredString(this.trackedPlayer.getName() + " has used " + this.usedStacks + " stacks of XP!", (TextUtil.Color)(HUD.getInstance()).commandColor.getValue()));
    } 
  }
  
  public void onSpawnEntity(Entity entity) {
    if (entity instanceof net.minecraft.entity.item.EntityExpBottle && Objects.equals(mc.world.getClosestPlayerToEntity(entity, 3.0D), this.trackedPlayer))
      this.usedExp++; 
  }
  
  public void onDisable() {
    this.trackedPlayer = null;
    this.usedExp = 0;
    this.usedStacks = 0;
  }
  
  @SubscribeEvent
  public void onDeath(DeathEvent event) {
    if (event.player.equals(this.trackedPlayer)) {
      this.usedExp = 0;
      this.usedStacks = 0;
    } 
  }
  
  public String getDisplayInfo() {
    if (this.trackedPlayer != null)
      return this.trackedPlayer.getName(); 
    return null;
  }
}
