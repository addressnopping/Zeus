package me.alpha432.oyvey.features.modules.player;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;
import me.alpha432.oyvey.event.events.BlockDestructionEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Announcer extends Module {
  public static int blockBrokeDelay = 0;
  
  static int blockPlacedDelay = 0;
  
  static int jumpDelay = 0;
  
  static int attackDelay = 0;
  
  static int eattingDelay = 0;
  
  static long lastPositionUpdate;
  
  static double lastPositionX;
  
  static double lastPositionY;
  
  static double lastPositionZ;
  
  private static double speed;
  
  String heldItem = "";
  
  int blocksPlaced = 0;
  
  int blocksBroken = 0;
  
  int eaten = 0;
  
  public static String walkMessage = "I just moved {blocks} blocks thanks to zori";
  
  public static String breakMessage = "I just broke {amount} {name} thanks to zori";
  
  public static String eatMessage = "I just ate {amount} {name} thanks to zori";
  
  private final Setting<Boolean> move = register(new Setting("Move", Boolean.valueOf(false)));
  
  private final Setting<Boolean> breakBlock = register(new Setting("Break", Boolean.valueOf(false)));
  
  private final Setting<Boolean> eat = register(new Setting("Eat", Boolean.valueOf(false)));
  
  private final Setting<Double> delay = register(new Setting("Delay", Double.valueOf(1.0D), Double.valueOf(1.0D), Double.valueOf(20.0D)));
  
  public Announcer() {
    super("Announcer", "flood chat wit da cheat on baby", Module.Category.PLAYER, false, true, false);
  }
  
  public void onUpdate() {
    blockBrokeDelay++;
    blockPlacedDelay++;
    jumpDelay++;
    attackDelay++;
    eattingDelay++;
    this.heldItem = mc.player.getHeldItemMainhand().getDisplayName();
    if (((Boolean)this.move.getValue()).booleanValue() && 
      lastPositionUpdate + 5000.0D * ((Double)this.delay.getValue()).doubleValue() < System.currentTimeMillis()) {
      double d0 = lastPositionX - mc.player.lastTickPosX;
      double d2 = lastPositionY - mc.player.lastTickPosY;
      double d3 = lastPositionZ - mc.player.lastTickPosZ;
      speed = Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
      if (speed > 1.0D && speed <= 5000.0D) {
        String walkAmount = (new DecimalFormat("0")).format(speed);
        mc.player.sendChatMessage(walkMessage.replace("{blocks}", walkAmount));
      } 
      lastPositionUpdate = System.currentTimeMillis();
      lastPositionX = mc.player.lastTickPosX;
      lastPositionY = mc.player.lastTickPosY;
      lastPositionZ = mc.player.lastTickPosZ;
    } 
  }
  
  @SubscribeEvent
  public void onItemUse(LivingEntityUseItemEvent event) {
    int randomNum = ThreadLocalRandom.current().nextInt(1, 11);
    if (event.getEntity() == mc.player && (
      event.getItem().getItem() instanceof net.minecraft.item.ItemFood || event.getItem().getItem() instanceof net.minecraft.item.ItemAppleGold)) {
      this.eaten++;
      if (eattingDelay >= 300.0D * ((Double)this.delay.getValue()).doubleValue() && (
        (Boolean)this.eat.getValue()).booleanValue() && this.eaten > randomNum) {
        mc.player
          .sendChatMessage(eatMessage.replace("{amount}", this.eaten + "").replace("{name}", mc.player.getHeldItemMainhand().getDisplayName()));
        this.eaten = 0;
        eattingDelay = 0;
      } 
    } 
  }
  
  @SubscribeEvent
  public void onBlockBreak(BlockDestructionEvent event) {
    this.blocksBroken++;
    int randomNum = ThreadLocalRandom.current().nextInt(1, 11);
    if (blockBrokeDelay >= 300.0D * ((Double)this.delay.getValue()).doubleValue()) {
      if (((Boolean)this.breakBlock.getValue()).booleanValue() && this.blocksBroken > randomNum) {
        String msg = breakMessage.replace("{amount}", this.blocksBroken + "").replace("{name}", mc.world.getBlockState(event.getBlockPos()).getBlock().getLocalizedName());
        mc.player.sendChatMessage(msg);
      } 
      this.blocksBroken = 0;
      blockBrokeDelay = 0;
    } 
  }
}
