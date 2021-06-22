package me.alpha432.oyvey.util;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import me.alpha432.oyvey.OyVey;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

public class RenderUtil implements Util {
  public static void updateModelViewProjectionMatrix() {
    GL11.glGetFloat(2982, modelView);
    GL11.glGetFloat(2983, projection);
    GL11.glGetInteger(2978, viewport);
    ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
  }
  
  public static void drawRectangleCorrectly(int x, int y, int w, int h, int color) {
    GL11.glLineWidth(1.0F);
    Gui.drawRect(x, y, x + w, y + h, color);
  }
  
  public static AxisAlignedBB interpolateAxis(AxisAlignedBB bb) {
    return new AxisAlignedBB(bb.minX - (mc.getRenderManager()).viewerPosX, bb.minY - (mc.getRenderManager()).viewerPosY, bb.minZ - (mc.getRenderManager()).viewerPosZ, bb.maxX - (mc.getRenderManager()).viewerPosX, bb.maxY - (mc.getRenderManager()).viewerPosY, bb.maxZ - (mc.getRenderManager()).viewerPosZ);
  }
  
  public static void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height, int zLevel) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder BufferBuilder2 = tessellator.getBuffer();
    BufferBuilder2.begin(7, DefaultVertexFormats.POSITION_TEX);
    BufferBuilder2.pos((x + 0), (y + height), zLevel).tex(((textureX + 0) * 0.00390625F), ((textureY + height) * 0.00390625F)).endVertex();
    BufferBuilder2.pos((x + width), (y + height), zLevel).tex(((textureX + width) * 0.00390625F), ((textureY + height) * 0.00390625F)).endVertex();
    BufferBuilder2.pos((x + width), (y + 0), zLevel).tex(((textureX + width) * 0.00390625F), ((textureY + 0) * 0.00390625F)).endVertex();
    BufferBuilder2.pos((x + 0), (y + 0), zLevel).tex(((textureX + 0) * 0.00390625F), ((textureY + 0) * 0.00390625F)).endVertex();
    tessellator.draw();
  }
  
  public static void drawOpenGradientBox(BlockPos pos, Color startColor, Color endColor, double height) {
    for (EnumFacing face : EnumFacing.values()) {
      if (face != EnumFacing.UP)
        drawGradientPlane(pos, face, startColor, endColor, height); 
    } 
  }
  
  public static void drawClosedGradientBox(BlockPos pos, Color startColor, Color endColor, double height) {
    for (EnumFacing face : EnumFacing.values())
      drawGradientPlane(pos, face, startColor, endColor, height); 
  }
  
  public static void drawTricolorGradientBox(BlockPos pos, Color startColor, Color midColor, Color endColor) {
    for (EnumFacing face : EnumFacing.values()) {
      if (face != EnumFacing.UP)
        drawGradientPlane(pos, face, startColor, midColor, true, false); 
    } 
    for (EnumFacing face : EnumFacing.values()) {
      if (face != EnumFacing.DOWN)
        drawGradientPlane(pos, face, midColor, endColor, true, true); 
    } 
  }
  
  public static void drawGradientPlane(BlockPos pos, EnumFacing face, Color startColor, Color endColor, boolean half, boolean top) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuffer();
    IBlockState iblockstate = mc.world.getBlockState(pos);
    Vec3d interp = EntityUtil.interpolateEntity((Entity)mc.player, mc.getRenderPartialTicks());
    AxisAlignedBB bb = iblockstate.getSelectedBoundingBox((World)mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z);
    float red = startColor.getRed() / 255.0F;
    float green = startColor.getGreen() / 255.0F;
    float blue = startColor.getBlue() / 255.0F;
    float alpha = startColor.getAlpha() / 255.0F;
    float red2 = endColor.getRed() / 255.0F;
    float green2 = endColor.getGreen() / 255.0F;
    float blue2 = endColor.getBlue() / 255.0F;
    float alpha2 = endColor.getAlpha() / 255.0F;
    double x1 = 0.0D;
    double y1 = 0.0D;
    double z1 = 0.0D;
    double x2 = 0.0D;
    double y2 = 0.0D;
    double z2 = 0.0D;
    if (face == EnumFacing.DOWN) {
      x1 = bb.minX;
      x2 = bb.maxX;
      y1 = bb.minY + (top ? 0.5D : 0.0D);
      y2 = bb.minY + (top ? 0.5D : 0.0D);
      z1 = bb.minZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.UP) {
      x1 = bb.minX;
      x2 = bb.maxX;
      y1 = bb.maxY / (half ? 2 : true);
      y2 = bb.maxY / (half ? 2 : true);
      z1 = bb.minZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.EAST) {
      x1 = bb.maxX;
      x2 = bb.maxX;
      y1 = bb.minY + (top ? 0.5D : 0.0D);
      y2 = bb.maxY / (half ? 2 : true);
      z1 = bb.minZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.WEST) {
      x1 = bb.minX;
      x2 = bb.minX;
      y1 = bb.minY + (top ? 0.5D : 0.0D);
      y2 = bb.maxY / (half ? 2 : true);
      z1 = bb.minZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.SOUTH) {
      x1 = bb.minX;
      x2 = bb.maxX;
      y1 = bb.minY + (top ? 0.5D : 0.0D);
      y2 = bb.maxY / (half ? 2 : true);
      z1 = bb.maxZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.NORTH) {
      x1 = bb.minX;
      x2 = bb.maxX;
      y1 = bb.minY + (top ? 0.5D : 0.0D);
      y2 = bb.maxY / (half ? 2 : true);
      z1 = bb.minZ;
      z2 = bb.minZ;
    } 
    GlStateManager.pushMatrix();
    GlStateManager.disableDepth();
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.depthMask(false);
    builder.begin(5, DefaultVertexFormats.POSITION_COLOR);
    if (face == EnumFacing.EAST || face == EnumFacing.WEST || face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
    } else if (face == EnumFacing.UP) {
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
    } else if (face == EnumFacing.DOWN) {
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    } 
    tessellator.draw();
    GlStateManager.depthMask(true);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.popMatrix();
  }
  
  public static void drawGradientPlane(BlockPos pos, EnumFacing face, Color startColor, Color endColor, double height) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuffer();
    IBlockState iblockstate = mc.world.getBlockState(pos);
    Vec3d interp = EntityUtil.interpolateEntity((Entity)mc.player, mc.getRenderPartialTicks());
    AxisAlignedBB bb = iblockstate.getSelectedBoundingBox((World)mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z).expand(0.0D, height, 0.0D);
    float red = startColor.getRed() / 255.0F;
    float green = startColor.getGreen() / 255.0F;
    float blue = startColor.getBlue() / 255.0F;
    float alpha = startColor.getAlpha() / 255.0F;
    float red2 = endColor.getRed() / 255.0F;
    float green2 = endColor.getGreen() / 255.0F;
    float blue2 = endColor.getBlue() / 255.0F;
    float alpha2 = endColor.getAlpha() / 255.0F;
    double x1 = 0.0D;
    double y1 = 0.0D;
    double z1 = 0.0D;
    double x2 = 0.0D;
    double y2 = 0.0D;
    double z2 = 0.0D;
    if (face == EnumFacing.DOWN) {
      x1 = bb.minX;
      x2 = bb.maxX;
      y1 = bb.minY;
      y2 = bb.minY;
      z1 = bb.minZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.UP) {
      x1 = bb.minX;
      x2 = bb.maxX;
      y1 = bb.maxY;
      y2 = bb.maxY;
      z1 = bb.minZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.EAST) {
      x1 = bb.maxX;
      x2 = bb.maxX;
      y1 = bb.minY;
      y2 = bb.maxY;
      z1 = bb.minZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.WEST) {
      x1 = bb.minX;
      x2 = bb.minX;
      y1 = bb.minY;
      y2 = bb.maxY;
      z1 = bb.minZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.SOUTH) {
      x1 = bb.minX;
      x2 = bb.maxX;
      y1 = bb.minY;
      y2 = bb.maxY;
      z1 = bb.maxZ;
      z2 = bb.maxZ;
    } else if (face == EnumFacing.NORTH) {
      x1 = bb.minX;
      x2 = bb.maxX;
      y1 = bb.minY;
      y2 = bb.maxY;
      z1 = bb.minZ;
      z2 = bb.minZ;
    } 
    GlStateManager.pushMatrix();
    GlStateManager.disableDepth();
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.depthMask(false);
    builder.begin(5, DefaultVertexFormats.POSITION_COLOR);
    if (face == EnumFacing.EAST || face == EnumFacing.WEST || face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
    } else if (face == EnumFacing.UP) {
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
    } else if (face == EnumFacing.DOWN) {
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    } 
    tessellator.draw();
    GlStateManager.depthMask(true);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.popMatrix();
  }
  
  public static void drawGradientRect(int x, int y, int w, int h, int startColor, int endColor) {
    float f = (startColor >> 24 & 0xFF) / 255.0F;
    float f2 = (startColor >> 16 & 0xFF) / 255.0F;
    float f3 = (startColor >> 8 & 0xFF) / 255.0F;
    float f4 = (startColor & 0xFF) / 255.0F;
    float f5 = (endColor >> 24 & 0xFF) / 255.0F;
    float f6 = (endColor >> 16 & 0xFF) / 255.0F;
    float f7 = (endColor >> 8 & 0xFF) / 255.0F;
    float f8 = (endColor & 0xFF) / 255.0F;
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    GlStateManager.shadeModel(7425);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder vertexbuffer = tessellator.getBuffer();
    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    vertexbuffer.pos(x + w, y, 0.0D).color(f2, f3, f4, f).endVertex();
    vertexbuffer.pos(x, y, 0.0D).color(f2, f3, f4, f).endVertex();
    vertexbuffer.pos(x, y + h, 0.0D).color(f6, f7, f8, f5).endVertex();
    vertexbuffer.pos(x + w, y + h, 0.0D).color(f6, f7, f8, f5).endVertex();
    tessellator.draw();
    GlStateManager.shadeModel(7424);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
  }
  
  public static void drawGradientBlockOutline(BlockPos pos, Color startColor, Color endColor, float linewidth, double height) {
    IBlockState iblockstate = mc.world.getBlockState(pos);
    Vec3d interp = EntityUtil.interpolateEntity((Entity)mc.player, mc.getRenderPartialTicks());
    drawGradientBlockOutline(iblockstate.getSelectedBoundingBox((World)mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z).expand(0.0D, height, 0.0D), startColor, endColor, linewidth);
  }
  
  public static void drawProperGradientBlockOutline(BlockPos pos, Color startColor, Color midColor, Color endColor, float linewidth) {
    IBlockState iblockstate = mc.world.getBlockState(pos);
    Vec3d interp = EntityUtil.interpolateEntity((Entity)mc.player, mc.getRenderPartialTicks());
    drawProperGradientBlockOutline(iblockstate.getSelectedBoundingBox((World)mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z), startColor, midColor, endColor, linewidth);
  }
  
  public static void drawProperGradientBlockOutline(AxisAlignedBB bb, Color startColor, Color midColor, Color endColor, float linewidth) {
    float red = endColor.getRed() / 255.0F;
    float green = endColor.getGreen() / 255.0F;
    float blue = endColor.getBlue() / 255.0F;
    float alpha = endColor.getAlpha() / 255.0F;
    float red2 = midColor.getRed() / 255.0F;
    float green2 = midColor.getGreen() / 255.0F;
    float blue2 = midColor.getBlue() / 255.0F;
    float alpha2 = midColor.getAlpha() / 255.0F;
    float red3 = startColor.getRed() / 255.0F;
    float green3 = startColor.getGreen() / 255.0F;
    float blue3 = startColor.getBlue() / 255.0F;
    float alpha3 = startColor.getAlpha() / 255.0F;
    double dif = (bb.maxY - bb.minY) / 2.0D;
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    GL11.glLineWidth(linewidth);
    GL11.glBegin(1);
    GL11.glColor4d(red, green, blue, alpha);
    GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
    GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
    GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
    GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
    GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
    GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
    GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
    GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
    GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
    GL11.glColor4d(red2, green2, blue2, alpha2);
    GL11.glVertex3d(bb.minX, bb.minY + dif, bb.minZ);
    GL11.glVertex3d(bb.minX, bb.minY + dif, bb.minZ);
    GL11.glColor4f(red3, green3, blue3, alpha3);
    GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
    GL11.glColor4d(red, green, blue, alpha);
    GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
    GL11.glColor4d(red2, green2, blue2, alpha2);
    GL11.glVertex3d(bb.minX, bb.minY + dif, bb.maxZ);
    GL11.glVertex3d(bb.minX, bb.minY + dif, bb.maxZ);
    GL11.glColor4d(red3, green3, blue3, alpha3);
    GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
    GL11.glColor4d(red, green, blue, alpha);
    GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
    GL11.glColor4d(red2, green2, blue2, alpha2);
    GL11.glVertex3d(bb.maxX, bb.minY + dif, bb.maxZ);
    GL11.glVertex3d(bb.maxX, bb.minY + dif, bb.maxZ);
    GL11.glColor4d(red3, green3, blue3, alpha3);
    GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
    GL11.glColor4d(red, green, blue, alpha);
    GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
    GL11.glColor4d(red2, green2, blue2, alpha2);
    GL11.glVertex3d(bb.maxX, bb.minY + dif, bb.minZ);
    GL11.glVertex3d(bb.maxX, bb.minY + dif, bb.minZ);
    GL11.glColor4d(red3, green3, blue3, alpha3);
    GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
    GL11.glColor4d(red3, green3, blue3, alpha3);
    GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
    GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
    GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
    GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
    GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
    GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
    GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
    GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
    GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
    GL11.glEnd();
    GL11.glDisable(2848);
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void drawGradientBlockOutline(AxisAlignedBB bb, Color startColor, Color endColor, float linewidth) {
    float red = startColor.getRed() / 255.0F;
    float green = startColor.getGreen() / 255.0F;
    float blue = startColor.getBlue() / 255.0F;
    float alpha = startColor.getAlpha() / 255.0F;
    float red2 = endColor.getRed() / 255.0F;
    float green2 = endColor.getGreen() / 255.0F;
    float blue2 = endColor.getBlue() / 255.0F;
    float alpha2 = endColor.getAlpha() / 255.0F;
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    GL11.glLineWidth(linewidth);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GL11.glDisable(2848);
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void drawGradientFilledBox(BlockPos pos, Color startColor, Color endColor) {
    IBlockState iblockstate = mc.world.getBlockState(pos);
    Vec3d interp = EntityUtil.interpolateEntity((Entity)mc.player, mc.getRenderPartialTicks());
    drawGradientFilledBox(iblockstate.getSelectedBoundingBox((World)mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z), startColor, endColor);
  }
  
  public static void drawGradientFilledBox(AxisAlignedBB bb, Color startColor, Color endColor) {
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    float alpha = endColor.getAlpha() / 255.0F;
    float red = endColor.getRed() / 255.0F;
    float green = endColor.getGreen() / 255.0F;
    float blue = endColor.getBlue() / 255.0F;
    float alpha2 = startColor.getAlpha() / 255.0F;
    float red2 = startColor.getRed() / 255.0F;
    float green2 = startColor.getGreen() / 255.0F;
    float blue2 = startColor.getBlue() / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void drawGradientRect(float x, float y, float w, float h, int startColor, int endColor) {
    float f = (startColor >> 24 & 0xFF) / 255.0F;
    float f2 = (startColor >> 16 & 0xFF) / 255.0F;
    float f3 = (startColor >> 8 & 0xFF) / 255.0F;
    float f4 = (startColor & 0xFF) / 255.0F;
    float f5 = (endColor >> 24 & 0xFF) / 255.0F;
    float f6 = (endColor >> 16 & 0xFF) / 255.0F;
    float f7 = (endColor >> 8 & 0xFF) / 255.0F;
    float f8 = (endColor & 0xFF) / 255.0F;
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    GlStateManager.shadeModel(7425);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder vertexbuffer = tessellator.getBuffer();
    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    vertexbuffer.pos(x + w, y, 0.0D).color(f2, f3, f4, f).endVertex();
    vertexbuffer.pos(x, y, 0.0D).color(f2, f3, f4, f).endVertex();
    vertexbuffer.pos(x, y + h, 0.0D).color(f6, f7, f8, f5).endVertex();
    vertexbuffer.pos(x + w, y + h, 0.0D).color(f6, f7, f8, f5).endVertex();
    tessellator.draw();
    GlStateManager.shadeModel(7424);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
  }
  
  public static void drawFilledCircle(double x, double y, double z, Color color, double radius) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuffer();
    builder.begin(5, DefaultVertexFormats.POSITION_COLOR);
  }
  
  public static void drawGradientBoxTest(BlockPos pos, Color startColor, Color endColor) {}
  
  public static void blockESP(BlockPos b, Color c, double length, double length2) {
    blockEsp(b, c, length, length2);
  }
  
  public static void drawBoxESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air) {
    if (box)
      drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha)); 
    if (outline)
      drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air); 
  }
  
  public static void drawBoxESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air, double height, boolean gradientBox, boolean gradientOutline, boolean invertGradientBox, boolean invertGradientOutline, int gradientAlpha) {
    if (box)
      drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha), height, gradientBox, invertGradientBox, gradientAlpha); 
    if (outline)
      drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air, height, gradientOutline, invertGradientOutline, gradientAlpha); 
  }
  
  public static void glScissor(float x, float y, float x1, float y1, ScaledResolution sr) {
    GL11.glScissor((int)(x * sr.getScaleFactor()), (int)(mc.displayHeight - y1 * sr.getScaleFactor()), (int)((x1 - x) * sr.getScaleFactor()), (int)((y1 - y) * sr.getScaleFactor()));
  }
  
  public static void drawLine(float x, float y, float x1, float y1, float thickness, int hex) {
    float red = (hex >> 16 & 0xFF) / 255.0F;
    float green = (hex >> 8 & 0xFF) / 255.0F;
    float blue = (hex & 0xFF) / 255.0F;
    float alpha = (hex >> 24 & 0xFF) / 255.0F;
    GlStateManager.pushMatrix();
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.shadeModel(7425);
    GL11.glLineWidth(thickness);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(x1, y1, 0.0D).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GlStateManager.shadeModel(7424);
    GL11.glDisable(2848);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.popMatrix();
  }
  
  public static void drawBox(BlockPos pos, Color color) {
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (mc.getRenderManager()).viewerPosX, pos.getY() - (mc.getRenderManager()).viewerPosY, pos.getZ() - (mc.getRenderManager()).viewerPosZ, (pos.getX() + 1) - (mc.getRenderManager()).viewerPosX, (pos.getY() + 1) - (mc.getRenderManager()).viewerPosY, (pos.getZ() + 1) - (mc.getRenderManager()).viewerPosZ);
    camera.setPosition(((Entity)Objects.requireNonNull((T)mc.getRenderViewEntity())).posX, (mc.getRenderViewEntity()).posY, (mc.getRenderViewEntity()).posZ);
    if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + (mc.getRenderManager()).viewerPosX, bb.minY + (mc.getRenderManager()).viewerPosY, bb.minZ + (mc.getRenderManager()).viewerPosZ, bb.maxX + (mc.getRenderManager()).viewerPosX, bb.maxY + (mc.getRenderManager()).viewerPosY, bb.maxZ + (mc.getRenderManager()).viewerPosZ))) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
    } 
  }
  
  public static void drawBetterGradientBox(BlockPos pos, Color startColor, Color endColor) {
    float red = startColor.getRed() / 255.0F;
    float green = startColor.getGreen() / 255.0F;
    float blue = startColor.getBlue() / 255.0F;
    float alpha = startColor.getAlpha() / 255.0F;
    float red2 = endColor.getRed() / 255.0F;
    float green2 = endColor.getGreen() / 255.0F;
    float blue2 = endColor.getBlue() / 255.0F;
    float alpha2 = endColor.getAlpha() / 255.0F;
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (mc.getRenderManager()).viewerPosX, pos.getY() - (mc.getRenderManager()).viewerPosY, pos.getZ() - (mc.getRenderManager()).viewerPosZ, (pos.getX() + 1) - (mc.getRenderManager()).viewerPosX, (pos.getY() + 1) - (mc.getRenderManager()).viewerPosY, (pos.getZ() + 1) - (mc.getRenderManager()).viewerPosZ);
    double offset = (bb.maxY - bb.minY) / 2.0D;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuffer();
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    builder.begin(5, DefaultVertexFormats.POSITION_COLOR);
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
  }
  
  public static void drawBetterGradientBox(BlockPos pos, Color startColor, Color midColor, Color endColor) {
    float red = startColor.getRed() / 255.0F;
    float green = startColor.getGreen() / 255.0F;
    float blue = startColor.getBlue() / 255.0F;
    float alpha = startColor.getAlpha() / 255.0F;
    float red2 = endColor.getRed() / 255.0F;
    float green2 = endColor.getGreen() / 255.0F;
    float blue2 = endColor.getBlue() / 255.0F;
    float alpha2 = endColor.getAlpha() / 255.0F;
    float red3 = midColor.getRed() / 255.0F;
    float green3 = midColor.getGreen() / 255.0F;
    float blue3 = midColor.getBlue() / 255.0F;
    float alpha3 = midColor.getAlpha() / 255.0F;
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (mc.getRenderManager()).viewerPosX, pos.getY() - (mc.getRenderManager()).viewerPosY, pos.getZ() - (mc.getRenderManager()).viewerPosZ, (pos.getX() + 1) - (mc.getRenderManager()).viewerPosX, (pos.getY() + 1) - (mc.getRenderManager()).viewerPosY, (pos.getZ() + 1) - (mc.getRenderManager()).viewerPosZ);
    double offset = (bb.maxY - bb.minY) / 2.0D;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuffer();
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    builder.begin(5, DefaultVertexFormats.POSITION_COLOR);
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY + offset, bb.minZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.minX, bb.minY + offset, bb.maxZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.minX, bb.minY + offset, bb.maxZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY + offset, bb.maxZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY + offset, bb.minZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.minX, bb.minY + offset, bb.minZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.minX, bb.minY + offset, bb.minZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.minX, bb.minY + offset, bb.maxZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.minY + offset, bb.maxZ).color(red3, green3, blue3, alpha3).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.minY + offset, bb.maxZ).color(red3, green3, blue3, alpha3).endVertex();
    tessellator.draw();
    GL11.glDisable(2848);
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void drawEvenBetterGradientBox(BlockPos pos, Color startColor, Color midColor, Color endColor) {
    float red = startColor.getRed() / 255.0F;
    float green = startColor.getGreen() / 255.0F;
    float blue = startColor.getBlue() / 255.0F;
    float alpha = startColor.getAlpha() / 255.0F;
    float red2 = endColor.getRed() / 255.0F;
    float green2 = endColor.getGreen() / 255.0F;
    float blue2 = endColor.getBlue() / 255.0F;
    float alpha2 = endColor.getAlpha() / 255.0F;
    float red3 = midColor.getRed() / 255.0F;
    float green3 = midColor.getGreen() / 255.0F;
    float blue3 = midColor.getBlue() / 255.0F;
    float alpha3 = midColor.getAlpha() / 255.0F;
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (mc.getRenderManager()).viewerPosX, pos.getY() - (mc.getRenderManager()).viewerPosY, pos.getZ() - (mc.getRenderManager()).viewerPosZ, (pos.getX() + 1) - (mc.getRenderManager()).viewerPosX, (pos.getY() + 1) - (mc.getRenderManager()).viewerPosY, (pos.getZ() + 1) - (mc.getRenderManager()).viewerPosZ);
    double offset = (bb.maxY - bb.minY) / 2.0D;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuffer();
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    builder.begin(5, DefaultVertexFormats.POSITION_COLOR);
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GL11.glDisable(2848);
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void drawBox(BlockPos pos, Color color, double height, boolean gradient, boolean invert, int alpha) {
    if (gradient) {
      Color endColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
      drawOpenGradientBox(pos, invert ? endColor : color, invert ? color : endColor, height);
      return;
    } 
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (mc.getRenderManager()).viewerPosX, pos.getY() - (mc.getRenderManager()).viewerPosY, pos.getZ() - (mc.getRenderManager()).viewerPosZ, (pos.getX() + 1) - (mc.getRenderManager()).viewerPosX, (pos.getY() + 1) - (mc.getRenderManager()).viewerPosY + height, (pos.getZ() + 1) - (mc.getRenderManager()).viewerPosZ);
    camera.setPosition(((Entity)Objects.requireNonNull((T)mc.getRenderViewEntity())).posX, (mc.getRenderViewEntity()).posY, (mc.getRenderViewEntity()).posZ);
    if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + (mc.getRenderManager()).viewerPosX, bb.minY + (mc.getRenderManager()).viewerPosY, bb.minZ + (mc.getRenderManager()).viewerPosZ, bb.maxX + (mc.getRenderManager()).viewerPosX, bb.maxY + (mc.getRenderManager()).viewerPosY, bb.maxZ + (mc.getRenderManager()).viewerPosZ))) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
    } 
  }
  
  public static void drawBlockOutline(BlockPos pos, Color color, float linewidth, boolean air) {
    IBlockState iblockstate = mc.world.getBlockState(pos);
    if ((air || iblockstate.getMaterial() != Material.AIR) && mc.world.getWorldBorder().contains(pos)) {
      Vec3d interp = EntityUtil.interpolateEntity((Entity)mc.player, mc.getRenderPartialTicks());
      drawBlockOutline(iblockstate.getSelectedBoundingBox((World)mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z), color, linewidth);
    } 
  }
  
  public static void drawBlockOutline(BlockPos pos, Color color, float linewidth, boolean air, double height, boolean gradient, boolean invert, int alpha) {
    if (gradient) {
      Color endColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
      drawGradientBlockOutline(pos, invert ? endColor : color, invert ? color : endColor, linewidth, height);
      return;
    } 
    IBlockState iblockstate = mc.world.getBlockState(pos);
    if ((air || iblockstate.getMaterial() != Material.AIR) && mc.world.getWorldBorder().contains(pos)) {
      AxisAlignedBB blockAxis = new AxisAlignedBB(pos.getX() - (mc.getRenderManager()).viewerPosX, pos.getY() - (mc.getRenderManager()).viewerPosY, pos.getZ() - (mc.getRenderManager()).viewerPosZ, (pos.getX() + 1) - (mc.getRenderManager()).viewerPosX, (pos.getY() + 1) - (mc.getRenderManager()).viewerPosY + height, (pos.getZ() + 1) - (mc.getRenderManager()).viewerPosZ);
      drawBlockOutline(blockAxis.grow(0.0020000000949949026D), color, linewidth);
    } 
  }
  
  public static void drawBlockOutline(AxisAlignedBB bb, Color color, float linewidth) {
    float red = color.getRed() / 255.0F;
    float green = color.getGreen() / 255.0F;
    float blue = color.getBlue() / 255.0F;
    float alpha = color.getAlpha() / 255.0F;
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    GL11.glLineWidth(linewidth);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GL11.glDisable(2848);
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void drawBoxESP(BlockPos pos, Color color, float lineWidth, boolean outline, boolean box, int boxAlpha) {
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (mc.getRenderManager()).viewerPosX, pos.getY() - (mc.getRenderManager()).viewerPosY, pos.getZ() - (mc.getRenderManager()).viewerPosZ, (pos.getX() + 1) - (mc.getRenderManager()).viewerPosX, (pos.getY() + 1) - (mc.getRenderManager()).viewerPosY, (pos.getZ() + 1) - (mc.getRenderManager()).viewerPosZ);
    camera.setPosition(((Entity)Objects.requireNonNull((T)mc.getRenderViewEntity())).posX, (mc.getRenderViewEntity()).posY, (mc.getRenderViewEntity()).posZ);
    if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + (mc.getRenderManager()).viewerPosX, bb.minY + (mc.getRenderManager()).viewerPosY, bb.minZ + (mc.getRenderManager()).viewerPosZ, bb.maxX + (mc.getRenderManager()).viewerPosX, bb.maxY + (mc.getRenderManager()).viewerPosY, bb.maxZ + (mc.getRenderManager()).viewerPosZ))) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(lineWidth);
      double dist = mc.player.getDistance((pos.getX() + 0.5F), (pos.getY() + 0.5F), (pos.getZ() + 0.5F)) * 0.75D;
      if (box)
        RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, boxAlpha / 255.0F); 
      if (outline)
        RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F); 
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
    } 
  }
  
  public static void drawText(BlockPos pos, String text) {
    if (pos == null || text == null)
      return; 
    GlStateManager.pushMatrix();
    glBillboardDistanceScaled(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, (EntityPlayer)mc.player, 1.0F);
    GlStateManager.disableDepth();
    GlStateManager.translate(-(OyVey.textManager.getStringWidth(text) / 2.0D), 0.0D, 0.0D);
    OyVey.textManager.drawStringWithShadow(text, 0.0F, 0.0F, -5592406);
    GlStateManager.popMatrix();
  }
  
  public static void drawOutlinedBlockESP(BlockPos pos, Color color, float linewidth) {
    IBlockState iblockstate = mc.world.getBlockState(pos);
    Vec3d interp = EntityUtil.interpolateEntity((Entity)mc.player, mc.getRenderPartialTicks());
    drawBoundingBox(iblockstate.getSelectedBoundingBox((World)mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z), linewidth, ColorUtil.toRGBA(color));
  }
  
  public static void blockEsp(BlockPos blockPos, Color c, double length, double length2) {
    double x = blockPos.getX() - mc.renderManager.renderPosX;
    double y = blockPos.getY() - mc.renderManager.renderPosY;
    double z = blockPos.getZ() - mc.renderManager.renderPosZ;
    GL11.glPushMatrix();
    GL11.glBlendFunc(770, 771);
    GL11.glEnable(3042);
    GL11.glLineWidth(2.0F);
    GL11.glDisable(3553);
    GL11.glDisable(2929);
    GL11.glDepthMask(false);
    GL11.glColor4d((c.getRed() / 255.0F), (c.getGreen() / 255.0F), (c.getBlue() / 255.0F), 0.25D);
    drawColorBox(new AxisAlignedBB(x, y, z, x + length2, y + 1.0D, z + length), 0.0F, 0.0F, 0.0F, 0.0F);
    GL11.glColor4d(0.0D, 0.0D, 0.0D, 0.5D);
    drawSelectionBoundingBox(new AxisAlignedBB(x, y, z, x + length2, y + 1.0D, z + length));
    GL11.glLineWidth(2.0F);
    GL11.glEnable(3553);
    GL11.glEnable(2929);
    GL11.glDepthMask(true);
    GL11.glDisable(3042);
    GL11.glPopMatrix();
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
  }
  
  public static void drawRect(float x, float y, float w, float h, int color) {
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    float red = (color >> 16 & 0xFF) / 255.0F;
    float green = (color >> 8 & 0xFF) / 255.0F;
    float blue = (color & 0xFF) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(x, h, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(w, h, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(w, y, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
  }
  
  public static void drawColorBox(AxisAlignedBB axisalignedbb, float red, float green, float blue, float alpha) {
    Tessellator ts = Tessellator.getInstance();
    BufferBuilder vb = ts.getBuffer();
    vb.begin(7, DefaultVertexFormats.POSITION_TEX);
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    ts.draw();
    vb.begin(7, DefaultVertexFormats.POSITION_TEX);
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    ts.draw();
    vb.begin(7, DefaultVertexFormats.POSITION_TEX);
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    ts.draw();
    vb.begin(7, DefaultVertexFormats.POSITION_TEX);
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    ts.draw();
    vb.begin(7, DefaultVertexFormats.POSITION_TEX);
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    ts.draw();
    vb.begin(7, DefaultVertexFormats.POSITION_TEX);
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
    ts.draw();
  }
  
  public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder vertexbuffer = tessellator.getBuffer();
    vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
    vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
    vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
    vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
    vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
    vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
    tessellator.draw();
    vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
    vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
    vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
    vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
    vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
    vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
    tessellator.draw();
    vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
    vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
    vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
    vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
    vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
    vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
    vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
    vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
    vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
    tessellator.draw();
  }
  
  public static void glrendermethod() {
    GL11.glEnable(3042);
    GL11.glBlendFunc(770, 771);
    GL11.glEnable(2848);
    GL11.glLineWidth(2.0F);
    GL11.glDisable(3553);
    GL11.glEnable(2884);
    GL11.glDisable(2929);
    double viewerPosX = (mc.getRenderManager()).viewerPosX;
    double viewerPosY = (mc.getRenderManager()).viewerPosY;
    double viewerPosZ = (mc.getRenderManager()).viewerPosZ;
    GL11.glPushMatrix();
    GL11.glTranslated(-viewerPosX, -viewerPosY, -viewerPosZ);
  }
  
  public static void glStart(float n, float n2, float n3, float n4) {
    glrendermethod();
    GL11.glColor4f(n, n2, n3, n4);
  }
  
  public static void glEnd() {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glPopMatrix();
    GL11.glEnable(2929);
    GL11.glEnable(3553);
    GL11.glDisable(3042);
    GL11.glDisable(2848);
  }
  
  public static AxisAlignedBB getBoundingBox(BlockPos blockPos) {
    return mc.world.getBlockState(blockPos).getBoundingBox((IBlockAccess)mc.world, blockPos).offset(blockPos);
  }
  
  public static void drawOutlinedBox(AxisAlignedBB axisAlignedBB) {
    GL11.glBegin(1);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
    GL11.glEnd();
  }
  
  public static void drawFilledBoxESPN(BlockPos pos, Color color) {
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (mc.getRenderManager()).viewerPosX, pos.getY() - (mc.getRenderManager()).viewerPosY, pos.getZ() - (mc.getRenderManager()).viewerPosZ, (pos.getX() + 1) - (mc.getRenderManager()).viewerPosX, (pos.getY() + 1) - (mc.getRenderManager()).viewerPosY, (pos.getZ() + 1) - (mc.getRenderManager()).viewerPosZ);
    int rgba = ColorUtil.toRGBA(color);
    drawFilledBox(bb, rgba);
  }
  
  public static void drawFilledBox(AxisAlignedBB bb, int color) {
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    float red = (color >> 16 & 0xFF) / 255.0F;
    float green = (color >> 8 & 0xFF) / 255.0F;
    float blue = (color & 0xFF) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void drawBoundingBox(AxisAlignedBB bb, float width, int color) {
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    GL11.glLineWidth(width);
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    float red = (color >> 16 & 0xFF) / 255.0F;
    float green = (color >> 8 & 0xFF) / 255.0F;
    float blue = (color & 0xFF) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GL11.glDisable(2848);
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void glBillboard(float x, float y, float z) {
    float scale = 0.02666667F;
    GlStateManager.translate(x - (mc.getRenderManager()).renderPosX, y - (mc.getRenderManager()).renderPosY, z - (mc.getRenderManager()).renderPosZ);
    GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(-mc.player.rotationYaw, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(mc.player.rotationPitch, (mc.gameSettings.thirdPersonView == 2) ? -1.0F : 1.0F, 0.0F, 0.0F);
    GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F);
  }
  
  public static void glBillboardDistanceScaled(float x, float y, float z, EntityPlayer player, float scale) {
    glBillboard(x, y, z);
    int distance = (int)player.getDistance(x, y, z);
    float scaleDistance = distance / 2.0F / (2.0F + 2.0F - scale);
    if (scaleDistance < 1.0F)
      scaleDistance = 1.0F; 
    GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
  }
  
  public static void drawColoredBoundingBox(AxisAlignedBB bb, float width, float red, float green, float blue, float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    GL11.glEnable(2848);
    GL11.glHint(3154, 4354);
    GL11.glLineWidth(width);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0F).endVertex();
    bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0F).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
    tessellator.draw();
    GL11.glDisable(2848);
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
  }
  
  public static void drawSphere(double x, double y, double z, float size, int slices, int stacks) {
    Sphere s = new Sphere();
    GL11.glPushMatrix();
    GL11.glBlendFunc(770, 771);
    GL11.glEnable(3042);
    GL11.glLineWidth(1.2F);
    GL11.glDisable(3553);
    GL11.glDisable(2929);
    GL11.glDepthMask(false);
    s.setDrawStyle(100013);
    GL11.glTranslated(x - mc.renderManager.renderPosX, y - mc.renderManager.renderPosY, z - mc.renderManager.renderPosZ);
    s.draw(size, slices, stacks);
    GL11.glLineWidth(2.0F);
    GL11.glEnable(3553);
    GL11.glEnable(2929);
    GL11.glDepthMask(true);
    GL11.glDisable(3042);
    GL11.glPopMatrix();
  }
  
  public static void drawCompleteImage(float posX, float posY, float width, float height) {
    GL11.glPushMatrix();
    GL11.glTranslatef(posX, posY, 0.0F);
    GL11.glBegin(7);
    GL11.glTexCoord2f(0.0F, 0.0F);
    GL11.glVertex3f(0.0F, 0.0F, 0.0F);
    GL11.glTexCoord2f(0.0F, 1.0F);
    GL11.glVertex3f(0.0F, height, 0.0F);
    GL11.glTexCoord2f(1.0F, 1.0F);
    GL11.glVertex3f(width, height, 0.0F);
    GL11.glTexCoord2f(1.0F, 0.0F);
    GL11.glVertex3f(width, 0.0F, 0.0F);
    GL11.glEnd();
    GL11.glPopMatrix();
  }
  
  public static void drawOutlineRect(float x, float y, float w, float h, int color) {
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    float red = (color >> 16 & 0xFF) / 255.0F;
    float green = (color >> 8 & 0xFF) / 255.0F;
    float blue = (color & 0xFF) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.glLineWidth(1.0F);
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    bufferbuilder.begin(2, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(x, h, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(w, h, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(w, y, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
  }
  
  public static void draw3DRect(float x, float y, float w, float h, Color startColor, Color endColor, float lineWidth) {
    float alpha = startColor.getAlpha() / 255.0F;
    float red = startColor.getRed() / 255.0F;
    float green = startColor.getGreen() / 255.0F;
    float blue = startColor.getBlue() / 255.0F;
    float alpha2 = endColor.getAlpha() / 255.0F;
    float red2 = endColor.getRed() / 255.0F;
    float green2 = endColor.getGreen() / 255.0F;
    float blue2 = endColor.getBlue() / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.glLineWidth(lineWidth);
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(x, h, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(w, h, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(w, y, 0.0D).color(red, green, blue, alpha).endVertex();
    bufferbuilder.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
    tessellator.draw();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
  }
  
  public static void drawClock(float x, float y, float radius, int slices, int loops, float lineWidth, boolean fill, Color color) {
    Disk disk = new Disk();
    Date date = new Date();
    int hourAngle = 180 + -(Calendar.getInstance().get(10) * 30 + Calendar.getInstance().get(12) / 2);
    int minuteAngle = 180 + -(Calendar.getInstance().get(12) * 6 + Calendar.getInstance().get(13) / 10);
    int secondAngle = 180 + -(Calendar.getInstance().get(13) * 6);
    int totalMinutesTime = Calendar.getInstance().get(12);
    int totalHoursTime = Calendar.getInstance().get(10);
    if (fill) {
      GL11.glPushMatrix();
      GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(3042);
      GL11.glLineWidth(lineWidth);
      GL11.glDisable(3553);
      disk.setOrientation(100020);
      disk.setDrawStyle(100012);
      GL11.glTranslated(x, y, 0.0D);
      disk.draw(0.0F, radius, slices, loops);
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glPopMatrix();
    } else {
      GL11.glPushMatrix();
      GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
      GL11.glEnable(3042);
      GL11.glLineWidth(lineWidth);
      GL11.glDisable(3553);
      GL11.glBegin(3);
      ArrayList<Vec2f> hVectors = new ArrayList<>();
      float hue = (float)(System.currentTimeMillis() % 7200L) / 7200.0F;
      for (int i = 0; i <= 360; i++) {
        Vec2f vec = new Vec2f(x + (float)Math.sin(i * Math.PI / 180.0D) * radius, y + (float)Math.cos(i * Math.PI / 180.0D) * radius);
        hVectors.add(vec);
      } 
      Color color2 = new Color(Color.HSBtoRGB(hue, 1.0F, 1.0F));
      for (int j = 0; j < hVectors.size() - 1; j++) {
        GL11.glColor4f(color2.getRed() / 255.0F, color2.getGreen() / 255.0F, color2.getBlue() / 255.0F, color2.getAlpha() / 255.0F);
        GL11.glVertex3d(((Vec2f)hVectors.get(j)).x, ((Vec2f)hVectors.get(j)).y, 0.0D);
        GL11.glVertex3d(((Vec2f)hVectors.get(j + 1)).x, ((Vec2f)hVectors.get(j + 1)).y, 0.0D);
        color2 = new Color(Color.HSBtoRGB(hue += 0.0027777778F, 1.0F, 1.0F));
      } 
      GL11.glEnd();
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glPopMatrix();
    } 
    drawLine(x, y, x + (float)Math.sin(hourAngle * Math.PI / 180.0D) * radius / 2.0F, y + (float)Math.cos(hourAngle * Math.PI / 180.0D) * radius / 2.0F, 1.0F, Color.WHITE.getRGB());
    drawLine(x, y, x + (float)Math.sin(minuteAngle * Math.PI / 180.0D) * (radius - radius / 10.0F), y + (float)Math.cos(minuteAngle * Math.PI / 180.0D) * (radius - radius / 10.0F), 1.0F, Color.WHITE.getRGB());
    drawLine(x, y, x + (float)Math.sin(secondAngle * Math.PI / 180.0D) * (radius - radius / 10.0F), y + (float)Math.cos(secondAngle * Math.PI / 180.0D) * (radius - radius / 10.0F), 1.0F, Color.RED.getRGB());
  }
  
  public static void GLPre(float lineWidth) {
    depth = GL11.glIsEnabled(2896);
    texture = GL11.glIsEnabled(3042);
    clean = GL11.glIsEnabled(3553);
    bind = GL11.glIsEnabled(2929);
    override = GL11.glIsEnabled(2848);
    GLPre(depth, texture, clean, bind, override, lineWidth);
  }
  
  public static void GlPost() {
    GLPost(depth, texture, clean, bind, override);
  }
  
  private static void GLPre(boolean depth, boolean texture, boolean clean, boolean bind, boolean override, float lineWidth) {
    if (depth)
      GL11.glDisable(2896); 
    if (!texture)
      GL11.glEnable(3042); 
    GL11.glLineWidth(lineWidth);
    if (clean)
      GL11.glDisable(3553); 
    if (bind)
      GL11.glDisable(2929); 
    if (!override)
      GL11.glEnable(2848); 
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    GL11.glHint(3154, 4354);
    GlStateManager.depthMask(false);
  }
  
  public static float[][] getBipedRotations(ModelBiped biped) {
    float[][] rotations = new float[5][];
    float[] headRotation = { biped.bipedHead.rotateAngleX, biped.bipedHead.rotateAngleY, biped.bipedHead.rotateAngleZ };
    rotations[0] = headRotation;
    float[] rightArmRotation = { biped.bipedRightArm.rotateAngleX, biped.bipedRightArm.rotateAngleY, biped.bipedRightArm.rotateAngleZ };
    rotations[1] = rightArmRotation;
    float[] leftArmRotation = { biped.bipedLeftArm.rotateAngleX, biped.bipedLeftArm.rotateAngleY, biped.bipedLeftArm.rotateAngleZ };
    rotations[2] = leftArmRotation;
    float[] rightLegRotation = { biped.bipedRightLeg.rotateAngleX, biped.bipedRightLeg.rotateAngleY, biped.bipedRightLeg.rotateAngleZ };
    rotations[3] = rightLegRotation;
    float[] leftLegRotation = { biped.bipedLeftLeg.rotateAngleX, biped.bipedLeftLeg.rotateAngleY, biped.bipedLeftLeg.rotateAngleZ };
    rotations[4] = leftLegRotation;
    return rotations;
  }
  
  private static void GLPost(boolean depth, boolean texture, boolean clean, boolean bind, boolean override) {
    GlStateManager.depthMask(true);
    if (!override)
      GL11.glDisable(2848); 
    if (bind)
      GL11.glEnable(2929); 
    if (clean)
      GL11.glEnable(3553); 
    if (!texture)
      GL11.glDisable(3042); 
    if (depth)
      GL11.glEnable(2896); 
  }
  
  public static void drawArc(float cx, float cy, float r, float start_angle, float end_angle, int num_segments) {
    GL11.glBegin(4);
    for (int i = (int)(num_segments / 360.0F / start_angle) + 1; i <= num_segments / 360.0F / end_angle; i++) {
      double previousangle = 6.283185307179586D * (i - 1) / num_segments;
      double angle = 6.283185307179586D * i / num_segments;
      GL11.glVertex2d(cx, cy);
      GL11.glVertex2d(cx + Math.cos(angle) * r, cy + Math.sin(angle) * r);
      GL11.glVertex2d(cx + Math.cos(previousangle) * r, cy + Math.sin(previousangle) * r);
    } 
    glEnd();
  }
  
  public static void drawArcOutline(float cx, float cy, float r, float start_angle, float end_angle, int num_segments) {
    GL11.glBegin(2);
    for (int i = (int)(num_segments / 360.0F / start_angle) + 1; i <= num_segments / 360.0F / end_angle; i++) {
      double angle = 6.283185307179586D * i / num_segments;
      GL11.glVertex2d(cx + Math.cos(angle) * r, cy + Math.sin(angle) * r);
    } 
    glEnd();
  }
  
  public static void drawCircleOutline(float x, float y, float radius) {
    drawCircleOutline(x, y, radius, 0, 360, 40);
  }
  
  public static void drawCircleOutline(float x, float y, float radius, int start, int end, int segments) {
    drawArcOutline(x, y, radius, start, end, segments);
  }
  
  public static void drawCircle(float x, float y, float radius) {
    drawCircle(x, y, radius, 0, 360, 64);
  }
  
  public static void drawCircle(float x, float y, float radius, int start, int end, int segments) {
    drawArc(x, y, radius, start, end, segments);
  }
  
  public static void drawOutlinedRoundedRectangle(int x, int y, int width, int height, float radius, float dR, float dG, float dB, float dA, float outlineWidth) {
    drawRoundedRectangle(x, y, width, height, radius);
    GL11.glColor4f(dR, dG, dB, dA);
    drawRoundedRectangle(x + outlineWidth, y + outlineWidth, width - outlineWidth * 2.0F, height - outlineWidth * 2.0F, radius);
  }
  
  public static void drawRectangle(float x, float y, float width, float height) {
    GL11.glEnable(3042);
    GL11.glBlendFunc(770, 771);
    GL11.glBegin(2);
    GL11.glVertex2d(width, 0.0D);
    GL11.glVertex2d(0.0D, 0.0D);
    GL11.glVertex2d(0.0D, height);
    GL11.glVertex2d(width, height);
    glEnd();
  }
  
  public static void drawRectangleXY(float x, float y, float width, float height) {
    GL11.glEnable(3042);
    GL11.glBlendFunc(770, 771);
    GL11.glBegin(2);
    GL11.glVertex2d((x + width), y);
    GL11.glVertex2d(x, y);
    GL11.glVertex2d(x, (y + height));
    GL11.glVertex2d((x + width), (y + height));
    glEnd();
  }
  
  public static void drawFilledRectangle(float x, float y, float width, float height) {
    GL11.glEnable(3042);
    GL11.glBlendFunc(770, 771);
    GL11.glBegin(7);
    GL11.glVertex2d((x + width), y);
    GL11.glVertex2d(x, y);
    GL11.glVertex2d(x, (y + height));
    GL11.glVertex2d((x + width), (y + height));
    glEnd();
  }
  
  public static Vec3d to2D(double x, double y, double z) {
    GL11.glGetFloat(2982, modelView);
    GL11.glGetFloat(2983, projection);
    GL11.glGetInteger(2978, viewport);
    boolean result = GLU.gluProject((float)x, (float)y, (float)z, modelView, projection, viewport, screenCoords);
    if (result)
      return new Vec3d(screenCoords.get(0), (Display.getHeight() - screenCoords.get(1)), screenCoords.get(2)); 
    return null;
  }
  
  public static void drawTracerPointer(float x, float y, float size, float widthDiv, float heightDiv, boolean outline, float outlineWidth, int color) {
    boolean blend = GL11.glIsEnabled(3042);
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    GL11.glEnable(3042);
    GL11.glDisable(3553);
    GL11.glBlendFunc(770, 771);
    GL11.glEnable(2848);
    GL11.glPushMatrix();
    hexColor(color);
    GL11.glBegin(7);
    GL11.glVertex2d(x, y);
    GL11.glVertex2d((x - size / widthDiv), (y + size));
    GL11.glVertex2d(x, (y + size / heightDiv));
    GL11.glVertex2d((x + size / widthDiv), (y + size));
    GL11.glVertex2d(x, y);
    GL11.glEnd();
    if (outline) {
      GL11.glLineWidth(outlineWidth);
      GL11.glColor4f(0.0F, 0.0F, 0.0F, alpha);
      GL11.glBegin(2);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d((x - size / widthDiv), (y + size));
      GL11.glVertex2d(x, (y + size / heightDiv));
      GL11.glVertex2d((x + size / widthDiv), (y + size));
      GL11.glVertex2d(x, y);
      GL11.glEnd();
    } 
    GL11.glPopMatrix();
    GL11.glEnable(3553);
    if (!blend)
      GL11.glDisable(3042); 
    GL11.glDisable(2848);
  }
  
  public static int getRainbow(int speed, int offset, float s, float b) {
    float hue = (float)((System.currentTimeMillis() + offset) % speed);
    return Color.getHSBColor(hue /= speed, s, b).getRGB();
  }
  
  public static void hexColor(int hexColor) {
    float red = (hexColor >> 16 & 0xFF) / 255.0F;
    float green = (hexColor >> 8 & 0xFF) / 255.0F;
    float blue = (hexColor & 0xFF) / 255.0F;
    float alpha = (hexColor >> 24 & 0xFF) / 255.0F;
    GL11.glColor4f(red, green, blue, alpha);
  }
  
  public static boolean isInViewFrustrum(Entity entity) {
    return (isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck);
  }
  
  public static boolean isInViewFrustrum(AxisAlignedBB bb) {
    Entity current = Minecraft.getMinecraft().getRenderViewEntity();
    frustrum.setPosition(current.posX, current.posY, current.posZ);
    return frustrum.isBoundingBoxInFrustum(bb);
  }
  
  public static void drawRoundedRectangle(float x, float y, float width, float height, float radius) {
    GL11.glEnable(3042);
    drawArc(x + width - radius, y + height - radius, radius, 0.0F, 90.0F, 16);
    drawArc(x + radius, y + height - radius, radius, 90.0F, 180.0F, 16);
    drawArc(x + radius, y + radius, radius, 180.0F, 270.0F, 16);
    drawArc(x + width - radius, y + radius, radius, 270.0F, 360.0F, 16);
    GL11.glBegin(4);
    GL11.glVertex2d((x + width - radius), y);
    GL11.glVertex2d((x + radius), y);
    GL11.glVertex2d((x + width - radius), (y + radius));
    GL11.glVertex2d((x + width - radius), (y + radius));
    GL11.glVertex2d((x + radius), y);
    GL11.glVertex2d((x + radius), (y + radius));
    GL11.glVertex2d((x + width), (y + radius));
    GL11.glVertex2d(x, (y + radius));
    GL11.glVertex2d(x, (y + height - radius));
    GL11.glVertex2d((x + width), (y + radius));
    GL11.glVertex2d(x, (y + height - radius));
    GL11.glVertex2d((x + width), (y + height - radius));
    GL11.glVertex2d((x + width - radius), (y + height - radius));
    GL11.glVertex2d((x + radius), (y + height - radius));
    GL11.glVertex2d((x + width - radius), (y + height));
    GL11.glVertex2d((x + width - radius), (y + height));
    GL11.glVertex2d((x + radius), (y + height - radius));
    GL11.glVertex2d((x + radius), (y + height));
    glEnd();
  }
  
  public static void renderOne(float lineWidth) {
    checkSetupFBO();
    GL11.glPushAttrib(1048575);
    GL11.glDisable(3008);
    GL11.glDisable(3553);
    GL11.glDisable(2896);
    GL11.glEnable(3042);
    GL11.glBlendFunc(770, 771);
    GL11.glLineWidth(lineWidth);
    GL11.glEnable(2848);
    GL11.glEnable(2960);
    GL11.glClear(1024);
    GL11.glClearStencil(15);
    GL11.glStencilFunc(512, 1, 15);
    GL11.glStencilOp(7681, 7681, 7681);
    GL11.glPolygonMode(1032, 6913);
  }
  
  public static void renderTwo() {
    GL11.glStencilFunc(512, 0, 15);
    GL11.glStencilOp(7681, 7681, 7681);
    GL11.glPolygonMode(1032, 6914);
  }
  
  public static void renderThree() {
    GL11.glStencilFunc(514, 1, 15);
    GL11.glStencilOp(7680, 7680, 7680);
    GL11.glPolygonMode(1032, 6913);
  }
  
  public static void renderFour(Color color) {
    setColor(color);
    GL11.glDepthMask(false);
    GL11.glDisable(2929);
    GL11.glEnable(10754);
    GL11.glPolygonOffset(1.0F, -2000000.0F);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
  }
  
  public static void renderFive() {
    GL11.glPolygonOffset(1.0F, 2000000.0F);
    GL11.glDisable(10754);
    GL11.glEnable(2929);
    GL11.glDepthMask(true);
    GL11.glDisable(2960);
    GL11.glDisable(2848);
    GL11.glHint(3154, 4352);
    GL11.glEnable(3042);
    GL11.glEnable(2896);
    GL11.glEnable(3553);
    GL11.glEnable(3008);
    GL11.glPopAttrib();
  }
  
  public static void setColor(Color color) {
    GL11.glColor4d(color.getRed() / 255.0D, color.getGreen() / 255.0D, color.getBlue() / 255.0D, color.getAlpha() / 255.0D);
  }
  
  public static void checkSetupFBO() {
    Framebuffer fbo = mc.framebuffer;
    if (fbo != null && fbo.depthBuffer > -1) {
      setupFBO(fbo);
      fbo.depthBuffer = -1;
    } 
  }
  
  private static void setupFBO(Framebuffer fbo) {
    EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
    int stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
    EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilDepthBufferID);
    EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, mc.displayWidth, mc.displayHeight);
    EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilDepthBufferID);
    EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilDepthBufferID);
  }
  
  public static RenderItem itemRender = mc.getRenderItem();
  
  public static ICamera camera = (ICamera)new Frustum();
  
  private static final Frustum frustrum = new Frustum();
  
  private static boolean depth = GL11.glIsEnabled(2896);
  
  private static boolean texture = GL11.glIsEnabled(3042);
  
  private static boolean clean = GL11.glIsEnabled(3553);
  
  private static boolean bind = GL11.glIsEnabled(2929);
  
  private static boolean override = GL11.glIsEnabled(2848);
  
  private static final FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
  
  private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
  
  private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
  
  private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
}
