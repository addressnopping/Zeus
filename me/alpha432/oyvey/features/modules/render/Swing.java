package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.util.EnumHand;

public class Swing extends Module {
  private Setting<Hand> hand = register(new Setting("Hand", Hand.OFFHAND));
  
  public Swing() {
    super("Swing", "Changes the hand you swing with", Module.Category.RENDER, false, true, false);
  }
  
  public void onUpdate() {
    if (mc.world == null)
      return; 
    if (((Hand)this.hand.getValue()).equals(Hand.OFFHAND))
      mc.player.swingingHand = EnumHand.OFF_HAND; 
    if (((Hand)this.hand.getValue()).equals(Hand.MAINHAND))
      mc.player.swingingHand = EnumHand.MAIN_HAND; 
    if (((Hand)this.hand.getValue()).equals(Hand.PACKETSWING) && 
      mc.player.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemSword && mc.entityRenderer.itemRenderer.prevEquippedProgressMainHand >= 0.9D) {
      mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1.0F;
      mc.entityRenderer.itemRenderer.itemStackMainHand = mc.player.getHeldItemMainhand();
    } 
  }
  
  public enum Hand {
    OFFHAND, MAINHAND, PACKETSWING;
  }
}
