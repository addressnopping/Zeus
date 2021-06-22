package me.alpha432.oyvey.features.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

public class MCF extends Module {
  private boolean clicked = false;
  
  public MCF() {
    super("MCF", "Middleclick Friends.", Module.Category.MISC, true, true, false);
  }
  
  public void onUpdate() {
    if (Mouse.isButtonDown(2)) {
      if (!this.clicked && mc.currentScreen == null)
        onClick(); 
      this.clicked = true;
    } else {
      this.clicked = false;
    } 
  }
  
  private void onClick() {
    RayTraceResult result = mc.objectMouseOver;
    Entity entity;
    if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && entity = result.entityHit instanceof net.minecraft.entity.player.EntityPlayer)
      if (OyVey.friendManager.isFriend(entity.getName())) {
        OyVey.friendManager.removeFriend(entity.getName());
        Command.sendMessage(ChatFormatting.RED + entity.getName() + ChatFormatting.RED + " has been unfriended.");
      } else {
        OyVey.friendManager.addFriend(entity.getName());
        Command.sendMessage(ChatFormatting.AQUA + entity.getName() + ChatFormatting.AQUA + " has been friended.");
      }  
    this.clicked = true;
  }
}
