package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtilll;
import me.alpha432.oyvey.util.MathUtilll;
import me.alpha432.oyvey.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GodModule extends Module {
  public Setting<Integer> rotations = register(new Setting("Spoofs", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(20)));
  
  public Setting<Boolean> rotate = register(new Setting("Rotate", Boolean.valueOf(false)));
  
  public Setting<Boolean> render = register(new Setting("Render", Boolean.valueOf(false)));
  
  public Setting<Boolean> antiIllegal = register(new Setting("AntiIllegal", Boolean.valueOf(true)));
  
  public Setting<Boolean> checkPos = register(new Setting("CheckPos", Boolean.valueOf(true)));
  
  public Setting<Boolean> oneDot15 = register(new Setting("1.15", Boolean.valueOf(false)));
  
  public Setting<Boolean> entitycheck = register(new Setting("EntityCheck", Boolean.valueOf(false)));
  
  public Setting<Integer> attacks = register(new Setting("Attacks", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(10)));
  
  public Setting<Integer> delay = register(new Setting("Delay", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(50)));
  
  private float yaw = 0.0F;
  
  private float pitch = 0.0F;
  
  private boolean rotating;
  
  private int rotationPacketsSpoofed;
  
  private int highestID = -100000;
  
  public GodModule() {
    super("GodModule", "Wow", Module.Category.COMBAT, true, true, false);
  }
  
  public void onToggle() {
    resetFields();
    if (mc.world != null)
      updateEntityID(); 
  }
  
  public void onUpdate() {
    if (((Boolean)this.render.getValue()).booleanValue())
      for (Entity entity : mc.world.loadedEntityList) {
        if (!(entity instanceof net.minecraft.entity.item.EntityEnderCrystal))
          continue; 
        entity.setCustomNameTag(String.valueOf(entity.entityId));
        entity.setAlwaysRenderNameTag(true);
      }  
  }
  
  public void onLogout() {
    resetFields();
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onSendPacket(PacketEvent.Send event) {
    if (event.getStage() == 0 && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
      CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock)event.getPacket();
      if (mc.player.getHeldItem(packet.hand).getItem() instanceof net.minecraft.item.ItemEndCrystal) {
        if ((((Boolean)this.checkPos.getValue()).booleanValue() && !BlockUtilll.canPlaceCrystal(packet.position, ((Boolean)this.entitycheck.getValue()).booleanValue(), ((Boolean)this.oneDot15.getValue()).booleanValue())) || checkPlayers())
          return; 
        updateEntityID();
        for (int i = 1; i < ((Integer)this.attacks.getValue()).intValue(); i++)
          attackID(packet.position, this.highestID + i); 
      } 
    } 
    if (event.getStage() == 0 && this.rotating && ((Boolean)this.rotate.getValue()).booleanValue() && event.getPacket() instanceof CPacketPlayer) {
      CPacketPlayer packet = (CPacketPlayer)event.getPacket();
      packet.yaw = this.yaw;
      packet.pitch = this.pitch;
      this.rotationPacketsSpoofed++;
      if (this.rotationPacketsSpoofed >= ((Integer)this.rotations.getValue()).intValue()) {
        this.rotating = false;
        this.rotationPacketsSpoofed = 0;
      } 
    } 
  }
  
  private void attackID(BlockPos pos, int id) {
    Entity entity = mc.world.getEntityByID(id);
    if (entity == null || entity instanceof net.minecraft.entity.item.EntityEnderCrystal) {
      AttackThread attackThread = new AttackThread(id, pos, ((Integer)this.delay.getValue()).intValue(), this);
      attackThread.start();
    } 
  }
  
  @SubscribeEvent
  public void onPacketReceive(PacketEvent.Receive event) {
    if (event.getPacket() instanceof SPacketSpawnObject) {
      checkID(((SPacketSpawnObject)event.getPacket()).getEntityID());
    } else if (event.getPacket() instanceof SPacketSpawnExperienceOrb) {
      checkID(((SPacketSpawnExperienceOrb)event.getPacket()).getEntityID());
    } else if (event.getPacket() instanceof SPacketSpawnPlayer) {
      checkID(((SPacketSpawnPlayer)event.getPacket()).getEntityID());
    } else if (event.getPacket() instanceof SPacketSpawnGlobalEntity) {
      checkID(((SPacketSpawnGlobalEntity)event.getPacket()).getEntityId());
    } else if (event.getPacket() instanceof SPacketSpawnPainting) {
      checkID(((SPacketSpawnPainting)event.getPacket()).getEntityID());
    } else if (event.getPacket() instanceof SPacketSpawnMob) {
      checkID(((SPacketSpawnMob)event.getPacket()).getEntityID());
    } 
  }
  
  private void checkID(int id) {
    if (id > this.highestID)
      this.highestID = id; 
  }
  
  public void updateEntityID() {
    for (Entity entity : mc.world.loadedEntityList) {
      if (entity.getEntityId() <= this.highestID)
        continue; 
      this.highestID = entity.getEntityId();
    } 
  }
  
  private boolean checkPlayers() {
    if (((Boolean)this.antiIllegal.getValue()).booleanValue())
      for (EntityPlayer player : mc.world.playerEntities) {
        if (!checkItem(player.getHeldItemMainhand()) && !checkItem(player.getHeldItemOffhand()))
          continue; 
        return false;
      }  
    return true;
  }
  
  private boolean checkItem(ItemStack stack) {
    return (stack.getItem() instanceof net.minecraft.item.ItemBow || stack.getItem() instanceof net.minecraft.item.ItemExpBottle || stack.getItem() == Items.STRING);
  }
  
  public void rotateTo(BlockPos pos) {
    float[] angle = MathUtilll.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((Vec3i)pos));
    this.yaw = angle[0];
    this.pitch = angle[1];
    this.rotating = true;
  }
  
  private void resetFields() {
    this.rotating = false;
    this.highestID = -1000000;
  }
  
  public static class AttackThread extends Thread {
    private final BlockPos pos;
    
    private final int id;
    
    private final int delay;
    
    private final GodModule godModule;
    
    public AttackThread(int idIn, BlockPos posIn, int delayIn, GodModule godModuleIn) {
      this.id = idIn;
      this.pos = posIn;
      this.delay = delayIn;
      this.godModule = godModuleIn;
    }
    
    public void run() {
      try {
        wait(this.delay);
        CPacketUseEntity attack = new CPacketUseEntity();
        attack.entityId = this.id;
        attack.action = CPacketUseEntity.Action.ATTACK;
        this.godModule.rotateTo(this.pos.up());
        Util.mc.player.connection.sendPacket((Packet)attack);
        Util.mc.player.connection.sendPacket((Packet)new CPacketAnimation(EnumHand.MAIN_HAND));
      } catch (InterruptedException e) {
        e.printStackTrace();
      } 
    }
  }
}
