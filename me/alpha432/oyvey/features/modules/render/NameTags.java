package me.alpha432.oyvey.features.modules.render;

import java.awt.Color;
import java.util.Objects;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorHolder;
import me.alpha432.oyvey.util.DamageUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.TextUtil;
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

public class NameTags extends Module {
  private final Setting<Boolean> rect = register(new Setting("Rectangle", Boolean.valueOf(true)));
  
  private final Setting<Boolean> armor = register(new Setting("Armor", Boolean.valueOf(true)));
  
  private final Setting<Boolean> reversed = register(new Setting("ArmorReversed", Boolean.valueOf(false), v -> ((Boolean)this.armor.getValue()).booleanValue()));
  
  private final Setting<Boolean> health = register(new Setting("Health", Boolean.valueOf(true)));
  
  private final Setting<Boolean> ping = register(new Setting("Ping", Boolean.valueOf(true)));
  
  private final Setting<Boolean> gamemode = register(new Setting("Gamemode", Boolean.valueOf(false)));
  
  private final Setting<Boolean> entityID = register(new Setting("EntityID", Boolean.valueOf(false)));
  
  private final Setting<Boolean> heldStackName = register(new Setting("StackName", Boolean.valueOf(true)));
  
  private final Setting<Boolean> max = register(new Setting("Max", Boolean.valueOf(true)));
  
  private final Setting<Boolean> maxText = register(new Setting("NoMaxText", Boolean.valueOf(false), v -> ((Boolean)this.max.getValue()).booleanValue()));
  
