package me.alpha432.oyvey.util;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class HoleUtil {
  public static final List<BlockPos> holeBlocks = Arrays.asList(new BlockPos[] { new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1) });
  
  private static Minecraft mc = Minecraft.getMinecraft();
  
  public static final Vec3d[] cityOffsets = new Vec3d[] { new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, -1.0D) };
  
  public static boolean isInHole() {
    Vec3d playerPos = CombatUtil.interpolateEntity((Entity)mc.player);
    BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
    int size = 0;
    for (BlockPos bPos : holeBlocks) {
      if (CombatUtil.isHard(mc.world.getBlockState(blockpos.add((Vec3i)bPos)).getBlock()))
        size++; 
    } 
    return (size == 5);
  }
}
