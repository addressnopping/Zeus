package me.alpha432.oyvey.features.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockInteractionUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SelfWeb extends Module {
  public Setting<Boolean> alwayson = register(new Setting("AlwaysOn", Boolean.valueOf(false)));
  
  public Setting<Boolean> rotate = register(new Setting("Rotate", Boolean.valueOf(true)));
  
  public Setting<Integer> webRange = register(new Setting("EnemyRange", Integer.valueOf(4), Integer.valueOf(0), Integer.valueOf(8)));
  
  int new_slot;
  
  boolean sneak;
  
  public SelfWeb() {
    super("SelfWeb", "Places webs at your feet", Module.Category.COMBAT, false, true, false);
    this.new_slot = -1;
    this.sneak = false;
  }
  
  public void enable() {
    if (mc.player != null) {
      this.new_slot = find_in_hotbar();
      if (this.new_slot == -1)
        Command.sendMessage(ChatFormatting.RED + "< " + ChatFormatting.GRAY + "SelfWeb" + ChatFormatting.RED + "> " + ChatFormatting.DARK_RED + "No webs in hotbar!"); 
    } 
  }
  
  public void disable() {
    if (mc.player != null && 
      this.sneak) {
      mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
      this.sneak = false;
    } 
  }
  
  public void onUpdate() {
    if (mc.player == null)
      return; 
    if (((Boolean)this.alwayson.getValue()).booleanValue()) {
      EntityPlayer target = find_closest_target();
      if (target == null)
        return; 
      if (mc.player.getDistance((Entity)target) < ((Integer)this.webRange.getValue()).intValue() && is_surround()) {
        int last_slot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = this.new_slot;
        mc.playerController.updateController();
        place_blocks(WorldUtil.GetLocalPlayerPosFloored());
        mc.player.inventory.currentItem = last_slot;
      } 
    } else {
      int last_slot = mc.player.inventory.currentItem;
      mc.player.inventory.currentItem = this.new_slot;
      mc.playerController.updateController();
      place_blocks(WorldUtil.GetLocalPlayerPosFloored());
      mc.player.inventory.currentItem = last_slot;
      disable();
    } 
  }
  
  public EntityPlayer find_closest_target() {
    if (mc.world.playerEntities.isEmpty())
      return null; 
    EntityPlayer closestTarget = null;
    for (EntityPlayer target : mc.world.playerEntities) {
      if (target == mc.player)
        continue; 
      if (EntityUtil.isLiving((Entity)target))
        continue; 
      if (target.getHealth() <= 0.0F)
        continue; 
      if (closestTarget != null && 
        mc.player.getDistance((Entity)target) > mc.player.getDistance((Entity)closestTarget))
        continue; 
      closestTarget = target;
    } 
    return closestTarget;
  }
  
  private int find_in_hotbar() {
    for (int i = 0; i < 9; i++) {
      ItemStack stack = mc.player.inventory.getStackInSlot(i);
      if (stack.getItem() == Item.getItemById(30))
        return i; 
    } 
    return -1;
  }
  
  private boolean is_surround() {
    BlockPos player_block = WorldUtil.GetLocalPlayerPosFloored();
    return (mc.world.getBlockState(player_block.east()).getBlock() != Blocks.AIR && mc.world
      .getBlockState(player_block.west()).getBlock() != Blocks.AIR && mc.world
      .getBlockState(player_block.north()).getBlock() != Blocks.AIR && mc.world
      .getBlockState(player_block.south()).getBlock() != Blocks.AIR && mc.world
      .getBlockState(player_block).getBlock() == Blocks.AIR);
  }
  
  private void place_blocks(BlockPos pos) {
    if (!mc.world.getBlockState(pos).getMaterial().isReplaceable())
      return; 
    if (!BlockInteractionUtil.checkForNeighbours(pos))
      return; 
    EnumFacing[] arrayOfEnumFacing;
    int i;
    byte b;
    for (arrayOfEnumFacing = EnumFacing.values(), i = arrayOfEnumFacing.length, b = 0; b < i; ) {
      EnumFacing side = arrayOfEnumFacing[b];
      BlockPos neighbor = pos.offset(side);
      EnumFacing side2 = side.getOpposite();
      if (!BlockInteractionUtil.canBeClicked(neighbor)) {
        b++;
        continue;
      } 
      if (BlockInteractionUtil.blackList.contains(mc.world.getBlockState(neighbor).getBlock())) {
        mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_SNEAKING));
        this.sneak = true;
      } 
      Vec3d hitVec = (new Vec3d((Vec3i)neighbor)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(side2.getDirectionVec())).scale(0.5D));
      if (((Boolean)this.rotate.getValue()).booleanValue())
        BlockInteractionUtil.faceVectorPacketInstant(hitVec); 
      mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
      mc.player.swingArm(EnumHand.MAIN_HAND);
      return;
    } 
  }
}
