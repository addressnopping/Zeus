package me.alpha432.oyvey.features.modules.render;

import java.awt.Color;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ESP extends Module {
  private static ESP INSTANCE = new ESP();
  
  private final Setting<Boolean> items = register(new Setting("Items", Boolean.valueOf(false)));
  
  private final Setting<Boolean> xporbs = register(new Setting("XpOrbs", Boolean.valueOf(false)));
  
  private final Setting<Boolean> xpbottles = register(new Setting("XpBottles", Boolean.valueOf(false)));
  
  private final Setting<Boolean> pearl = register(new Setting("Pearls", Boolean.valueOf(false)));
  
  private final Setting<Integer> red = register(new Setting("Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private final Setting<Integer> green = register(new Setting("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private final Setting<Integer> blue = register(new Setting("Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private final Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", Integer.valueOf(120), Integer.valueOf(0), Integer.valueOf(255)));
  
  private final Setting<Integer> alpha = register(new Setting("Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  public ESP() {
    super("ESP", "Renders a nice ESP.", Module.Category.RENDER, false, true, false);
    setInstance();
  }
  
  public static ESP getInstance() {
    if (INSTANCE == null)
      INSTANCE = new ESP(); 
    return INSTANCE;
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  public void onRender3D(Render3DEvent event) {
    if (((Boolean)this.items.getValue()).booleanValue()) {
      int i = 0;
      for (Entity entity : mc.world.loadedEntityList) {
        if (!(entity instanceof net.minecraft.entity.item.EntityItem) || mc.player.getDistanceSq(entity) >= 2500.0D)
          continue; 
        Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
        AxisAlignedBB bb = new AxisAlignedBB((entity.getEntityBoundingBox()).minX - 0.05D - entity.posX + interp.x, (entity.getEntityBoundingBox()).minY - 0.0D - entity.posY + interp.y, (entity.getEntityBoundingBox()).minZ - 0.05D - entity.posZ + interp.z, (entity.getEntityBoundingBox()).maxX + 0.05D - entity.posX + interp.x, (entity.getEntityBoundingBox()).maxY + 0.1D - entity.posY + interp.y, (entity.getEntityBoundingBox()).maxZ + 0.05D - entity.posZ + interp.z);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0F);
        RenderGlobal.renderFilledBox(bb, ((Integer)this.red.getValue()).intValue() / 255.0F, ((Integer)this.green.getValue()).intValue() / 255.0F, ((Integer)this.blue.getValue()).intValue() / 255.0F, ((Integer)this.boxAlpha.getValue()).intValue() / 255.0F);
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderUtil.drawBlockOutline(bb, new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue(), ((Integer)this.alpha.getValue()).intValue()), 1.0F);
        if (++i < 50);
      } 
    } 
    if (((Boolean)this.xporbs.getValue()).booleanValue()) {
      int i = 0;
      for (Entity entity : mc.world.loadedEntityList) {
        if (!(entity instanceof net.minecraft.entity.item.EntityXPOrb) || mc.player.getDistanceSq(entity) >= 2500.0D)
          continue; 
        Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
        AxisAlignedBB bb = new AxisAlignedBB((entity.getEntityBoundingBox()).minX - 0.05D - entity.posX + interp.x, (entity.getEntityBoundingBox()).minY - 0.0D - entity.posY + interp.y, (entity.getEntityBoundingBox()).minZ - 0.05D - entity.posZ + interp.z, (entity.getEntityBoundingBox()).maxX + 0.05D - entity.posX + interp.x, (entity.getEntityBoundingBox()).maxY + 0.1D - entity.posY + interp.y, (entity.getEntityBoundingBox()).maxZ + 0.05D - entity.posZ + interp.z);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0F);
        RenderGlobal.renderFilledBox(bb, ((Integer)this.red.getValue()).intValue() / 255.0F, ((Integer)this.green.getValue()).intValue() / 255.0F, ((Integer)this.blue.getValue()).intValue() / 255.0F, ((Integer)this.boxAlpha.getValue()).intValue() / 255.0F);
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderUtil.drawBlockOutline(bb, new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue(), ((Integer)this.alpha.getValue()).intValue()), 1.0F);
        if (++i < 50);
      } 
    } 
    if (((Boolean)this.pearl.getValue()).booleanValue()) {
      int i = 0;
      for (Entity entity : mc.world.loadedEntityList) {
        if (!(entity instanceof net.minecraft.entity.item.EntityEnderPearl) || mc.player.getDistanceSq(entity) >= 2500.0D)
          continue; 
        Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
        AxisAlignedBB bb = new AxisAlignedBB((entity.getEntityBoundingBox()).minX - 0.05D - entity.posX + interp.x, (entity.getEntityBoundingBox()).minY - 0.0D - entity.posY + interp.y, (entity.getEntityBoundingBox()).minZ - 0.05D - entity.posZ + interp.z, (entity.getEntityBoundingBox()).maxX + 0.05D - entity.posX + interp.x, (entity.getEntityBoundingBox()).maxY + 0.1D - entity.posY + interp.y, (entity.getEntityBoundingBox()).maxZ + 0.05D - entity.posZ + interp.z);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0F);
        RenderGlobal.renderFilledBox(bb, ((Integer)this.red.getValue()).intValue() / 255.0F, ((Integer)this.green.getValue()).intValue() / 255.0F, ((Integer)this.blue.getValue()).intValue() / 255.0F, ((Integer)this.boxAlpha.getValue()).intValue() / 255.0F);
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderUtil.drawBlockOutline(bb, new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue(), ((Integer)this.alpha.getValue()).intValue()), 1.0F);
        if (++i < 50);
      } 
    } 
    if (((Boolean)this.xpbottles.getValue()).booleanValue()) {
      int i = 0;
      for (Entity entity : mc.world.loadedEntityList) {
        if (!(entity instanceof net.minecraft.entity.item.EntityExpBottle) || mc.player.getDistanceSq(entity) >= 2500.0D)
          continue; 
        Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
        AxisAlignedBB bb = new AxisAlignedBB((entity.getEntityBoundingBox()).minX - 0.05D - entity.posX + interp.x, (entity.getEntityBoundingBox()).minY - 0.0D - entity.posY + interp.y, (entity.getEntityBoundingBox()).minZ - 0.05D - entity.posZ + interp.z, (entity.getEntityBoundingBox()).maxX + 0.05D - entity.posX + interp.x, (entity.getEntityBoundingBox()).maxY + 0.1D - entity.posY + interp.y, (entity.getEntityBoundingBox()).maxZ + 0.05D - entity.posZ + interp.z);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0F);
        RenderGlobal.renderFilledBox(bb, ((Integer)this.red.getValue()).intValue() / 255.0F, ((Integer)this.green.getValue()).intValue() / 255.0F, ((Integer)this.blue.getValue()).intValue() / 255.0F, ((Integer)this.boxAlpha.getValue()).intValue() / 255.0F);
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderUtil.drawBlockOutline(bb, new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue(), ((Integer)this.alpha.getValue()).intValue()), 1.0F);
        if (++i < 50);
      } 
    } 
  }
}
