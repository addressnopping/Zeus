package me.alpha432.oyvey.manager;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.chat.AutoGG;
import me.alpha432.oyvey.features.modules.chat.ChatSuffix;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.features.modules.client.FontMod;
import me.alpha432.oyvey.features.modules.client.GUIBlur;
import me.alpha432.oyvey.features.modules.client.HUD;
import me.alpha432.oyvey.features.modules.client.HudComponents;
import me.alpha432.oyvey.features.modules.client.NickHider;
import me.alpha432.oyvey.features.modules.client.RPC;
import me.alpha432.oyvey.features.modules.combat.AutoArmor;
import me.alpha432.oyvey.features.modules.combat.AutoMinecart;
import me.alpha432.oyvey.features.modules.combat.AutoTrap;
import me.alpha432.oyvey.features.modules.combat.AutoWeb;
import me.alpha432.oyvey.features.modules.combat.Criticals;
import me.alpha432.oyvey.features.modules.combat.GodModule;
import me.alpha432.oyvey.features.modules.combat.HoleFiller;
import me.alpha432.oyvey.features.modules.combat.InstantSelfFill;
import me.alpha432.oyvey.features.modules.combat.Killaura;
import me.alpha432.oyvey.features.modules.combat.Offhand;
import me.alpha432.oyvey.features.modules.combat.OyVeyAutoCrystal;
import me.alpha432.oyvey.features.modules.combat.PhobosCrystal;
import me.alpha432.oyvey.features.modules.combat.Quiver;
import me.alpha432.oyvey.features.modules.combat.SelfCrystal;
import me.alpha432.oyvey.features.modules.combat.SelfFill;
import me.alpha432.oyvey.features.modules.combat.SelfWeb;
import me.alpha432.oyvey.features.modules.combat.Selftrap;
import me.alpha432.oyvey.features.modules.combat.Surround;
import me.alpha432.oyvey.features.modules.misc.BuildHeight;
import me.alpha432.oyvey.features.modules.misc.ChatModifier;
import me.alpha432.oyvey.features.modules.misc.ExtraTab;
import me.alpha432.oyvey.features.modules.misc.GhastNotifier;
import me.alpha432.oyvey.features.modules.misc.MCF;
import me.alpha432.oyvey.features.modules.misc.NarratorTweaks;
import me.alpha432.oyvey.features.modules.misc.NoEntityTrace;
import me.alpha432.oyvey.features.modules.misc.NoHandShake;
import me.alpha432.oyvey.features.modules.misc.NoHitBox;
import me.alpha432.oyvey.features.modules.misc.NoSoundLag;
import me.alpha432.oyvey.features.modules.misc.PearlNotify;
import me.alpha432.oyvey.features.modules.misc.PopCounter;
import me.alpha432.oyvey.features.modules.misc.Timestamps;
import me.alpha432.oyvey.features.modules.misc.ToolTips;
import me.alpha432.oyvey.features.modules.misc.Tracker;
import me.alpha432.oyvey.features.modules.movement.AntiVoid;
import me.alpha432.oyvey.features.modules.movement.BoatFly;
import me.alpha432.oyvey.features.modules.movement.Flight;
import me.alpha432.oyvey.features.modules.movement.NoSlowDown;
import me.alpha432.oyvey.features.modules.movement.PacketFly;
import me.alpha432.oyvey.features.modules.movement.ReverseStep;
import me.alpha432.oyvey.features.modules.movement.Scaffold;
import me.alpha432.oyvey.features.modules.movement.Speed;
import me.alpha432.oyvey.features.modules.movement.Step;
import me.alpha432.oyvey.features.modules.player.Announcer;
import me.alpha432.oyvey.features.modules.player.Burrow;
import me.alpha432.oyvey.features.modules.player.FakeKick;
import me.alpha432.oyvey.features.modules.player.FakePlayer;
import me.alpha432.oyvey.features.modules.player.FastPlace;
import me.alpha432.oyvey.features.modules.player.Freecam;
import me.alpha432.oyvey.features.modules.player.LiquidInteract;
import me.alpha432.oyvey.features.modules.player.MCP;
import me.alpha432.oyvey.features.modules.player.MultiTask;
import me.alpha432.oyvey.features.modules.player.QueueSkip;
import me.alpha432.oyvey.features.modules.player.Replenish;
import me.alpha432.oyvey.features.modules.player.Speedmine;
import me.alpha432.oyvey.features.modules.player.TpsSync;
import me.alpha432.oyvey.features.modules.player.XCarry;
import me.alpha432.oyvey.features.modules.render.ArrowESP;
import me.alpha432.oyvey.features.modules.render.BlockHighlight;
import me.alpha432.oyvey.features.modules.render.ESP;
import me.alpha432.oyvey.features.modules.render.GlintModify;
import me.alpha432.oyvey.features.modules.render.HandChams;
import me.alpha432.oyvey.features.modules.render.HitMarkers;
import me.alpha432.oyvey.features.modules.render.HoleESP;
import me.alpha432.oyvey.features.modules.render.NameTags;
import me.alpha432.oyvey.features.modules.render.Shaders;
import me.alpha432.oyvey.features.modules.render.Skeleton;
import me.alpha432.oyvey.features.modules.render.SkyColor;
import me.alpha432.oyvey.features.modules.render.SmallShield;
import me.alpha432.oyvey.features.modules.render.Swing;
import me.alpha432.oyvey.features.modules.render.TestNameTags;
import me.alpha432.oyvey.features.modules.render.TexturedChams;
import me.alpha432.oyvey.features.modules.render.Trajectories;
import me.alpha432.oyvey.features.modules.render.Viewmodel;
import me.alpha432.oyvey.features.modules.render.Wireframe;
import me.alpha432.oyvey.util.Util;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class ModuleManager extends Feature {
  public ArrayList<Module> modules = new ArrayList<>();
  
  public List<Module> sortedModules = new ArrayList<>();
  
  public List<String> sortedModulesABC = new ArrayList<>();
  
  public Animation animationThread;
  
  public static ArrayList<Module> nigger;
  
  public void init() {
    this.modules.add(new ClickGui());
    this.modules.add(new FontMod());
    this.modules.add(new GUIBlur());
    this.modules.add(new HUD());
    this.modules.add(new HudComponents());
    this.modules.add(new NickHider());
    this.modules.add(new RPC());
    this.modules.add(new Shaders());
    this.modules.add(new BlockHighlight());
    this.modules.add(new HoleESP());
    this.modules.add(new Skeleton());
    this.modules.add(new Wireframe());
    this.modules.add(new SmallShield());
    this.modules.add(new HandChams());
    this.modules.add(new Trajectories());
    this.modules.add(new Viewmodel());
    this.modules.add(new Swing());
    this.modules.add(new ArrowESP());
    this.modules.add(new GlintModify());
    this.modules.add(new SkyColor());
    this.modules.add(new ESP());
    this.modules.add(new NameTags());
    this.modules.add(new TestNameTags());
    this.modules.add(new TexturedChams());
    this.modules.add(new HitMarkers());
    this.modules.add(new Offhand());
    this.modules.add(new Surround());
    this.modules.add(new AutoTrap());
    this.modules.add(new GodModule());
    this.modules.add(new AutoWeb());
    this.modules.add(new PhobosCrystal());
    this.modules.add(new OyVeyAutoCrystal());
    this.modules.add(new Killaura());
    this.modules.add(new Criticals());
    this.modules.add(new HoleFiller());
    this.modules.add(new AutoArmor());
    this.modules.add(new SelfCrystal());
    this.modules.add(new Selftrap());
    this.modules.add(new SelfWeb());
    this.modules.add(new Quiver());
    this.modules.add(new AutoMinecart());
    this.modules.add(new SelfFill());
    this.modules.add(new InstantSelfFill());
    this.modules.add(new FakeKick());
    this.modules.add(new Freecam());
    this.modules.add(new FastPlace());
    this.modules.add(new TpsSync());
    this.modules.add(new Replenish());
    this.modules.add(new FakePlayer());
    this.modules.add(new MultiTask());
    this.modules.add(new MCP());
    this.modules.add(new LiquidInteract());
    this.modules.add(new Speedmine());
    this.modules.add(new Announcer());
    this.modules.add(new Burrow());
    this.modules.add(new ExtraTab());
    this.modules.add(new NoHitBox());
    this.modules.add(new Timestamps());
    this.modules.add(new NoSoundLag());
    this.modules.add(new QueueSkip());
    this.modules.add(new NoHandShake());
    this.modules.add(new BuildHeight());
    this.modules.add(new ChatModifier());
    this.modules.add(new MCF());
    this.modules.add(new PearlNotify());
    this.modules.add(new ToolTips());
    this.modules.add(new Tracker());
    this.modules.add(new PopCounter());
    this.modules.add(new NarratorTweaks());
    this.modules.add(new GhastNotifier());
    this.modules.add(new XCarry());
    this.modules.add(new NoEntityTrace());
    this.modules.add(new PacketFly());
    this.modules.add(new Speed());
    this.modules.add(new Step());
    this.modules.add(new BoatFly());
    this.modules.add(new ReverseStep());
    this.modules.add(new AntiVoid());
    this.modules.add(new Flight());
    this.modules.add(new Scaffold());
    this.modules.add(new NoSlowDown());
    this.modules.add(new ChatSuffix());
    this.modules.add(new AutoGG());
  }
  
  public Module getModuleByName(String name) {
    for (Module module : this.modules) {
      if (!module.getName().equalsIgnoreCase(name))
        continue; 
      return module;
    } 
    return null;
  }
  
  public <T extends Module> T getModuleByClass(Class<T> clazz) {
    for (Module module : this.modules) {
      if (!clazz.isInstance(module))
        continue; 
      return (T)module;
    } 
    return null;
  }
  
  public void enableModule(Class<Module> clazz) {
    Module module = getModuleByClass(clazz);
    if (module != null)
      module.enable(); 
  }
  
  public void disableModule(Class<Module> clazz) {
    Module module = getModuleByClass(clazz);
    if (module != null)
      module.disable(); 
  }
  
  public void enableModule(String name) {
    Module module = getModuleByName(name);
    if (module != null)
      module.enable(); 
  }
  
  public void disableModule(String name) {
    Module module = getModuleByName(name);
    if (module != null)
      module.disable(); 
  }
  
  public boolean isModuleEnabled(String name) {
    Module module = getModuleByName(name);
    return (module != null && module.isOn());
  }
  
  public boolean isModuleEnabled(Class<Module> clazz) {
    Module module = getModuleByClass(clazz);
    return (module != null && module.isOn());
  }
  
  public Module getModuleByDisplayName(String displayName) {
    for (Module module : this.modules) {
      if (!module.getDisplayName().equalsIgnoreCase(displayName))
        continue; 
      return module;
    } 
    return null;
  }
  
  public ArrayList<Module> getEnabledModules() {
    ArrayList<Module> enabledModules = new ArrayList<>();
    for (Module module : this.modules) {
      if (!module.isEnabled())
        continue; 
      enabledModules.add(module);
    } 
    return enabledModules;
  }
  
  public ArrayList<String> getEnabledModulesName() {
    ArrayList<String> enabledModules = new ArrayList<>();
    for (Module module : this.modules) {
      if (!module.isEnabled() || !module.isDrawn())
        continue; 
      enabledModules.add(module.getFullArrayString());
    } 
    return enabledModules;
  }
  
  public ArrayList<Module> getModulesByCategory(Module.Category category) {
    ArrayList<Module> modulesCategory = new ArrayList<>();
    this.modules.forEach(module -> {
          if (module.getCategory() == category)
            modulesCategory.add(module); 
        });
    return modulesCategory;
  }
  
  public List<Module.Category> getCategories() {
    return Arrays.asList(Module.Category.values());
  }
  
  public void onLoad() {
    this.modules.stream().filter(Module::listening).forEach(MinecraftForge.EVENT_BUS::register);
    this.modules.forEach(Module::onLoad);
  }
  
  public void onUpdate() {
    this.modules.stream().filter(Feature::isEnabled).forEach(Module::onUpdate);
  }
  
  public void onTick() {
    this.modules.stream().filter(Feature::isEnabled).forEach(Module::onTick);
  }
  
  public void onRender2D(Render2DEvent event) {
    this.modules.stream().filter(Feature::isEnabled).forEach(module -> module.onRender2D(event));
  }
  
  public void onRender3D(Render3DEvent event) {
    this.modules.stream().filter(Feature::isEnabled).forEach(module -> module.onRender3D(event));
  }
  
  public <T extends Module> T getModuleT(Class<T> clazz) {
    return (T)this.modules.stream().filter(module -> (module.getClass() == clazz)).map(module -> module).findFirst().orElse(null);
  }
  
  public void sortModules(boolean reverse) {
    this.sortedModules = (List<Module>)getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> Integer.valueOf(this.renderer.getStringWidth(module.getFullArrayString()) * (reverse ? -1 : 1)))).collect(Collectors.toList());
  }
  
  public void sortModulesABC() {
    this.sortedModulesABC = new ArrayList<>(getEnabledModulesName());
    this.sortedModulesABC.sort(String.CASE_INSENSITIVE_ORDER);
  }
  
  public void onLogout() {
    this.modules.forEach(Module::onLogout);
  }
  
  public void onLogin() {
    this.modules.forEach(Module::onLogin);
  }
  
  public void onUnload() {
    this.modules.forEach(MinecraftForge.EVENT_BUS::unregister);
    this.modules.forEach(Module::onUnload);
  }
  
  public void onUnloadPost() {
    for (Module module : this.modules)
      module.enabled.setValue(Boolean.valueOf(false)); 
  }
  
  public void onKeyPressed(int eventKey) {
    if (eventKey == 0 || !Keyboard.getEventKeyState() || mc.currentScreen instanceof me.alpha432.oyvey.features.gui.OyVeyGui)
      return; 
    this.modules.forEach(module -> {
          if (module.getBind().getKey() == eventKey)
            module.toggle(); 
        });
  }
  
  private class Animation extends Thread {
    public Module module;
    
    public float offset;
    
    public float vOffset;
    
    ScheduledExecutorService service;
    
    public Animation() {
      super("Animation");
      this.service = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void run() {
      if ((HUD.getInstance()).renderingMode.getValue() == HUD.RenderingMode.Length) {
        for (Module module : ModuleManager.this.sortedModules) {
          String text = module.getDisplayName() + ChatFormatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + ChatFormatting.WHITE + module.getDisplayInfo() + ChatFormatting.GRAY + "]") : "");
          module.offset = ModuleManager.this.renderer.getStringWidth(text) / ((Integer)(HUD.getInstance()).animationHorizontalTime.getValue()).floatValue();
          module.vOffset = ModuleManager.this.renderer.getFontHeight() / ((Integer)(HUD.getInstance()).animationVerticalTime.getValue()).floatValue();
          if (module.isEnabled() && ((Integer)(HUD.getInstance()).animationHorizontalTime.getValue()).intValue() != 1) {
            if (module.arrayListOffset <= module.offset || Util.mc.world == null)
              continue; 
            module.arrayListOffset -= module.offset;
            module.sliding = true;
            continue;
          } 
          if (!module.isDisabled() || ((Integer)(HUD.getInstance()).animationHorizontalTime.getValue()).intValue() == 1)
            continue; 
          if (module.arrayListOffset < ModuleManager.this.renderer.getStringWidth(text) && Util.mc.world != null) {
            module.arrayListOffset += module.offset;
            module.sliding = true;
            continue;
          } 
          module.sliding = false;
        } 
      } else {
        for (String e : ModuleManager.this.sortedModulesABC) {
          Module module = OyVey.moduleManager.getModuleByName(e);
          String text = module.getDisplayName() + ChatFormatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + ChatFormatting.WHITE + module.getDisplayInfo() + ChatFormatting.GRAY + "]") : "");
          module.offset = ModuleManager.this.renderer.getStringWidth(text) / ((Integer)(HUD.getInstance()).animationHorizontalTime.getValue()).floatValue();
          module.vOffset = ModuleManager.this.renderer.getFontHeight() / ((Integer)(HUD.getInstance()).animationVerticalTime.getValue()).floatValue();
          if (module.isEnabled() && ((Integer)(HUD.getInstance()).animationHorizontalTime.getValue()).intValue() != 1) {
            if (module.arrayListOffset <= module.offset || Util.mc.world == null)
              continue; 
            module.arrayListOffset -= module.offset;
            module.sliding = true;
            continue;
          } 
          if (!module.isDisabled() || ((Integer)(HUD.getInstance()).animationHorizontalTime.getValue()).intValue() == 1)
            continue; 
          if (module.arrayListOffset < ModuleManager.this.renderer.getStringWidth(text) && Util.mc.world != null) {
            module.arrayListOffset += module.offset;
            module.sliding = true;
            continue;
          } 
          module.sliding = false;
        } 
      } 
    }
    
    public void start() {
      System.out.println("Starting animation thread.");
      this.service.scheduleAtFixedRate(this, 0L, 1L, TimeUnit.MILLISECONDS);
    }
  }
  
  public static ArrayList<Module> getModules() {
    return nigger;
  }
  
  public static boolean isModuleEnablednigger(String name) {
    Module modulenigger = getModules().stream().filter(mm -> mm.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    return modulenigger.isEnabled();
  }
  
  public static boolean isModuleEnablednigger(Module modulenigger) {
    return modulenigger.isEnabled();
  }
}
