package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.DiscordPresence;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class RPC extends Module {
  public static RPC INSTANCE;
  
  public Setting<Boolean> showIP;
  
  public Setting<String> state;
  
  public RPC() {
    super("RPC", "Discord rich presence", Module.Category.CLIENT, false, false, false);
    this.showIP = register(new Setting("ShowIP", Boolean.valueOf(true), "Shows the server IP in your discord presence."));
    this.state = register(new Setting("State", "Zeus", "Sets the state of the DiscordRPC."));
    INSTANCE = this;
  }
  
  public void onEnable() {
    DiscordPresence.start();
  }
  
  public void onDisable() {
    DiscordPresence.stop();
  }
}
