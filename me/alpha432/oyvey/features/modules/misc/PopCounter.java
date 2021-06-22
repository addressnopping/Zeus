package me.alpha432.oyvey.features.modules.misc;

import java.util.HashMap;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.player.EntityPlayer;

public class PopCounter extends Module {
  public static HashMap<String, Integer> TotemPopContainer = new HashMap<>();
  
  public static PopCounter INSTANCE = new PopCounter();
  
  public final Setting<String> clientname;
  
  public PopCounter() {
    super("PopCounter", "Counts other players totem pops.", Module.Category.MISC, true, true, false);
    this.clientname = register(new Setting("Name", "onpoint.ie"));
    setInstance();
  }
  
  public static PopCounter getInstance() {
    if (INSTANCE == null)
      INSTANCE = new PopCounter(); 
    return INSTANCE;
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  public void onEnable() {
    TotemPopContainer.clear();
  }
  
  public void onDeath(EntityPlayer player) {
    if (TotemPopContainer.containsKey(player.getName())) {
      int l_Count = ((Integer)TotemPopContainer.get(player.getName())).intValue();
      TotemPopContainer.remove(player.getName());
      if (l_Count == 1) {
        Command.sendSilentMessage("");
      } else {
        Command.sendSilentMessage("");
      } 
    } 
  }
  
  public void onTotemPop(EntityPlayer player) {
    if (fullNullCheck())
      return; 
    if (mc.player.equals(player))
      return; 
    int l_Count = 1;
    if (TotemPopContainer.containsKey(player.getName())) {
      l_Count = ((Integer)TotemPopContainer.get(player.getName())).intValue();
      TotemPopContainer.put(player.getName(), Integer.valueOf(++l_Count));
    } else {
      TotemPopContainer.put(player.getName(), Integer.valueOf(l_Count));
    } 
    if (l_Count == 1) {
      Command.sendSilentMessage("");
    } else {
      Command.sendSilentMessage("");
    } 
  }
}
