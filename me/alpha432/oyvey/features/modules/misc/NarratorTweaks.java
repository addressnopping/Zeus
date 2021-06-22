package me.alpha432.oyvey.features.modules.misc;

import com.mojang.text2speech.Narrator;
import java.util.concurrent.ConcurrentHashMap;
import me.alpha432.oyvey.event.events.DeathEvent;
import me.alpha432.oyvey.event.events.TotemPopEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.chat.AutoGG;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NarratorTweaks extends Module {
  public Setting<Boolean> pop = register(new Setting("OwnTotemPop", Boolean.valueOf(true)));
  
  public Setting<Boolean> death = register(new Setting("Death", Boolean.valueOf(true)));
  
  public Setting<Boolean> totemPop = register(new Setting("EnemyTotemPop", Boolean.valueOf(true)));
  
  public Setting<Boolean> killsay = register(new Setting("Killsay", Boolean.valueOf(true)));
  
  public Setting<String> totemPopMessage = register(new Setting("PopMessage", "<player> bro stop popping ", v -> ((Boolean)this.pop.getValue()).booleanValue()));
  
  public Setting<String> deathMessages = register(new Setting("DeathMessage", "<player> ayt bro its calm just come back innit", v -> ((Boolean)this.death.getValue()).booleanValue()));
  
  public Setting<String> popEnemyMessage = register(new Setting("PopEnemyMessage", "<player> YOU'RE POPPING KID", v -> ((Boolean)this.totemPop.getValue()).booleanValue()));
  
  public Setting<String> killsayMsg = register(new Setting("KillsayMessage", "1 sit no name dog!", v -> ((Boolean)this.killsay.getValue()).booleanValue()));
  
  private final Narrator narrator = Narrator.getNarrator();
  
  private ConcurrentHashMap<String, Integer> targetedPlayers = null;
  
  public NarratorTweaks() {
    super("NarratorTweaks", "Sends messages through narrator", Module.Category.MISC, true, true, false);
  }
  
  @SubscribeEvent
  public void onTotemPop(TotemPopEvent event) {
    if (event.getEntity() == mc.player)
      this.narrator.say(((String)this.totemPopMessage.getValue()).replaceAll("<player>", mc.player.getName())); 
  }
  
  public void onTotemPop(EntityPlayer player) {
    this.narrator.say(((String)this.popEnemyMessage.getValue()).replaceAll("<player>", String.valueOf(PopCounter.TotemPopContainer.get(player.getName()))));
  }
  
  @SubscribeEvent
  public void onDeath(DeathEvent event) {
    if (event.player == mc.player)
      this.narrator.say(((String)this.deathMessages.getValue()).replaceAll("<player>", mc.player.getName())); 
  }
  
  public void onUpdate() {
    if (nullCheck())
      return; 
    if (this.targetedPlayers == null)
      this.targetedPlayers = new ConcurrentHashMap<>(); 
    for (Entity entity : mc.world.getLoadedEntityList()) {
      String name2;
      EntityPlayer player;
      if (!(entity instanceof EntityPlayer) || (player = (EntityPlayer)entity).getHealth() > 0.0F || !shouldAnnounce(name2 = player.getName()))
        continue; 
      doAnnounce(name2);
    } 
    this.targetedPlayers.forEach((name, timeout) -> {
          if (timeout.intValue() <= 0) {
            this.targetedPlayers.remove(name);
          } else {
            this.targetedPlayers.put(name, Integer.valueOf(timeout.intValue() - 1));
          } 
        });
  }
  
  private boolean shouldAnnounce(String name) {
    return this.targetedPlayers.containsKey(name);
  }
  
  private void doAnnounce(String name) {
    this.targetedPlayers.remove(name);
    this.narrator.say((String)this.killsayMsg.getValue());
    int u = 0;
    for (int i = 0; i < 10; i++)
      u++; 
  }
  
  @SubscribeEvent
  public void onLeavingDeathEvent(LivingDeathEvent event) {
    if (AutoGG.mc.player == null)
      return; 
    if (this.targetedPlayers == null)
      this.targetedPlayers = new ConcurrentHashMap<>(); 
    EntityLivingBase entity;
    if ((entity = event.getEntityLiving()) == null)
      return; 
    if (!(entity instanceof EntityPlayer))
      return; 
    EntityPlayer player = (EntityPlayer)entity;
    if (player.getHealth() > 0.0F)
      return; 
    String name = player.getName();
    if (shouldAnnounce(name))
      doAnnounce(name); 
  }
}
