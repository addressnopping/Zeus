package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Viewmodel extends Module {
  public float defaultFov;
  
  public Setting<Integer> viewmodelDistance = register(new Setting("ViewmodelDistance", Integer.valueOf(125), Integer.valueOf(0), Integer.valueOf(170), "Changes the distance of the Viewmodel"));
  
  public Viewmodel() {
    super("Viewmodel", "Changes viewmodel of items", Module.Category.RENDER, false, true, false);
  }
  
  @SubscribeEvent
  public void fovVMC(EntityViewRenderEvent.FOVModifier e) {
    e.setFOV(((Integer)this.viewmodelDistance.getValue()).intValue());
  }
  
  public void onEnable() {
    this.defaultFov = mc.gameSettings.fovSetting;
  }
  
  public void onDisable() {
    mc.gameSettings.fovSetting = this.defaultFov;
  }
}
