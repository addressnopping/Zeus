package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFog extends Module {
  public NoFog() {
    super("NoFog", "Removes fog", Module.Category.RENDER, false, true, false);
  }
  
  @SubscribeEvent
  public void fog_density(EntityViewRenderEvent.FogDensity event) {
    event.setDensity(0.0F);
    event.setCanceled(true);
  }
}
