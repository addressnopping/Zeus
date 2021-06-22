package me.alpha432.oyvey.features.modules.render;

import java.awt.Color;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class BlockHighlight extends Module {
  private final Setting<Float> lineWidth = register(new Setting("LineWidth", Float.valueOf(1.0F), Float.valueOf(0.1F), Float.valueOf(5.0F)));
  
  private final Setting<Integer> alpha = register(new Setting("Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private final Setting<Integer> red = register(new Setting("Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private final Setting<Integer> green = register(new Setting("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private final Setting<Integer> blue = register(new Setting("Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)));
  
  private final Setting<Boolean> rainbow = register(new Setting("Rainbow", Boolean.valueOf(false)));
  
  private final Setting<Integer> rainbowhue = register(new Setting("RainbowHue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> ((Boolean)this.rainbow.getValue()).booleanValue()));
  
  public BlockHighlight() {
    super("BlockHighlight", "Highlights the block u look at.", Module.Category.RENDER, false, true, false);
  }
  
  public void onRender3D(Render3DEvent event) {
    RayTraceResult ray = mc.objectMouseOver;
    if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
      BlockPos blockpos = ray.getBlockPos();
      RenderUtil.drawBlockOutline(blockpos, ((Boolean)this.rainbow.getValue()).booleanValue() ? ColorUtil.rainbow(((Integer)this.rainbowhue.getValue()).intValue()) : new Color(((Integer)this.red.getValue()).intValue(), ((Integer)this.green.getValue()).intValue(), ((Integer)this.blue.getValue()).intValue(), ((Integer)this.alpha.getValue()).intValue()), ((Float)this.lineWidth.getValue()).floatValue(), false);
    } 
  }
}
