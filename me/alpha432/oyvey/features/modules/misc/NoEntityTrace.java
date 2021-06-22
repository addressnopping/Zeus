package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class NoEntityTrace extends Module {
  private boolean focus;
  
  public NoEntityTrace() {
    super("NoEntityTrace", "Ignores player hitbox", Module.Category.MISC, true, false, false);
    this.focus = false;
  }
  
  public void onUpdate() {
    mc.world.loadedEntityList.stream()
      .filter(entity -> entity instanceof EntityLivingBase)
      .filter(entity -> (mc.player == entity))
      .map(entity -> (EntityLivingBase)entity)
      .filter(entity -> !entity.isDead)
      .forEach(this::process);
    RayTraceResult normalResult = mc.objectMouseOver;
    if (normalResult != null)
      this.focus = (normalResult.typeOfHit == RayTraceResult.Type.ENTITY); 
  }
  
  private void process(EntityLivingBase event) {
    RayTraceResult bypassEntityResult = event.rayTrace(6.0D, mc.getRenderPartialTicks());
    if (bypassEntityResult != null && this.focus && 
      bypassEntityResult.typeOfHit == RayTraceResult.Type.BLOCK) {
      BlockPos pos = bypassEntityResult.getBlockPos();
      if (mc.gameSettings.keyBindAttack.isKeyDown())
        mc.playerController.onPlayerDamageBlock(pos, EnumFacing.UP); 
    } 
  }
}
