package me.alpha432.oyvey.features.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.HashMap;
import java.util.UUID;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PearlNotify extends Module {
  private final HashMap<EntityPlayer, UUID> list;
  
  private Entity enderPearl;
  
  private boolean flag;
  
  public PearlNotify() {
    super("PearlResolver", "Notify pearl throws.", Module.Category.MISC, true, true, false);
    this.list = new HashMap<>();
  }
  
  public void onEnable() {
    this.flag = true;
  }
  
  public void onUpdate() {
    if (mc.world == null || mc.player == null)
      return; 
    this.enderPearl = null;
    for (Entity e : mc.world.loadedEntityList) {
      if (e instanceof net.minecraft.entity.item.EntityEnderPearl) {
        this.enderPearl = e;
        break;
      } 
    } 
    if (this.enderPearl == null) {
      this.flag = true;
      return;
    } 
    EntityPlayer closestPlayer = null;
    for (EntityPlayer entity : mc.world.playerEntities) {
      if (closestPlayer == null) {
        closestPlayer = entity;
        continue;
      } 
      if (closestPlayer.getDistance(this.enderPearl) <= entity.getDistance(this.enderPearl))
        continue; 
      closestPlayer = entity;
    } 
    if (closestPlayer == mc.player)
      this.flag = false; 
    if (closestPlayer != null && this.flag) {
      String faceing = this.enderPearl.getHorizontalFacing().toString();
      if (faceing.equals("west")) {
        faceing = "east";
      } else if (faceing.equals("east")) {
        faceing = "west";
      } 
      Command.sendSilentMessage(OyVey.friendManager.isFriend(closestPlayer.getName()) ? (ChatFormatting.AQUA + closestPlayer.getName() + ChatFormatting.DARK_GRAY + " has just thrown a pearl heading " + faceing + "!") : (ChatFormatting.RED + closestPlayer.getName() + ChatFormatting.DARK_GRAY + " has just thrown a pearl heading " + faceing + "!"));
      this.flag = false;
    } 
  }
}
