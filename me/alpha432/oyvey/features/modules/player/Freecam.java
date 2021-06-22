package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.event.events.MoveEvent;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Freecam extends Module {
  private static Freecam INSTANCE = new Freecam();
  
  public Setting<Double> speed = register(new Setting("Speed", Double.valueOf(0.5D), Double.valueOf(0.1D), Double.valueOf(5.0D)));
  
  public Setting<Boolean> packet = register(new Setting("Cancel Packets", Boolean.valueOf(true)));
  
  private double posX;
  
  private double posY;
  
  private double posZ;
  
  private float pitch;
  
  private float yaw;
  
  private EntityOtherPlayerMP clonedPlayer;
  
  private boolean isRidingEntity;
  
  private Entity ridingEntity;
  
  public Freecam() {
    super("Freecam", "Look around freely.", Module.Category.PLAYER, true, false, false);
    setInstance();
  }
  
  public static Freecam getInstance() {
    if (INSTANCE == null)
      INSTANCE = new Freecam(); 
    return INSTANCE;
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  public void onEnable() {
    if (mc.player != null) {
      this.isRidingEntity = (mc.player.getRidingEntity() != null);
      if (mc.player.getRidingEntity() == null) {
        this.posX = mc.player.posX;
        this.posY = mc.player.posY;
        this.posZ = mc.player.posZ;
      } else {
        this.ridingEntity = mc.player.getRidingEntity();
        mc.player.dismountRidingEntity();
      } 
      this.pitch = mc.player.rotationPitch;
      this.yaw = mc.player.rotationYaw;
      this.clonedPlayer = new EntityOtherPlayerMP((World)mc.world, mc.getSession().getProfile());
      this.clonedPlayer.copyLocationAndAnglesFrom((Entity)mc.player);
      this.clonedPlayer.rotationYawHead = mc.player.rotationYawHead;
      mc.world.addEntityToWorld(-100, (Entity)this.clonedPlayer);
      mc.player.capabilities.isFlying = true;
      mc.player.capabilities.setFlySpeed((float)(((Double)this.speed.getValue()).doubleValue() / 100.0D));
      mc.player.noClip = true;
    } 
  }
  
  public void onDisable() {
    EntityPlayerSP entityPlayerSP = mc.player;
    mc.player.setPositionAndRotation(this.posX, this.posY, this.posZ, this.yaw, this.pitch);
    mc.world.removeEntityFromWorld(-100);
    this.clonedPlayer = null;
    this.posX = this.posY = this.posZ = 0.0D;
    this.pitch = this.yaw = 0.0F;
    mc.player.capabilities.isFlying = false;
    mc.player.capabilities.setFlySpeed(0.05F);
    mc.player.noClip = false;
    mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0.0D;
    if (entityPlayerSP != null && this.isRidingEntity)
      mc.player.startRiding(this.ridingEntity, true); 
  }
  
  public void onUpdate() {
    mc.player.capabilities.isFlying = true;
    mc.player.capabilities.setFlySpeed((float)(((Double)this.speed.getValue()).doubleValue() / 100.0D));
    mc.player.noClip = true;
    mc.player.onGround = false;
    mc.player.fallDistance = 0.0F;
  }
  
  @SubscribeEvent
  public void onMove(MoveEvent event) {
    mc.player.noClip = true;
  }
  
  @SubscribeEvent
  public void onPlayerPushOutOfBlock(PlayerSPPushOutOfBlocksEvent event) {
    event.setCanceled(true);
  }
  
  @SubscribeEvent
  public void onPacketSend(PacketEvent event) {
    if ((event.getPacket() instanceof net.minecraft.network.play.client.CPacketPlayer || event.getPacket() instanceof net.minecraft.network.play.client.CPacketInput) && ((Boolean)this.packet.getValue()).booleanValue())
      event.setCanceled(true); 
  }
}
