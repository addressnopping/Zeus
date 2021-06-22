package me.alpha432.oyvey.features.modules.render;

import java.awt.Color;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class HoleESP extends Module {
  public Setting<Boolean> renderOwn = register(new Setting("RenderOwn", Boolean.valueOf(true)));
  
  public Setting<Boolean> fov = register(new Setting("InFov", Boolean.valueOf(true)));
  
  public Setting<Boolean> rainbow = register(new Setting("Rainbow", Boolean.valueOf(false)));
  
  private final Setting<Integer> range = register(new Setting("RangeX", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(10)));
  
  private final Setting<Integer> rangeY = register(new Setting("RangeY", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(10)));
  
  public Setting<Boolean> box = register(new Setting("Box", Boolean.valueOf(true)));
  
  public Setting<Boolean> gradientBox = register(new Setting("Gradient", Boolean.valueOf(false), v -> ((Boolean)this.box.getValue()).booleanValue()));
  
  public Setting<Boolean> invertGradientBox = register(new Setting("ReverseGradient", Boolean.valueOf(false), v -> ((Boolean)this.gradientBox.getValue()).booleanValue()));
  
  public Setting<Boolean> outline = register(new Setting("Outline", Boolean.valueOf(true)));
  
  public Setting<Boolean> gradientOutline = register(new Setting("GradientOutline", Boolean.valueOf(false), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  public Setting<Boolean> invertGradientOutline = register(new Setting("ReverseOutline", Boolean.valueOf(false), v -> ((Boolean)this.gradientOutline.getValue()).booleanValue()));
  
  public Setting<Double> height = register(new Setting("Height", Double.valueOf(0.0D), Double.valueOf(-2.0D), Double.valueOf(2.0D)));
  
  private Setting<Integer> red = register(new Setting("Red", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255)));
  
  private Setting<Integer> green = register(new Setting("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private Setting<Integer> blue = register(new Setting("Blue", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255)));
  
  private Setting<Integer> alpha = register(new Setting("Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", Integer.valueOf(125), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.box.getValue()).booleanValue()));
  
  private Setting<Float> lineWidth = register(new Setting("LineWidth", Float.valueOf(1.0F), Float.valueOf(0.1F), Float.valueOf(5.0F), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  public Setting<Boolean> safeColor = register(new Setting("BedrockColor", Boolean.valueOf(false)));
  
  private Setting<Integer> safeRed = register(new Setting("BedrockRed", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.safeColor.getValue()).booleanValue()));
  
  private Setting<Integer> safeGreen = register(new Setting("BedrockGreen", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.safeColor.getValue()).booleanValue()));
  
  private Setting<Integer> safeBlue = register(new Setting("BedrockBlue", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.safeColor.getValue()).booleanValue()));
  
  private Setting<Integer> safeAlpha = register(new Setting("BedrockAlpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.safeColor.getValue()).booleanValue()));
  
  public Setting<Boolean> customOutline = register(new Setting("CustomLine", Boolean.valueOf(false), v -> ((Boolean)this.outline.getValue()).booleanValue()));
  
  private Setting<Integer> cRed = register(new Setting("OL-Red", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.customOutline.getValue()).booleanValue() && ((Boolean)this.outline.getValue()).booleanValue())));
  
  private Setting<Integer> cGreen = register(new Setting("OL-Green", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.customOutline.getValue()).booleanValue() && ((Boolean)this.outline.getValue()).booleanValue())));
  
  private Setting<Integer> cBlue = register(new Setting("OL-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.customOutline.getValue()).booleanValue() && ((Boolean)this.outline.getValue()).booleanValue())));
  
  private Setting<Integer> cAlpha = register(new Setting("OL-Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.customOutline.getValue()).booleanValue() && ((Boolean)this.outline.getValue()).booleanValue())));
  
  private Setting<Integer> safecRed = register(new Setting("OL-SafeRed", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.customOutline.getValue()).booleanValue() && ((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.safeColor.getValue()).booleanValue())));
  
  private Setting<Integer> safecGreen = register(new Setting("OL-SafeGreen", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.customOutline.getValue()).booleanValue() && ((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.safeColor.getValue()).booleanValue())));
  
  private Setting<Integer> safecBlue = register(new Setting("OL-SafeBlue", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.customOutline.getValue()).booleanValue() && ((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.safeColor.getValue()).booleanValue())));
  
  private Setting<Integer> safecAlpha = register(new Setting("OL-SafeAlpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> (((Boolean)this.customOutline.getValue()).booleanValue() && ((Boolean)this.outline.getValue()).booleanValue() && ((Boolean)this.safeColor.getValue()).booleanValue())));
  
  private static HoleESP INSTANCE = new HoleESP();
  
  private int currentAlpha = 0;
  
  public HoleESP() {
    super("HoleESP", "Shows safe spots.", Module.Category.RENDER, false, false, false);
    setInstance();
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  public static HoleESP getInstance() {
    if (INSTANCE == null)
      INSTANCE = new HoleESP(); 
    return INSTANCE;
  }
  
  public void onRender3D(Render3DEvent event) {
    assert mc.renderViewEntity != null;
    Vec3i playerPos = new Vec3i(mc.renderViewEntity.posX, mc.renderViewEntity.posY, mc.renderViewEntity.posZ);
    for (int x = playerPos.getX() - ((Integer)this.range.getValue()).intValue(); x < playerPos.getX() + ((Integer)this.range.getValue()).intValue(); x++) {
      for (int z = playerPos.getZ() - ((Integer)this.range.getValue()).intValue(); z < playerPos.getZ() + ((Integer)this.range.getValue()).intValue(); z++) {
        for (int y = playerPos.getY() + ((Integer)this.rangeY.getValue()).intValue(); y > playerPos.getY() - ((Integer)this.rangeY.getValue()).intValue(); y--) {
          BlockPos pos = new BlockPos(x, y, z);
          if (mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) && (!pos.equals(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)) || ((Boolean)this.renderOwn.getValue()).booleanValue()) && (BlockUtil.isPosInFov(pos).booleanValue() || !((Boolean)this.fov.getValue()).booleanValue()))
            if (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
              RenderUtil.drawBoxESP(pos, ((Boolean)this.rainbow.getValue()).booleanValue() ? ColorUtil.rainbow(((Integer)(ClickGui.getInstance()).rainbowHue.getValue()).intValue()) : new Color(((Integer)this.safeRed.getValue()).intValue(), ((Integer)this.safeGreen.getValue()).intValue(), ((Integer)this.safeBlue.getValue()).intValue(), ((Integer)this.safeAlpha.getValue()).intValue()), ((Boolean)this.customOutline.getValue()).booleanValue(), new Color(((Integer)this.safecRed.getValue()).intValue(), ((Integer)this.safecGreen.getValue()).intValue(), ((Integer)this.safecBlue.getValue()).intValue(), ((Integer)this.safecAlpha.getValue()).intValue()), ((Float)this.lineWidth.getValue()).floatValue(), ((Boolean)this.outline.getValue()).booleanValue(), ((Boolean)this.box.getValue()).booleanValue(), ((Integer)this.boxAlpha.getValue()).intValue(), true, ((Double)this.height.getValue()).doubleValue(), ((Boolean)this.gradientBox.getValue()).booleanValue(), ((Boolean)this.gradientOutline.getValue()).booleanValue(), ((Boolean)this.invertGradientBox.getValue()).booleanValue(), ((Boolean)this.invertGradientOutline.getValue()).booleanValue(), this.currentAlpha);
            } else if (BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.down()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.east()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.west()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.south()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.north()).getBlock())) {
              RenderUtil.drawBoxESP(pos, ((Boolean)this.rainbow.getValue()).booleanValue() ? ColorUtil.rainbow(((Integer)(ClickGui.getInstance()).rainbowHue.getValue()).intValue()) : new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue(), ((Integer)this.alpha.getValue()).intValue()), ((Boolean)this.customOutline.getValue()).booleanValue(), new Color(((Integer)this.cRed.getValue()).intValue(), ((Integer)this.cGreen.getValue()).intValue(), ((Integer)this.cBlue.getValue()).intValue(), ((Integer)this.cAlpha.getValue()).intValue()), ((Float)this.lineWidth.getValue()).floatValue(), ((Boolean)this.outline.getValue()).booleanValue(), ((Boolean)this.box.getValue()).booleanValue(), ((Integer)this.boxAlpha.getValue()).intValue(), true, ((Double)this.height.getValue()).doubleValue(), ((Boolean)this.gradientBox.getValue()).booleanValue(), ((Boolean)this.gradientOutline.getValue()).booleanValue(), ((Boolean)this.invertGradientBox.getValue()).booleanValue(), ((Boolean)this.invertGradientOutline.getValue()).booleanValue(), this.currentAlpha);
            }  
        } 
      } 
    } 
  }
}
