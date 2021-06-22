package me.alpha432.oyvey.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class TestUtil {
  private static final Minecraft mc = Minecraft.getMinecraft();
  
  public static List<Block> emptyBlocks = Arrays.asList(new Block[] { Blocks.AIR, (Block)Blocks.FLOWING_LAVA, (Block)Blocks.LAVA, (Block)Blocks.FLOWING_WATER, (Block)Blocks.WATER, Blocks.VINE, Blocks.SNOW_LAYER, (Block)Blocks.TALLGRASS, (Block)Blocks.FIRE });
  
  public static List<Block> rightclickableBlocks = Arrays.asList(new Block[] { 
        (Block)Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, 
        Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.ANVIL, 
        Blocks.WOODEN_BUTTON, Blocks.STONE_BUTTON, (Block)Blocks.UNPOWERED_COMPARATOR, (Block)Blocks.UNPOWERED_REPEATER, (Block)Blocks.POWERED_REPEATER, (Block)Blocks.POWERED_COMPARATOR, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, 
        Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.BREWING_STAND, Blocks.DISPENSER, Blocks.DROPPER, Blocks.LEVER, Blocks.NOTEBLOCK, Blocks.JUKEBOX, (Block)Blocks.BEACON, Blocks.BED, 
        Blocks.FURNACE, (Block)Blocks.OAK_DOOR, (Block)Blocks.SPRUCE_DOOR, (Block)Blocks.BIRCH_DOOR, (Block)Blocks.JUNGLE_DOOR, (Block)Blocks.ACACIA_DOOR, (Block)Blocks.DARK_OAK_DOOR, Blocks.CAKE, Blocks.ENCHANTING_TABLE, Blocks.DRAGON_EGG, 
        (Block)Blocks.HOPPER, Blocks.REPEATING_COMMAND_BLOCK, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.CRAFTING_TABLE });
  
  public static boolean canSeeBlock(BlockPos p_Pos) {
    return (mc.player != null && mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(p_Pos.getX(), p_Pos.getY(), p_Pos.getZ()), false, true, false) == null);
  }
  
  public static void placeCrystalOnBlock(BlockPos pos, EnumHand hand) {
    RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D));
    EnumFacing facing = (result == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
    mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, 0.0F, 0.0F, 0.0F));
  }
  
  public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck, float height) {
    return (!shouldCheck || mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX(), (pos.getY() + height), pos.getZ()), false, true, false) == null);
  }
  
  public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck) {
    return rayTracePlaceCheck(pos, shouldCheck, 1.0F);
  }
  
  public static void openBlock(BlockPos pos) {
    EnumFacing[] facings;
    EnumFacing[] arrayOfEnumFacing1;
    int i;
    byte b;
    for (arrayOfEnumFacing1 = facings = EnumFacing.values(), i = arrayOfEnumFacing1.length, b = 0; b < i; ) {
      EnumFacing f = arrayOfEnumFacing1[b];
      Block neighborBlock = mc.world.getBlockState(pos.offset(f)).getBlock();
      if (!emptyBlocks.contains(neighborBlock)) {
        b++;
        continue;
      } 
      mc.playerController.processRightClickBlock(mc.player, mc.world, pos, f.getOpposite(), new Vec3d((Vec3i)pos), EnumHand.MAIN_HAND);
      return;
    } 
  }
  
  public static boolean placeBlock(BlockPos pos) {
    if (isBlockEmpty(pos)) {
      EnumFacing[] facings;
      EnumFacing[] arrayOfEnumFacing1;
      int i;
      byte b;
      for (arrayOfEnumFacing1 = facings = EnumFacing.values(), i = arrayOfEnumFacing1.length, b = 0; b < i; ) {
        EnumFacing f = arrayOfEnumFacing1[b];
        Block neighborBlock = mc.world.getBlockState(pos.offset(f)).getBlock();
        Vec3d vec = new Vec3d(pos.getX() + 0.5D + f.getXOffset() * 0.5D, pos.getY() + 0.5D + f.getYOffset() * 0.5D, pos.getZ() + 0.5D + f.getZOffset() * 0.5D);
        if (emptyBlocks.contains(neighborBlock) || mc.player.getPositionEyes(mc.getRenderPartialTicks()).distanceTo(vec) > 4.25D) {
          b++;
          continue;
        } 
        float[] rot = { mc.player.rotationYaw, mc.player.rotationPitch };
        if (rightclickableBlocks.contains(neighborBlock))
          mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_SNEAKING)); 
        mc.playerController.processRightClickBlock(mc.player, mc.world, pos.offset(f), f.getOpposite(), new Vec3d((Vec3i)pos), EnumHand.MAIN_HAND);
        if (rightclickableBlocks.contains(neighborBlock))
          mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.STOP_SNEAKING)); 
        mc.player.swingArm(EnumHand.MAIN_HAND);
        return true;
      } 
    } 
    return false;
  }
  
  public static boolean isBlockEmpty(BlockPos pos) {
    try {
      if (emptyBlocks.contains(mc.world.getBlockState(pos).getBlock())) {
        AxisAlignedBB box = new AxisAlignedBB(pos);
        Iterator<Entity> entityIter = mc.world.loadedEntityList.iterator();
        while (true) {
          if (entityIter.hasNext()) {
            Entity e;
            if (e = entityIter.next() instanceof net.minecraft.entity.EntityLivingBase && box.intersects(e.getEntityBoundingBox()))
              break; 
            continue;
          } 
          return true;
        } 
      } 
    } catch (Exception exception) {}
    return false;
  }
  
  public static boolean canPlaceBlock(BlockPos pos) {
    if (isBlockEmpty(pos)) {
      EnumFacing[] facings;
      for (EnumFacing f : facings = EnumFacing.values()) {
        if (!emptyBlocks.contains(mc.world.getBlockState(pos.offset(f)).getBlock())) {
          Vec3d vec3d = new Vec3d(pos.getX() + 0.5D + f.getXOffset() * 0.5D, pos.getY() + 0.5D + f.getYOffset() * 0.5D, pos.getZ() + 0.5D + f.getZOffset() * 0.5D);
          if (mc.player.getPositionEyes(mc.getRenderPartialTicks()).distanceTo(vec3d) <= 4.25D)
            return true; 
        } 
      } 
    } 
    return false;
  }
}
