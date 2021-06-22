package me.alpha432.oyvey.features.modules.render;

import java.awt.Color;
import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class HitMarkers extends Module {
  public final ResourceLocation image;
  
  private int renderTicks;
  
  public Setting<Integer> red;
  
  public Setting<Integer> green;
  
  public Setting<Integer> blue;
  
  public Setting<Integer> alpha;
  
  public Setting<Integer> thickness;
  
  public Setting<Double> time;
  
  public HitMarkers() {
    super("HitMarkers", "hitmarker thingys", Module.Category.RENDER, false, true, false);
    this.red = register(new Setting("Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
    this.green = register(new Setting("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
    this.blue = register(new Setting("Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
    this.alpha = register(new Setting("Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
    this.thickness = register(new Setting("Thickness", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(6)));
    this.time = register(new Setting("Time", Double.valueOf(20.0D), Double.valueOf(1.0D), Double.valueOf(50.0D)));
    this.image = new ResourceLocation("hitmarker.png");
    this.renderTicks = 100;
  }
  
  public void onRender2D(Render2DEvent event) {
    if (this.renderTicks < ((Double)this.time.getValue()).doubleValue()) {
      ScaledResolution resolution = new ScaledResolution(mc);
      drawHitMarkers();
    } 
  }
  
  public void onEnable() {
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  public void onDisable() {
    MinecraftForge.EVENT_BUS.unregister(this);
  }
  
  @SubscribeEvent
  public void onAttackEntity(AttackEntityEvent event) {
    if (!event.getEntity().equals(mc.player))
      return; 
    this.renderTicks = 0;
  }
  
  @SubscribeEvent
  public void onTickClientTick(TickEvent event) {
    this.renderTicks++;
  }
  
  public void drawHitMarkers() {
    ScaledResolution resolution = new ScaledResolution(mc);
    RenderUtil.drawLine(resolution.getScaledWidth() / 2.0F - 4.0F, resolution.getScaledHeight() / 2.0F - 4.0F, resolution.getScaledWidth() / 2.0F - 8.0F, resolution.getScaledHeight() / 2.0F - 8.0F, ((Integer)this.thickness.getValue()).intValue(), (new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue())).getRGB());
    RenderUtil.drawLine(resolution.getScaledWidth() / 2.0F + 4.0F, resolution.getScaledHeight() / 2.0F - 4.0F, resolution.getScaledWidth() / 2.0F + 8.0F, resolution.getScaledHeight() / 2.0F - 8.0F, ((Integer)this.thickness.getValue()).intValue(), (new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue())).getRGB());
    RenderUtil.drawLine(resolution.getScaledWidth() / 2.0F - 4.0F, resolution.getScaledHeight() / 2.0F + 4.0F, resolution.getScaledWidth() / 2.0F - 8.0F, resolution.getScaledHeight() / 2.0F + 8.0F, ((Integer)this.thickness.getValue()).intValue(), (new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue())).getRGB());
    RenderUtil.drawLine(resolution.getScaledWidth() / 2.0F + 4.0F, resolution.getScaledHeight() / 2.0F + 4.0F, resolution.getScaledWidth() / 2.0F + 8.0F, resolution.getScaledHeight() / 2.0F + 8.0F, ((Integer)this.thickness.getValue()).intValue(), (new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue())).getRGB());
  }
}
