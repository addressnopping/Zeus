package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.ItemUtil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class InstantSelfFill extends Module {
  private BlockPos originalPos;
  
  private int oldSlot;
  
  public InstantSelfFill() {
    super("InstantSelfFill", "does the thing i guess", Module.Category.COMBAT, true, true, false);
    this.oldSlot = -1;
  }
  
  public void onEnable() {
    this.originalPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
    if (mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)).getBlock().equals(Blocks.OBSIDIAN) || 
      intersectsWithEntity(this.originalPos)) {
      toggle();
      return;
    } 
    this.oldSlot = mc.player.inventory.currentItem;
  }
  
  public void onUpdate() {
    if (ItemUtil.findHotbarBlock(BlockObsidian.class) == -1) {
      Command.sendMessage("Can't find obsidian in hotbar!");
      toggle();
      return;
    } 
    ItemUtil.switchToSlot(ItemUtil.findHotbarBlock(BlockObsidian.class));
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698D, mc.player.posZ, true));
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997D, mc.player.posZ, true));
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214D, mc.player.posZ, true));
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ, true));
    ItemUtil.placeBlock(this.originalPos, EnumHand.MAIN_HAND, true, true, false);
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + -6.395812D, mc.player.posZ, false));
    ItemUtil.switchToSlot(this.oldSlot);
    (Minecraft.getMinecraft()).player.setSneaking(false);
    toggle();
  }
  
  private boolean intersectsWithEntity(BlockPos pos) {
    for (Entity entity : mc.world.loadedEntityList) {
      if (!entity.equals(mc.player) && 
        !(entity instanceof net.minecraft.entity.item.EntityItem) && (
        new AxisAlignedBB(pos)).intersects(entity.getEntityBoundingBox()))
        return true; 
    } 
    return false;
  }
  
  public void onDisable() {
    (Minecraft.getMinecraft()).player.setSneaking(false);
    super.onDisable();
  }
}
