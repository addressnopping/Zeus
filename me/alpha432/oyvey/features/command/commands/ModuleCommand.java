package me.alpha432.oyvey.features.command.commands;

import com.google.gson.JsonParser;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.manager.ConfigManager;

public class ModuleCommand extends Command {
  public ModuleCommand() {
    super("module", new String[] { "<module>", "<set/reset>", "<setting>", "<value>" });
  }
  
  public void execute(String[] commands) {
    if (commands.length == 1) {
      sendMessage("Modules: ");
      for (Module.Category category : OyVey.moduleManager.getCategories()) {
        String modules = category.getName() + ": ";
        for (Module module1 : OyVey.moduleManager.getModulesByCategory(category))
          modules = modules + (module1.isEnabled() ? (String)ChatFormatting.GREEN : (String)ChatFormatting.RED) + module1.getName() + ChatFormatting.WHITE + ", "; 
        sendMessage(modules);
      } 
      return;
    } 
    Module module = OyVey.moduleManager.getModuleByDisplayName(commands[0]);
    if (module == null) {
      module = OyVey.moduleManager.getModuleByName(commands[0]);
      if (module == null) {
        sendMessage("This module doesnt exist.");
        return;
      } 
      sendMessage(" This is the original name of the module. Its current name is: " + module.getDisplayName());
      return;
    } 
    if (commands.length == 2) {
      sendMessage(module.getDisplayName() + " : " + module.getDescription());
      for (Setting setting2 : module.getSettings())
        sendMessage(setting2.getName() + " : " + setting2.getValue() + ", " + setting2.getDescription()); 
      return;
    } 
    if (commands.length == 3) {
      if (commands[1].equalsIgnoreCase("set")) {
        sendMessage("Please specify a setting.");
      } else if (commands[1].equalsIgnoreCase("reset")) {
        for (Setting setting3 : module.getSettings())
          setting3.setValue(setting3.getDefaultValue()); 
      } else {
        sendMessage("This command doesnt exist.");
      } 
      return;
    } 
    if (commands.length == 4) {
      sendMessage("Please specify a value.");
      return;
    } 
    Setting setting;
    if (commands.length == 5 && (setting = module.getSettingByName(commands[2])) != null) {
      JsonParser jp = new JsonParser();
      if (setting.getType().equalsIgnoreCase("String")) {
        setting.setValue(commands[3]);
        sendMessage(ChatFormatting.DARK_GRAY + module.getName() + " " + setting.getName() + " has been set to " + commands[3] + ".");
        return;
      } 
      try {
        if (setting.getName().equalsIgnoreCase("Enabled")) {
          if (commands[3].equalsIgnoreCase("true"))
            module.enable(); 
          if (commands[3].equalsIgnoreCase("false"))
            module.disable(); 
        } 
        ConfigManager.setValueFromJson((Feature)module, setting, jp.parse(commands[3]));
      } catch (Exception e) {
        sendMessage("Bad Value! This setting requires a: " + setting.getType() + " value.");
        return;
      } 
      sendMessage(ChatFormatting.GRAY + module.getName() + " " + setting.getName() + " has been set to " + commands[3] + ".");
    } 
  }
}
