package me.alpha432.oyvey.features.modules.chat;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoGG extends Module {
  private static AutoGG INSTANCE = new AutoGG();
  
  public Setting<String> custom = register(new Setting("Message:", "GG, Zeus owns me!"));
  
  private ConcurrentHashMap<String, Integer> targetedPlayers = null;
  
  public AutoGG() {
    super("AutoGG", "Sends msg after you kill someone", Module.Category.CHAT, true, false, false);
    setInstance();
  }
  
  public static AutoGG getINSTANCE() {
    if (INSTANCE == null)
      INSTANCE = new AutoGG(); 
    return INSTANCE;
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  public void onEnable() {
    this.targetedPlayers = new ConcurrentHashMap<>();
  }
  
  public void onDisable() {
    this.targetedPlayers = null;
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
  
  @SubscribeEvent
  public void onLeavingDeathEvent(LivingDeathEvent event) {
    if (mc.player == null)
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
  
  private boolean shouldAnnounce(String name) {
    return this.targetedPlayers.containsKey(name);
  }
  
  private void doAnnounce(String name) {
    this.targetedPlayers.remove(name);
    mc.player.connection.sendPacket((Packet)new CPacketChatMessage((String)this.custom.getValue()));
    int u = 0;
    for (int i = 0; i < 10; i++)
      u++; 
  }
  
  public void addTargetedPlayer(String name) {
    if (Objects.equals(name, mc.player.getName()))
      return; 
    if (this.targetedPlayers == null)
      this.targetedPlayers = new ConcurrentHashMap<>(); 
    this.targetedPlayers.put(name, Integer.valueOf(20));
  }
}
