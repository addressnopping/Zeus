package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BurrowUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class Burrow extends Module {
  private final Setting<Integer> offset;
  
  private final Setting<Boolean> rotate;
  
  private final Setting<Mode> mode;
  
  private BlockPos originalPos;
  
  private int oldSlot;
  
  Block returnBlock;
  
  public Burrow() {
    super("Burrow", "TPs you into a block", Module.Category.PLAYER, true, false, false);
    this.offset = register(new Setting("Offset", Integer.valueOf(3), Integer.valueOf(-5), Integer.valueOf(5)));
    this.rotate = register(new Setting("Rotate", Boolean.valueOf(false)));
    this.mode = register(new Setting("Mode", Mode.OBBY));
    this.oldSlot = -1;
    this.returnBlock = null;
  }
  
  public void onEnable() {
    super.onEnable();
    this.originalPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
    switch ((Mode)this.mode.getValue()) {
      case OBBY:
        this.returnBlock = Blocks.OBSIDIAN;
        break;
      case ECHEST:
        this.returnBlock = Blocks.ENDER_CHEST;
        break;
      case ANVIL:
        this.returnBlock = Blocks.ANVIL;
        break;
    } 
    if (mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)).getBlock().equals(this.returnBlock) || intersectsWithEntity(this.originalPos)) {
      toggle();
      return;
    } 
    this.oldSlot = mc.player.inventory.currentItem;
  }
  
  public void onUpdate() {
    switch ((Mode)this.mode.getValue()) {
      case OBBY:
        if (BurrowUtil.findHotbarBlock(BlockObsidian.class) == -1) {
          Command.sendMessage("Can't find obby in hotbar!");
          toggle();
        } 
        break;
      case ECHEST:
        if (BurrowUtil.findHotbarBlock(BlockEnderChest.class) == -1) {
          Command.sendMessage("Can't find echest in hotbar!");
          toggle();
        } 
        break;
      case ANVIL:
        if (BurrowUtil.findHotbarBlock(BlockAnvil.class) == -1) {
          Command.sendMessage("Can't find anvil in hotbar!");
          toggle();
        } 
        break;
    } 
    BurrowUtil.switchToSlot((this.mode.getValue() == Mode.OBBY) ? BurrowUtil.findHotbarBlock(BlockObsidian.class) : ((this.mode.getValue() == Mode.ECHEST) ? BurrowUtil.findHotbarBlock(BlockEnderChest.class) : BurrowUtil.findHotbarBlock(BlockAnvil.class)));
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698D, mc.player.posZ, true));
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997D, mc.player.posZ, true));
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214D, mc.player.posZ, true));
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ, true));
    BurrowUtil.placeBlock(this.originalPos, EnumHand.MAIN_HAND, ((Boolean)this.rotate.getValue()).booleanValue(), true, false);
    mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(mc.player.posX, mc.player.posY + ((Integer)this.offset.getValue()).intValue(), mc.player.posZ, false));
    mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
    mc.player.setSneaking(false);
    BurrowUtil.switchToSlot(this.oldSlot);
    toggle();
  }
  
  private boolean intersectsWithEntity(BlockPos pos) {
    for (Entity entity : mc.world.loadedEntityList) {
      if (entity.equals(mc.player))
        continue; 
      if (entity instanceof net.minecraft.entity.item.EntityItem)
        continue; 
      if ((new AxisAlignedBB(pos)).intersects(entity.getEntityBoundingBox()))
        return true; 
    } 
    return false;
  }
  
  public enum Mode {
    OBBY, ECHEST, ANVIL;
  }
}
