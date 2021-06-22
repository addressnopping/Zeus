package me.alpha432.oyvey.features.modules;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.ClientEvent;
import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.client.HUD;
import me.alpha432.oyvey.features.setting.Bind;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class Module extends Feature {
  private final String description;
  
  private final Category category;
  
  public Setting<Boolean> enabled = register(new Setting("Enabled", Boolean.valueOf(false)));
  
  public Setting<Boolean> drawn = register(new Setting("Drawn", Boolean.valueOf(true)));
  
  public Setting<Bind> bind = register(new Setting("Keybind", new Bind(-1)));
  
  public Setting<String> displayName;
  
  public boolean hasListener;
  
  public boolean alwaysListening;
  
  public boolean hidden;
  
  public float arrayListOffset = 0.0F;
  
  public float arrayListVOffset = 0.0F;
  
  public float offset;
  
  public float vOffset;
  
  public boolean sliding;
  
  public Module(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening) {
    super(name);
    this.displayName = register(new Setting("DisplayName", name));
    this.description = description;
    this.category = category;
    this.hasListener = hasListener;
    this.hidden = hidden;
    this.alwaysListening = alwaysListening;
  }
  
  public boolean isSliding() {
    return this.sliding;
  }
  
  public void onEnable() {}
  
  public void onDisable() {}
  
  public void onToggle() {}
  
  public void onLoad() {}
  
  public void onTick() {}
  
  public void onLogin() {}
  
  public void onLogout() {}
  
  public void onUpdate() {}
  
  public void onRender2D(Render2DEvent event) {}
  
  public void onRender3D(Render3DEvent event) {}
  
  public void onUnload() {}
  
  public String getDisplayInfo() {
    return null;
  }
  
  public boolean isOn() {
    return ((Boolean)this.enabled.getValue()).booleanValue();
  }
  
  public boolean isOff() {
    return !((Boolean)this.enabled.getValue()).booleanValue();
  }
  
  public void setEnabled(boolean enabled) {
    if (enabled) {
      enable();
    } else {
      disable();
    } 
  }
  
  public void enable() {
    this.enabled.setValue(Boolean.TRUE);
    onToggle();
    onEnable();
    if (((Boolean)(HUD.getInstance()).notifyToggles.getValue()).booleanValue()) {
      TextComponentString text = new TextComponentString(OyVey.commandManager.getClientMessage() + " " + ChatFormatting.GREEN + getDisplayName() + " toggled on.");
      mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion((ITextComponent)text, 1);
    } 
    if (isOn() && this.hasListener && !this.alwaysListening)
      MinecraftForge.EVENT_BUS.register(this); 
  }
  
  public void disable() {
    if (this.hasListener && !this.alwaysListening)
      MinecraftForge.EVENT_BUS.unregister(this); 
    this.enabled.setValue(Boolean.valueOf(false));
    if (((Boolean)(HUD.getInstance()).notifyToggles.getValue()).booleanValue()) {
      TextComponentString text = new TextComponentString(OyVey.commandManager.getClientMessage() + " " + ChatFormatting.RED + getDisplayName() + " toggled off.");
      mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion((ITextComponent)text, 1);
    } 
    onToggle();
    onDisable();
  }
  
  public void toggle() {
    ClientEvent event = new ClientEvent(!isEnabled() ? 1 : 0, this);
    MinecraftForge.EVENT_BUS.post((Event)event);
    if (!event.isCanceled())
      setEnabled(!isEnabled()); 
  }
  
  public String getDisplayName() {
    return (String)this.displayName.getValue();
  }
  
  public void setDisplayName(String name) {
    Module module = OyVey.moduleManager.getModuleByDisplayName(name);
    Module originalModule = OyVey.moduleManager.getModuleByName(name);
    if (module == null && originalModule == null) {
      Command.sendMessage(getDisplayName() + ", name: " + getName() + ", has been renamed to: " + name);
      this.displayName.setValue(name);
      return;
    } 
    Command.sendMessage(ChatFormatting.RED + "A module of this name already exists.");
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public boolean isDrawn() {
    return ((Boolean)this.drawn.getValue()).booleanValue();
  }
  
  public void setDrawn(boolean drawn) {
    this.drawn.setValue(Boolean.valueOf(drawn));
  }
  
  public Category getCategory() {
    return this.category;
  }
  
  public String getInfo() {
    return null;
  }
  
  public Bind getBind() {
    return (Bind)this.bind.getValue();
  }
  
  public void setBind(int key) {
    this.bind.setValue(new Bind(key));
  }
  
  public boolean listening() {
    return ((this.hasListener && isOn()) || this.alwaysListening);
  }
  
  public String getFullArrayString() {
    return getDisplayName() + ChatFormatting.GRAY + ((getDisplayInfo() != null) ? (" [" + ChatFormatting.WHITE + getDisplayInfo() + ChatFormatting.GRAY + "]") : "");
  }
  
  public enum Category {
    COMBAT("Combat"),
    MISC("Misc"),
    RENDER("Render"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    CLIENT("Client"),
    CHAT("Chat");
    
    private final String name;
    
    Category(String name) {
      this.name = name;
    }
    
    public String getName() {
      return this.name;
    }
  }
}
