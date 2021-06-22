package me.alpha432.oyvey.features.modules.render;

import java.awt.Color;
import java.util.Objects;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.DamageUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.RotationUtil;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

public class TestNameTags extends Module {
  private final Setting<Boolean> health = register(new Setting("Health", Boolean.valueOf(true)));
  
  private final Setting<Boolean> armor = register(new Setting("Armor", Boolean.valueOf(true)));
  
  private final Setting<Float> scaling = register(new Setting("Size", Float.valueOf(0.3F), Float.valueOf(0.1F), Float.valueOf(20.0F)));
  
  private final Setting<Boolean> invisibles = register(new Setting("Invisibles", Boolean.valueOf(false)));
  
  private final Setting<Boolean> ping = register(new Setting("Ping", Boolean.valueOf(true)));
  
  private final Setting<Boolean> totemPops = register(new Setting("TotemPops", Boolean.valueOf(true)));
  
  private final Setting<Boolean> gamemode = register(new Setting("Gamemode", Boolean.valueOf(false)));
  
  private final Setting<Boolean> entityID = register(new Setting("ID", Boolean.valueOf(false)));
  
  private final Setting<Boolean> rect = register(new Setting("Rectangle", Boolean.valueOf(true)));
  
  private final Setting<Boolean> outline = register(new Setting("Outline", Boolean.valueOf(false), v -> ((Boolean)this.rect.getValue()).booleanValue()));
  