  private final Setting<Integer> Mred = register(new Setting("Max-Red", Integer.valueOf(178), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.max.getValue()).booleanValue()));
  
  private final Setting<Integer> Mgreen = register(new Setting("Max-Green", Integer.valueOf(52), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.max.getValue()).booleanValue()));
  
  private final Setting<Integer> Mblue = register(new Setting("Max-Blue", Integer.valueOf(57), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.max.getValue()).booleanValue()));
  
  private final Setting<Float> size = register(new Setting("Size", Float.valueOf(0.3F), Float.valueOf(0.1F), Float.valueOf(20.0F)));
  
  private final Setting<Boolean> scaleing = register(new Setting("Scale", Boolean.valueOf(false)));
  
  private final Setting<Boolean> smartScale = register(new Setting("SmartScale", Boolean.valueOf(false), v -> ((Boolean)this.scaleing.getValue()).booleanValue()));
  
  private final Setting<Float> factor = register(new Setting("Factor", Float.valueOf(0.3F), Float.valueOf(0.1F), Float.valueOf(1.0F), v -> ((Boolean)this.scaleing.getValue()).booleanValue()));
  
  private final Setting<Boolean> textcolor = register(new Setting("TextColor", Boolean.valueOf(true)));
  
  private final Setting<Boolean> NCRainbow = register(new Setting("Text-Rainbow", Boolean.valueOf(false), v -> ((Boolean)this.textcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> NCred = register(new Setting("Text-Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.textcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> NCgreen = register(new Setting("Text-Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.textcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> NCblue = register(new Setting("Text-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.textcolor.getValue()).booleanValue()));
  
  private final Setting<Boolean> outline = register(new Setting("Outline", Boolean.valueOf(true)));
  
  private final Setting<Boolean> ORainbow = register(new Setting("Outline-Rainbow", Boolean.valueOf(false), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Float> Owidth = register(new Setting("Outline-Width", Float.valueOf(1.3F), Float.valueOf(0.0F), Float.valueOf(5.0F), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Integer> Ored = register(new Setting("Outline-Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Integer> Ogreen = register(new Setting("Outline-Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Integer> Oblue = register(new Setting("Outline-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private final Setting<Boolean> friendcolor = register(new Setting("FriendColor", Boolean.valueOf(true)));
  
  private final Setting<Boolean> FCRainbow = register(new Setting("Friend-Rainbow", Boolean.valueOf(false), v -> ((Boolean)this.friendcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> FCred = register(new Setting("Friend-Red", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.friendcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> FCgreen = register(new Setting("Friend-Green", Integer.valueOf(213), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.friendcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> FCblue = register(new Setting("Friend-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.friendcolor.getValue()).booleanValue()));
  
  private final Setting<Boolean> FORainbow = register(new Setting("FriendOutline-Rainbow", Boolean.valueOf(false), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.friendcolor.getValue()).booleanValue())));
  
  private final Setting<Integer> FOred = register(new Setting("FriendOutline-Red", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.friendcolor.getValue()).booleanValue())));
  
  private final Setting<Integer> FOgreen = register(new Setting("FriendOutline-Green", Integer.valueOf(213), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.friendcolor.getValue()).booleanValue())));
  
  private final Setting<Integer> FOblue = register(new Setting("FriendOutline-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.friendcolor.getValue()).booleanValue())));
  
  private final Setting<Boolean> sneakcolor = register(new Setting("Sneak", Boolean.valueOf(false)));
  
  private final Setting<Boolean> sneak = register(new Setting("EnableSneak", Boolean.valueOf(true), v -> ((Boolean)this.sneakcolor.getValue()).booleanValue()));
  
  private final Setting<Boolean> SCRainbow = register(new Setting("Sneak-Rainbow", Boolean.valueOf(false), v -> ((Boolean)this.sneakcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> SCred = register(new Setting("Sneak-Red", Integer.valueOf(245), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.sneakcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> SCgreen = register(new Setting("Sneak-Green", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.sneakcolor.getValue()).booleanValue()));
  
  private final Setting<Integer> SCblue = register(new Setting("Sneak-Blue", Integer.valueOf(122), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.sneakcolor.getValue()).booleanValue()));
  
  private final Setting<Boolean> SORainbow = register(new Setting("SneakOutline-Rainbow", Boolean.valueOf(false), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.sneakcolor.getValue()).booleanValue())));
  
  private final Setting<Integer> SOred = register(new Setting("SneakOutline-Red", Integer.valueOf(245), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.sneakcolor.getValue()).booleanValue())));
  
  private final Setting<Integer> SOgreen = register(new Setting("SneakOutline-Green", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.sneakcolor.getValue()).booleanValue())));
  
  private final Setting<Integer> SOblue = register(new Setting("SneakOutline-Blue", Integer.valueOf(122), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.sneakcolor.getValue()).booleanValue())));
  
  private final Setting<Boolean> invisiblescolor = register(new Setting("InvisiblesColor", Boolean.valueOf(false)));
  
  private final Setting<Boolean> invisibles = register(new Setting("EnableInvisibles", Boolean.valueOf(true), v -> ((Boolean)this.invisiblescolor.getValue()).booleanValue()));
  
  private final Setting<Boolean> ICRainbow = register(new Setting("Invisible-Rainbow", Boolean.valueOf(false), v -> ((Boolean)this.invisiblescolor.getValue()).booleanValue()));
  
  private final Setting<Integer> ICred = register(new Setting("Invisible-Red", Integer.valueOf(148), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.invisiblescolor.getValue()).booleanValue()));
  
  private final Setting<Integer> ICgreen = register(new Setting("Invisible-Green", Integer.valueOf(148), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.invisiblescolor.getValue()).booleanValue()));
  
  private final Setting<Integer> ICblue = register(new Setting("Invisible-Blue", Integer.valueOf(148), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.invisiblescolor.getValue()).booleanValue()));
  
  private final Setting<Boolean> IORainbow = register(new Setting("InvisibleOutline-Rainbow", Boolean.valueOf(false), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.invisiblescolor.getValue()).booleanValue())));
  
  private final Setting<Integer> IOred = register(new Setting("InvisibleOutline-Red", Integer.valueOf(148), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.invisiblescolor.getValue()).booleanValue())));
  
  private final Setting<Integer> IOgreen = register(new Setting("InvisibleOutline-Green", Integer.valueOf(148), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.invisiblescolor.getValue()).booleanValue())));
  
  private final Setting<Integer> IOblue = register(new Setting("InvisibleOutline-Blue", Integer.valueOf(148), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.invisiblescolor.getValue()).booleanValue())));
  
  private static NameTags INSTANCE = new NameTags();
  
  public NameTags() {
    super("NameTags", "Renders info about the player on a NameTag", Module.Category.RENDER, false, true, false);
  }
  
  public static NameTags getInstance() {
    if (INSTANCE == null)
      INSTANCE = new NameTags(); 
    return INSTANCE;
  }
  
  public void onRender3D(Render3DEvent event) {
    for (EntityPlayer player : mc.world.playerEntities) {
      if (player != null && !player.equals(mc.player) && player.isEntityAlive() && (!player.isInvisible() || ((Boolean)this.invisibles.getValue()).booleanValue())) {
        double x = interpolate(player.lastTickPosX, player.posX, event.getPartialTicks()) - (mc.getRenderManager()).renderPosX;
        double y = interpolate(player.lastTickPosY, player.posY, event.getPartialTicks()) - (mc.getRenderManager()).renderPosY;
        double z = interpolate(player.lastTickPosZ, player.posZ, event.getPartialTicks()) - (mc.getRenderManager()).renderPosZ;
        renderNameTag(player, x, y, z, event.getPartialTicks());
      } 
    } 
  }
  
  private void renderNameTag(EntityPlayer player, double x, double y, double z, float delta) {
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
    double scale = (0.0018D + ((Float)this.size.getValue()).floatValue() * distance * ((Float)this.factor.getValue()).floatValue()) / 1000.0D;
    if (distance <= 8.0D && ((Boolean)this.smartScale.getValue()).booleanValue())
      scale = 0.0245D; 
    if (!((Boolean)this.scaleing.getValue()).booleanValue())
      scale = ((Float)this.size.getValue()).floatValue() / 100.0D; 
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
    GlStateManager.enableBlend();
    if (((Boolean)this.rect.getValue()).booleanValue())
      drawRect((-width - 2), -(mc.fontRenderer.FONT_HEIGHT + 1), width + 2.0F, 1.5F, 1426063360); 
    if (((Boolean)this.outline.getValue()).booleanValue())
      drawOutlineRect((-width - 2), -(mc.fontRenderer.FONT_HEIGHT + 1), width + 2.0F, 1.5F, getOutlineColor(player)); 
    GlStateManager.disableBlend();
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
    if (((Boolean)this.armor.getValue()).booleanValue()) {
      GlStateManager.pushMatrix();
      int xOffset = -6;
      int count = 0;
      for (ItemStack armourStack : player.inventory.armorInventory) {
        if (armourStack != null) {
          xOffset -= 8;
          if (armourStack.getItem() != Items.AIR)
            count++; 
        } 
      } 
      xOffset -= 8;
      ItemStack renderOffhand = player.getHeldItemOffhand().copy();
      if (renderOffhand.hasEffect() && (renderOffhand.getItem() instanceof net.minecraft.item.ItemTool || renderOffhand.getItem() instanceof net.minecraft.item.ItemArmor))
        renderOffhand.stackSize = 1; 
      renderItemStack(renderOffhand, xOffset, -26);
      xOffset += 16;
      if (((Boolean)this.reversed.getValue()).booleanValue()) {
        for (int index = 0; index <= 3; index++) {
          ItemStack armourStack = (ItemStack)player.inventory.armorInventory.get(index);
          if (armourStack != null && armourStack.getItem() != Items.AIR) {
            ItemStack renderStack1 = armourStack.copy();
            renderItemStack(armourStack, xOffset, -26);
            xOffset += 16;
          } 
        } 
      } else {
        for (int index = 3; index >= 0; index--) {
          ItemStack armourStack = (ItemStack)player.inventory.armorInventory.get(index);
          if (armourStack != null && armourStack.getItem() != Items.AIR) {
            ItemStack renderStack1 = armourStack.copy();
            renderItemStack(armourStack, xOffset, -26);
            xOffset += 16;
          } 
        } 
      } 
      renderItemStack(renderMainHand, xOffset, -26);
      GlStateManager.popMatrix();
    } 
    this.renderer.drawStringWithShadow(displayTag, -width, -(this.renderer.getFontHeight() - 1), getDisplayColor(player));
    camera.posX = originalPositionX;
    camera.posY = originalPositionY;
    camera.posZ = originalPositionZ;
    GlStateManager.enableDepth();
    GlStateManager.disableBlend();
    GlStateManager.disablePolygonOffset();
    GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
    GlStateManager.popMatrix();
  }
  
  private int getDisplayColor(EntityPlayer player) {
    int displaycolor = ColorHolder.toHex(((Integer)this.NCred.getValue()).intValue(), ((Integer)this.NCgreen.getValue()).intValue(), ((Integer)this.NCblue.getValue()).intValue());
    if (OyVey.friendManager.isFriend(player))
      return ColorHolder.toHex(((Integer)this.FCred.getValue()).intValue(), ((Integer)this.FCgreen.getValue()).intValue(), ((Integer)this.FCblue.getValue()).intValue()); 
    if (player.isInvisible() && ((Boolean)this.invisibles.getValue()).booleanValue()) {
      displaycolor = ColorHolder.toHex(((Integer)this.ICred.getValue()).intValue(), ((Integer)this.ICgreen.getValue()).intValue(), ((Integer)this.ICblue.getValue()).intValue());
    } else if (player.isSneaking() && ((Boolean)this.sneak.getValue()).booleanValue()) {
      displaycolor = ColorHolder.toHex(((Integer)this.SCred.getValue()).intValue(), ((Integer)this.SCgreen.getValue()).intValue(), ((Integer)this.SCblue.getValue()).intValue());
    } 
    return displaycolor;
  }
  
  private int getOutlineColor(EntityPlayer player) {
    int outlinecolor = ColorHolder.toHex(((Integer)this.Ored.getValue()).intValue(), ((Integer)this.Ogreen.getValue()).intValue(), ((Integer)this.Oblue.getValue()).intValue());
    if (OyVey.friendManager.isFriend(player)) {
      outlinecolor = ColorHolder.toHex(((Integer)this.FOred.getValue()).intValue(), ((Integer)this.FOgreen.getValue()).intValue(), ((Integer)this.FOblue.getValue()).intValue());
    } else if (player.isInvisible() && ((Boolean)this.invisibles.getValue()).booleanValue()) {
      outlinecolor = ColorHolder.toHex(((Integer)this.IOred.getValue()).intValue(), ((Integer)this.IOgreen.getValue()).intValue(), ((Integer)this.IOblue.getValue()).intValue());
    } else if (player.isSneaking() && ((Boolean)this.sneak.getValue()).booleanValue()) {
      outlinecolor = ColorHolder.toHex(((Integer)this.SOred.getValue()).intValue(), ((Integer)this.SOgreen.getValue()).intValue(), ((Integer)this.SOblue.getValue()).intValue());
    } 
    return outlinecolor;
  }
  
  private void renderItemStack(ItemStack stack, int x, int y) {
    GlStateManager.pushMatrix();
    GlStateManager.depthMask(true);
    GlStateManager.clear(256);
    RenderHelper.enableStandardItemLighting();
    (mc.getRenderItem()).zLevel = -150.0F;
    GlStateManager.disableAlpha();
    GlStateManager.enableDepth();
    GlStateManager.disableCull();
    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
    mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, y);
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
    int yCount = y;
    if (stack.getItem() == Items.GOLDEN_APPLE && stack.hasEffect()) {
      this.renderer.drawStringWithShadow("god", (x * 2), enchantmentY, -3977919);
      enchantmentY -= 8;
    } 
    NBTTagList enchants = stack.getEnchantmentTagList();
    if (enchants.tagCount() > 2 && ((Boolean)this.max.getValue()).booleanValue()) {
      if (((Boolean)this.maxText.getValue()).booleanValue()) {
        this.renderer.drawStringWithShadow("", (x * 2), enchantmentY, ColorHolder.toHex(((Integer)this.Mred.getValue()).intValue(), ((Integer)this.Mgreen.getValue()).intValue(), ((Integer)this.Mblue.getValue()).intValue()));
        enchantmentY -= 8;
      } else {
        this.renderer.drawStringWithShadow("max", (x * 2), enchantmentY, ColorHolder.toHex(((Integer)this.Mred.getValue()).intValue(), ((Integer)this.Mgreen.getValue()).intValue(), ((Integer)this.Mblue.getValue()).intValue()));
        enchantmentY -= 8;
      } 
    } else {
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
    } 
    if (DamageUtil.hasDurability(stack)) {
      String color;
      int percent = DamageUtil.getRoundedDamage(stack);
      if (percent >= 60) {
        color = TextUtil.GREEN;
      } else if (percent >= 25) {
        color = TextUtil.YELLOW;
      } else {
        color = TextUtil.RED;
      } 
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
      if (encY > enchantmentY)
        enchantmentY = encY; 
    } 
    ItemStack renderMainHand = player.getHeldItemMainhand().copy();
    if (renderMainHand.hasEffect()) {
      float encY = 0.0F;
      NBTTagList enchants = renderMainHand.getEnchantmentTagList();
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
    ItemStack renderOffHand = player.getHeldItemOffhand().copy();
    if (renderOffHand.hasEffect()) {
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
    String color, name = player.getDisplayName().getFormattedText();
    if (name.contains(mc.getSession().getUsername()))
      name = "You"; 
    if (!((Boolean)this.health.getValue()).booleanValue())
      return name; 
    float health = EntityUtil.getHealth((Entity)player);
    if (health > 18.0F) {
      color = TextUtil.GREEN;
    } else if (health > 16.0F) {
      color = TextUtil.DARK_GREEN;
    } else if (health > 12.0F) {
      color = TextUtil.YELLOW;
    } else if (health > 8.0F) {
      color = TextUtil.RED;
    } else if (health > 5.0F) {
      color = TextUtil.DARK_RED;
    } else {
      color = TextUtil.DARK_RED;
    } 
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
      if (player.isCreative()) {
        gameModeStr = gameModeStr + "[C] ";
      } else if (player.isSpectator() || player.isInvisible()) {
        gameModeStr = gameModeStr + "[I] ";
      } else {
        gameModeStr = gameModeStr + "[S] ";
      }  
    if (Math.floor(health) == health) {
      name = name + color + " " + ((health > 0.0F) ? (String)Integer.valueOf((int)Math.floor(health)) : "dead");
    } else {
      name = name + color + " " + ((health > 0.0F) ? (String)Integer.valueOf((int)health) : "dead");
    } 
    return " " + pingStr + idString + gameModeStr + name + " ";
  }
  
  private double interpolate(double previous, double current, float delta) {
    return previous + (current - previous) * delta;
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
    GlStateManager.glLineWidth(((Float)this.Owidth.getValue()).floatValue());
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
  
  public void drawRect(float x, float y, float w, float h, int color) {
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    float red = (color >> 16 & 0xFF) / 255.0F;
    float green = (color >> 8 & 0xFF) / 255.0F;
    float blue = (color & 0xFF) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.glLineWidth(((Float)this.Owidth.getValue()).floatValue());
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
  
  public void onUpdate() {
    if (((Boolean)this.outline.getValue()).equals(Boolean.valueOf(false))) {
      this.rect.setValue(Boolean.valueOf(true));
    } else if (((Boolean)this.rect.getValue()).equals(Boolean.valueOf(false))) {
      this.outline.setValue(Boolean.valueOf(true));
    } 
    if (((Boolean)this.ORainbow.getValue()).booleanValue())
      OutlineRainbow(); 
    if (((Boolean)this.NCRainbow.getValue()).booleanValue())
      TextRainbow(); 
    if (((Boolean)this.FCRainbow.getValue()).booleanValue())
      FriendRainbow(); 
    if (((Boolean)this.SCRainbow.getValue()).booleanValue())
      SneakColorRainbow(); 
    if (((Boolean)this.ICRainbow.getValue()).booleanValue())
      InvisibleRainbow(); 
    if (((Boolean)this.FORainbow.getValue()).booleanValue())
      FriendOutlineRainbow(); 
    if (((Boolean)this.IORainbow.getValue()).booleanValue())
      InvisibleOutlineRainbow(); 
    if (((Boolean)this.SORainbow.getValue()).booleanValue())
      SneakOutlineRainbow(); 
  }
  
  public void OutlineRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.Ored.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.Ogreen.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.Oblue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
  
  public void TextRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.NCred.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.NCgreen.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.NCblue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
  
  public void FriendRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.FCred.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.FCgreen.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.FCblue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
  
  public void SneakColorRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.SCred.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.SCgreen.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.SCblue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
  
  public void InvisibleRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.ICred.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.ICgreen.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.ICblue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
  
  public void InvisibleOutlineRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.IOred.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.IOgreen.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.IOblue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
  
  public void FriendOutlineRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.FOred.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.FOgreen.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.FOblue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
  
  public void SneakOutlineRainbow() {
    float[] tick_color = { (float)(System.currentTimeMillis() % 11520L) / 11520.0F };
    int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8F, 0.8F);
    this.SOred.setValue(Integer.valueOf(color_rgb_o >> 16 & 0xFF));
    this.SOgreen.setValue(Integer.valueOf(color_rgb_o >> 8 & 0xFF));
    this.SOblue.setValue(Integer.valueOf(color_rgb_o & 0xFF));
  }
}
