package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BoatFly extends Module {
  public Setting<Double> speed = register(new Setting("Speed", Double.valueOf(3.0D), Double.valueOf(1.0D), Double.valueOf(10.0D)));
  
  public Setting<Double> verticalSpeed = register(new Setting("VerticalSpeed", Double.valueOf(3.0D), Double.valueOf(1.0D), Double.valueOf(10.0D)));
  
  public Setting<Boolean> noKick = register(new Setting("No-Kick", Boolean.valueOf(true)));
  
  public Setting<Boolean> packet = register(new Setting("Packet", Boolean.valueOf(true)));
  
  public Setting<Integer> packets = register(new Setting("Packets", Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(5), v -> ((Boolean)this.packet.getValue()).booleanValue()));
  
  public Setting<Integer> interact = register(new Setting("Delay", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(20)));
  
  public static BoatFly INSTANCE;
  
  private EntityBoat target;
  
  private int teleportID;
  
  public BoatFly() {
    super("BoatFly", "/fly but boat", Module.Category.MOVEMENT, true, true, false);
    INSTANCE = this;
  }
  
  public void onUpdate() {
    if (mc.player == null)
      return; 
    if (mc.world == null || mc.player.getRidingEntity() == null)
      return; 
    if (mc.player.getRidingEntity() instanceof EntityBoat)
      this.target = (EntityBoat)mc.player.ridingEntity; 
    mc.player.getRidingEntity().setNoGravity(true);
    (mc.player.getRidingEntity()).motionY = 0.0D;
    if (mc.gameSettings.keyBindJump.isKeyDown()) {
      (mc.player.getRidingEntity()).onGround = false;
      (mc.player.getRidingEntity()).motionY = ((Double)this.verticalSpeed.getValue()).doubleValue() / 10.0D;
    } 
    if (mc.gameSettings.keyBindSprint.isKeyDown()) {
      (mc.player.getRidingEntity()).onGround = false;
      (mc.player.getRidingEntity()).motionY = -(((Double)this.verticalSpeed.getValue()).doubleValue() / 10.0D);
    } 
    double[] normalDir = directionSpeed(((Double)this.speed.getValue()).doubleValue() / 2.0D);
    if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
      (mc.player.getRidingEntity()).motionX = normalDir[0];
      (mc.player.getRidingEntity()).motionZ = normalDir[1];
    } else {
      (mc.player.getRidingEntity()).motionX = 0.0D;
      (mc.player.getRidingEntity()).motionZ = 0.0D;
    } 
    if (((Boolean)this.noKick.getValue()).booleanValue())
      if (mc.gameSettings.keyBindJump.isKeyDown()) {
        if (mc.player.ticksExisted % 8 < 2)
          (mc.player.getRidingEntity()).motionY = -0.03999999910593033D; 
      } else if (mc.player.ticksExisted % 8 < 4) {
        (mc.player.getRidingEntity()).motionY = -0.07999999821186066D;
      }  
    handlePackets((mc.player.getRidingEntity()).motionX, (mc.player.getRidingEntity()).motionY, (mc.player.getRidingEntity()).motionZ);
  }
  
  public void handlePackets(double x, double y, double z) {
    if (((Boolean)this.packet.getValue()).booleanValue()) {
      Vec3d vec = new Vec3d(x, y, z);
      if (mc.player.getRidingEntity() == null)
        return; 
      Vec3d position = mc.player.getRidingEntity().getPositionVector().add(vec);
      mc.player.getRidingEntity().setPosition(position.x, position.y, position.z);
      mc.player.connection.sendPacket((Packet)new CPacketVehicleMove(mc.player.getRidingEntity()));
      for (int i = 0; i < ((Integer)this.packets.getValue()).intValue(); i++)
        mc.player.connection.sendPacket((Packet)new CPacketConfirmTeleport(this.teleportID++)); 
    } 
  }
  
  @SubscribeEvent
  public void onSendPacket(PacketEvent.Send event) {
    if (event.getPacket() instanceof CPacketVehicleMove && mc.player.isRiding() && mc.player.ticksExisted % ((Integer)this.interact.getValue()).intValue() == 0)
      mc.playerController.interactWithEntity((EntityPlayer)mc.player, mc.player.ridingEntity, EnumHand.OFF_HAND); 
    if ((event.getPacket() instanceof net.minecraft.network.play.client.CPacketPlayer.Rotation || event.getPacket() instanceof net.minecraft.network.play.client.CPacketInput) && mc.player.isRiding())
      event.setCanceled(true); 
  }
  
  @SubscribeEvent
  public void onReceivePacket(PacketEvent.Receive event) {
    if (event.getPacket() instanceof net.minecraft.network.play.server.SPacketMoveVehicle && mc.player.isRiding())
      event.setCanceled(true); 
    if (event.getPacket() instanceof SPacketPlayerPosLook)
      this.teleportID = ((SPacketPlayerPosLook)event.getPacket()).teleportId; 
  }
  
  private double[] directionSpeed(double speed) {
    float forward = mc.player.movementInput.moveForward;
    float side = mc.player.movementInput.moveStrafe;
    float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
    if (forward != 0.0F) {
      if (side > 0.0F) {
        yaw += ((forward > 0.0F) ? -45 : 45);
      } else if (side < 0.0F) {
        yaw += ((forward > 0.0F) ? 45 : -45);
      } 
      side = 0.0F;
      if (forward > 0.0F) {
        forward = 1.0F;
      } else if (forward < 0.0F) {
        forward = -1.0F;
      } 
    } 
    double sin = Math.sin(Math.toRadians((yaw + 90.0F)));
    double cos = Math.cos(Math.toRadians((yaw + 90.0F)));
    double posX = forward * speed * cos + side * speed * sin;
    double posZ = forward * speed * sin - side * speed * cos;
    return new double[] { posX, posZ };
  }
}