  private final Setting<Integer> redSetting = register(new Setting("Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Integer> greenSetting = register(new Setting("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Integer> blueSetting = register(new Setting("Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Integer> alphaSetting = register(new Setting("Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Float> lineWidth = register(new Setting("LineWidth", Float.valueOf(1.5F), Float.valueOf(0.1F), Float.valueOf(5.0F), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Boolean> sneak = register(new Setting("SneakColor", Boolean.valueOf(false)));
  
  private final Setting<Boolean> heldStackName = register(new Setting("StackName", Boolean.valueOf(false)));
  
  private final Setting<Boolean> whiter = register(new Setting("White", Boolean.valueOf(false)));
  
  private final Setting<Boolean> onlyFov = register(new Setting("OnlyFov", Boolean.valueOf(false)));
  
  private final Setting<Boolean> scaleing = register(new Setting("Scale", Boolean.valueOf(false)));
  
  private final Setting<Float> factor = register(new Setting("Factor", Float.valueOf(0.3F), Float.valueOf(0.1F), Float.valueOf(1.0F), v -> ((Boolean)this.scaleing.getValue()).booleanValue()));
  
  private final Setting<Boolean> smartScale = register(new Setting("SmartScale", Boolean.valueOf(false), v -> ((Boolean)this.scaleing.getValue()).booleanValue()));
  
  private static TestNameTags INSTANCE = new TestNameTags();
  
  public TestNameTags() {
    super("TestNameTags", "nigga wtf is this", Module.Category.RENDER, false, true, false);
    setInstance();
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  public static TestNameTags getInstance() {
    if (INSTANCE == null)
      INSTANCE = new TestNameTags(); 
    return INSTANCE;
  }
  
  public void onRender3D(Render3DEvent event) {
    if (!fullNullCheck())
      for (EntityPlayer player : mc.world.playerEntities) {
        if (player == null || player.equals(mc.player) || !player.isEntityAlive() || (player.isInvisible() && !((Boolean)this.invisibles.getValue()).booleanValue()) || (((Boolean)this.onlyFov.getValue()).booleanValue() && !RotationUtil.isInFov((Entity)player)))
          continue; 
        double x = interpolate(player.lastTickPosX, player.posX, event.getPartialTicks()) - (mc.getRenderManager()).renderPosX;
        double y = interpolate(player.lastTickPosY, player.posY, event.getPartialTicks()) - (mc.getRenderManager()).renderPosY;
        double z = interpolate(player.lastTickPosZ, player.posZ, event.getPartialTicks()) - (mc.getRenderManager()).renderPosZ;
        renderProperNameTag(player, x, y, z, event.getPartialTicks());
      }  
  }
  
  private void renderProperNameTag(EntityPlayer player, double x, double y, double z, float delta) {
    double tempY = y;
    tempY += player.isSneaking() ? 0.5D : 0.7D;
    Entity camera = mc.getRenderViewEntity();
    assert camera != null;
    double originalPositionX = camera.posX;
    double originalPositionY = camera.posY;
    double originalPositionZ = camera.posZ;
    camera.posX = interpolate(camera.prevPosX, camera.posX, delta);
    camera.posY = interpolate(camera.prevPosY, camera.posY, delta);
    camera.posZ = interpolate(camera.prevPosZ, camera.posZ, delta);
    String displayTag = getDisplayTag(player);
    double distance = camera.getDistance(x + (mc.getRenderManager()).viewerPosX, y + (mc.getRenderManager()).viewerPosY, z + (mc.getRenderManager()).viewerPosZ);
    int width = this.renderer.getStringWidth(displayTag) / 2;
    double scale = (0.0018D + ((Float)this.scaling.getValue()).floatValue() * distance * ((Float)this.factor.getValue()).floatValue()) / 1000.0D;
    if (distance <= 8.0D && ((Boolean)this.smartScale.getValue()).booleanValue())
      scale = 0.0245D; 
    if (!((Boolean)this.scaleing.getValue()).booleanValue())
      scale = ((Float)this.scaling.getValue()).floatValue() / 100.0D; 
    GlStateManager.pushMatrix();
    RenderHelper.enableStandardItemLighting();
    GlStateManager.enablePolygonOffset();
    GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
    GlStateManager.disableLighting();
    GlStateManager.translate((float)x, (float)tempY + 1.4F, (float)z);
    GlStateManager.rotate(-(mc.getRenderManager()).playerViewY, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate((mc.getRenderManager()).playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1.0F : 1.0F, 0.0F, 0.0F);
    GlStateManager.scale(-scale, -scale, scale);
    GlStateManager.disableDepth();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    if (((Boolean)this.rect.getValue()).booleanValue()) {
      drawRect((-width - 2), -(mc.fontRenderer.FONT_HEIGHT + 1), width + 2.0F, 1.5F, 1426063360);
      if (((Boolean)this.outline.getValue()).booleanValue()) {
        int color = (new Color(((Integer)this.redSetting.getValue()).intValue(), ((Integer)this.greenSetting.getValue()).intValue(), ((Integer)this.blueSetting.getValue()).intValue(), ((Integer)this.alphaSetting.getValue()).intValue())).getRGB();
        drawOutlineRect((-width - 2), -(mc.fontRenderer.FONT_HEIGHT + 1), width + 2.0F, 1.5F, color);
      } 
    } 
    GlStateManager.enableAlpha();
    ItemStack renderMainHand = player.getHeldItemMainhand().copy();
    if (renderMainHand.hasEffect() && (renderMainHand.getItem() instanceof net.minecraft.item.ItemTool || renderMainHand.getItem() instanceof net.minecraft.item.ItemArmor))
      renderMainHand.stackSize = 1; 
    if (((Boolean)this.heldStackName.getValue()).booleanValue() && !renderMainHand.isEmpty && renderMainHand.getItem() != Items.AIR) {
      String stackName = renderMainHand.getDisplayName();
      int stackNameWidth = this.renderer.getStringWidth(stackName) / 2;
      GL11.glPushMatrix();
      GL11.glScalef(0.75F, 0.75F, 0.0F);
      this.renderer.drawStringWithShadow(stackName, -stackNameWidth, -(getBiggestArmorTag(player) + 20.0F), -1);
      GL11.glScalef(1.5F, 1.5F, 1.0F);
      GL11.glPopMatrix();
    } 
    GlStateManager.pushMatrix();
    int xOffset = -8;
    for (ItemStack stack : player.inventory.armorInventory) {
      if (stack == null)
        continue; 
      xOffset -= 8;
    } 
    xOffset -= 8;
    ItemStack renderOffhand = player.getHeldItemOffhand().copy();
    if (renderOffhand.hasEffect() && (renderOffhand.getItem() instanceof net.minecraft.item.ItemTool || renderOffhand.getItem() instanceof net.minecraft.item.ItemArmor))
      renderOffhand.stackSize = 1; 
    renderItemStack(renderOffhand, xOffset, -26, ((Boolean)this.armor.getValue()).booleanValue());
    xOffset += 16;
    for (ItemStack stack : player.inventory.armorInventory) {
      if (stack == null)
        continue; 
      ItemStack armourStack = stack.copy();
      if (armourStack.hasEffect() && (armourStack.getItem() instanceof net.minecraft.item.ItemTool || armourStack.getItem() instanceof net.minecraft.item.ItemArmor))
        armourStack.stackSize = 1; 
      renderItemStack(armourStack, xOffset, -26, ((Boolean)this.armor.getValue()).booleanValue());
      xOffset += 16;
    } 
    renderItemStack(renderMainHand, xOffset, -26, ((Boolean)this.armor.getValue()).booleanValue());
    GlStateManager.popMatrix();
    this.renderer.drawStringWithShadow(displayTag, -width, -(this.renderer.getFontHeight() - 1), getDisplayColour(player));
    camera.posX = originalPositionX;
    camera.posY = originalPositionY;
    camera.posZ = originalPositionZ;
    GlStateManager.enableDepth();
    GlStateManager.enableLighting();
    GlStateManager.disableBlend();
    GlStateManager.enableLighting();
    GlStateManager.disablePolygonOffset();
    GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
    GlStateManager.popMatrix();
  }
  
  private void renderNameTag(EntityPlayer player, double x, double y, double z, float partialTicks) {
    double tempY = y + (player.isSneaking() ? 0.5D : 0.7D);
    Entity camera = mc.getRenderViewEntity();
    assert camera != null;
    double originalPositionX = camera.posX;
    double originalPositionY = camera.posY;
    double originalPositionZ = camera.posZ;
    camera.posX = interpolate(camera.prevPosX, camera.posX, partialTicks);
    camera.posY = interpolate(camera.prevPosY, camera.posY, partialTicks);
    camera.posZ = interpolate(camera.prevPosZ, camera.posZ, partialTicks);
    double distance = camera.getDistance(x + (mc.getRenderManager()).viewerPosX, y + (mc.getRenderManager()).viewerPosY, z + (mc.getRenderManager()).viewerPosZ);
    int width = mc.fontRenderer.getStringWidth(getDisplayTag(player)) / 2;
    double scale = (0.0018D + ((Float)this.scaling.getValue()).floatValue() * distance) / 50.0D;
    GlStateManager.pushMatrix();
    RenderHelper.enableStandardItemLighting();
    GlStateManager.enablePolygonOffset();
    GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
    GlStateManager.disableLighting();
    GlStateManager.translate((float)x, (float)tempY + 1.4F, (float)z);
    GlStateManager.rotate(-(mc.getRenderManager()).playerViewY, 0.0F, 1.0F, 0.0F);
    float thirdPersonOffset = (mc.gameSettings.thirdPersonView == 2) ? -1.0F : 1.0F;
    GlStateManager.rotate((mc.getRenderManager()).playerViewX, thirdPersonOffset, 0.0F, 0.0F);
    GlStateManager.scale(-scale, -scale, scale);
    GlStateManager.disableDepth();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    drawRect((-width - 2), -(mc.fontRenderer.FONT_HEIGHT + 1), width + 2.0F, 1.5F, 1426063360);
    GlStateManager.enableAlpha();
    mc.fontRenderer.drawStringWithShadow(getDisplayTag(player), -width, -(mc.fontRenderer.FONT_HEIGHT - 1), getNameColor((Entity)player).getRGB());
    if (((Boolean)this.armor.getValue()).booleanValue()) {
      GlStateManager.pushMatrix();
      double changeValue = 16.0D;
      int xOffset = 0;
      xOffset = (int)(xOffset - changeValue / 2.0D * player.inventory.armorInventory.size());
      xOffset = (int)(xOffset - changeValue / 2.0D);
      xOffset = (int)(xOffset - changeValue / 2.0D);
      if (!player.getHeldItemMainhand().isEmpty());
      xOffset = (int)(xOffset + changeValue);
      for (ItemStack stack : player.inventory.armorInventory) {
        if (!stack.isEmpty());
        xOffset = (int)(xOffset + changeValue);
      } 
      if (!player.getHeldItemOffhand().isEmpty());
      GlStateManager.popMatrix();
    } 
    camera.posX = originalPositionX;
    camera.posY = originalPositionY;
    camera.posZ = originalPositionZ;
    GlStateManager.enableDepth();
    GlStateManager.enableLighting();
    GlStateManager.disableBlend();
    GlStateManager.enableLighting();
    GlStateManager.disablePolygonOffset();
    GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
    GlStateManager.popMatrix();
  }
  
  public void drawRect(float x, float y, float w, float h, int color) {
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    float red = (color >> 16 & 0xFF) / 255.0F;
    float green = (color >> 8 & 0xFF) / 255.0F;
    float blue = (color & 0xFF) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.glLineWidth(((Float)this.lineWidth.getValue()).floatValue());
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
  
  public void drawOutlineRect(float x, float y, float w, float h, int color) {
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    float red = (color >> 16 & 0xFF) / 255.0F;
    float green = (color >> 8 & 0xFF) / 255.0F;
    float blue = (color & 0xFF) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.glLineWidth(((Float)this.lineWidth.getValue()).floatValue());
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
  
  private Color getNameColor(Entity entity) {
    return Color.WHITE;
  }
  
  private void renderItemStack(ItemStack stack, int x, int y, boolean item) {
    GlStateManager.pushMatrix();
    GlStateManager.depthMask(true);
    GlStateManager.clear(256);
    RenderHelper.enableStandardItemLighting();
    (mc.getRenderItem()).zLevel = -150.0F;
    GlStateManager.disableAlpha();
    GlStateManager.enableDepth();
    GlStateManager.disableCull();
    if (item) {
      mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
      mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, y);
    } 
    (mc.getRenderItem()).zLevel = 0.0F;
    RenderHelper.disableStandardItemLighting();
    GlStateManager.enableCull();
    GlStateManager.enableAlpha();
    GlStateManager.scale(0.5F, 0.5F, 0.5F);
    GlStateManager.disableDepth();
    renderEnchantmentText(stack, x, y);
    GlStateManager.enableDepth();
    GlStateManager.scale(2.0F, 2.0F, 2.0F);
    GlStateManager.popMatrix();
  }
  
  private void renderEnchantmentText(ItemStack stack, int x, int y) {
    int enchantmentY = y - 8;
    if (stack.getItem() == Items.GOLDEN_APPLE && stack.hasEffect()) {
      this.renderer.drawStringWithShadow("god", (x * 2), enchantmentY, -3977919);
      enchantmentY -= 8;
    } 
    NBTTagList enchants = stack.getEnchantmentTagList();
    for (int index = 0; index < enchants.tagCount(); index++) {
      short id = enchants.getCompoundTagAt(index).getShort("id");
      short level = enchants.getCompoundTagAt(index).getShort("lvl");
      Enchantment enc = Enchantment.getEnchantmentByID(id);
      if (enc != null) {
        String encName = enc.isCurse() ? (TextFormatting.RED + enc.getTranslatedName(level).substring(11).substring(0, 1).toLowerCase()) : enc.getTranslatedName(level).substring(0, 1).toLowerCase();
        encName = encName + level;
        this.renderer.drawStringWithShadow(encName, (x * 2), enchantmentY, -1);
        enchantmentY -= 8;
      } 
    } 
    if (DamageUtil.hasDurability(stack)) {
      int percent = DamageUtil.getRoundedDamage(stack);
      String color = (percent >= 60) ? "§a" : ((percent >= 25) ? "§e" : "§c");
      this.renderer.drawStringWithShadow(color + percent + "%", (x * 2), enchantmentY, -1);
    } 
  }
  
  private float getBiggestArmorTag(EntityPlayer player) {
    float enchantmentY = 0.0F;
    boolean arm = false;
    for (ItemStack stack : player.inventory.armorInventory) {
      float encY = 0.0F;
      if (stack != null) {
        NBTTagList enchants = stack.getEnchantmentTagList();
        for (int index = 0; index < enchants.tagCount(); index++) {
          short id = enchants.getCompoundTagAt(index).getShort("id");
          Enchantment enc = Enchantment.getEnchantmentByID(id);
          if (enc != null) {
            encY += 8.0F;
            arm = true;
          } 
        } 
      } 
      if (encY <= enchantmentY)
        continue; 
      enchantmentY = encY;
    } 
    ItemStack renderMainHand = player.getHeldItemMainhand().copy();
    if (renderMainHand.hasEffect()) {
      float encY = 0.0F;
      NBTTagList enchants = renderMainHand.getEnchantmentTagList();
      for (int index2 = 0; index2 < enchants.tagCount(); index2++) {
        short id = enchants.getCompoundTagAt(index2).getShort("id");
        Enchantment enc2 = Enchantment.getEnchantmentByID(id);
        if (enc2 != null) {
          encY += 8.0F;
          arm = true;
        } 
      } 
      if (encY > enchantmentY)
        enchantmentY = encY; 
    } 
    ItemStack renderOffHand;
    if ((renderOffHand = player.getHeldItemOffhand().copy()).hasEffect()) {
      float encY = 0.0F;
      NBTTagList enchants = renderOffHand.getEnchantmentTagList();
      for (int index = 0; index < enchants.tagCount(); index++) {
        short id = enchants.getCompoundTagAt(index).getShort("id");
        Enchantment enc = Enchantment.getEnchantmentByID(id);
        if (enc != null) {
          encY += 8.0F;
          arm = true;
        } 
      } 
      if (encY > enchantmentY)
        enchantmentY = encY; 
    } 
    return (arm ? false : 20) + enchantmentY;
  }
  
  private String getDisplayTag(EntityPlayer player) {
    String name = player.getDisplayName().getFormattedText();
    if (name.contains(mc.getSession().getUsername()))
      name = "You"; 
    if (!((Boolean)this.health.getValue()).booleanValue())
      return name; 
    float health = EntityUtil.getHealth((Entity)player);
    String color = (health > 18.0F) ? "§a" : ((health > 16.0F) ? "§2" : ((health > 12.0F) ? "§e" : ((health > 8.0F) ? "§6" : ((health > 5.0F) ? "§c" : "§4"))));
    String pingStr = "";
    if (((Boolean)this.ping.getValue()).booleanValue())
      try {
        int responseTime = ((NetHandlerPlayClient)Objects.<NetHandlerPlayClient>requireNonNull(mc.getConnection())).getPlayerInfo(player.getUniqueID()).getResponseTime();
        pingStr = pingStr + responseTime + "ms ";
      } catch (Exception exception) {} 
    String idString = "";
    if (((Boolean)this.entityID.getValue()).booleanValue())
      idString = idString + "ID: " + player.getEntityId() + " "; 
    String gameModeStr = "";
    if (((Boolean)this.gamemode.getValue()).booleanValue())
      gameModeStr = player.isCreative() ? (gameModeStr + "[C] ") : ((player.isSpectator() || player.isInvisible()) ? (gameModeStr + "[I] ") : (gameModeStr + "[S] ")); 
    name = (Math.floor(health) == health) ? (name + color + " " + ((health > 0.0F) ? (String)Integer.valueOf((int)Math.floor(health)) : "dead")) : (name + color + " " + ((health > 0.0F) ? (String)Integer.valueOf((int)health) : "dead"));
    return pingStr + idString + gameModeStr + name;
  }
  
  private int getDisplayColour(EntityPlayer player) {
    int colour = -5592406;
    if (((Boolean)this.whiter.getValue()).booleanValue())
      colour = -1; 
    if (OyVey.friendManager.isFriend(player))
      return -11157267; 
    if (player.isInvisible()) {
      colour = -1113785;
    } else if (player.isSneaking() && ((Boolean)this.sneak.getValue()).booleanValue()) {
      colour = -6481515;
    } 
    return colour;
  }
  
  private double interpolate(double previous, double current, float partialTicks) {
    return previous + (current - previous) * partialTicks;
  }
}
