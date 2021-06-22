package me.alpha432.oyvey;

import java.io.InputStream;
import java.nio.ByteBuffer;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.gui.font.CustomFont;
import me.alpha432.oyvey.manager.ColorManager;
import me.alpha432.oyvey.manager.CommandManager;
import me.alpha432.oyvey.manager.ConfigManager;
import me.alpha432.oyvey.manager.EventManager;
import me.alpha432.oyvey.manager.FileManager;
import me.alpha432.oyvey.manager.FriendManager;
import me.alpha432.oyvey.manager.HoleManager;
import me.alpha432.oyvey.manager.InventoryManager;
import me.alpha432.oyvey.manager.ModuleManager;
import me.alpha432.oyvey.manager.PacketManager;
import me.alpha432.oyvey.manager.PositionManager;
import me.alpha432.oyvey.manager.PotionManager;
import me.alpha432.oyvey.manager.ReloadManager;
import me.alpha432.oyvey.manager.RotationManager;
import me.alpha432.oyvey.manager.ServerManager;
import me.alpha432.oyvey.manager.SpeedManager;
import me.alpha432.oyvey.manager.TextManager;
import me.alpha432.oyvey.manager.TimerManager;
import me.alpha432.oyvey.util.Enemy;
import me.alpha432.oyvey.util.IconUtil;
import me.alpha432.oyvey.util.Title;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(modid = "zeus", name = "zeus", version = "1.0")
public class OyVey {
  public static final String MODID = "zeus";
  
  public static final String MODNAME = "Zeus";
  
  public static final String MODVER = "1.0";
  
  public static final Logger LOGGER = LogManager.getLogger("zeus");
  
  public static TimerManager timerManager;
  
  public static CommandManager commandManager;
  
  public static FriendManager friendManager;
  
  public static ModuleManager moduleManager;
  
  public static PacketManager packetManager;
  
  public static ColorManager colorManager;
  
  public static HoleManager holeManager;
  
  public static InventoryManager inventoryManager;
  
  public static PotionManager potionManager;
  
  public static RotationManager rotationManager;
  
  public static PositionManager positionManager;
  
  public static SpeedManager speedManager;
  
  public static ReloadManager reloadManager;
  
  public static FileManager fileManager;
  
  public static ConfigManager configManager;
  
  public static ServerManager serverManager;
  
  public static EventManager eventManager;
  
  public static TextManager textManager;
  
  public static CustomFont fontRenderer;
  
  public static Render3DEvent render3DEvent;
  
  public static Enemy enemy;
  
  @Instance
  public static OyVey INSTANCE;
  
  private static boolean unloaded = false;
  
  public static void load() {
    LOGGER.info("loading zeus");
    unloaded = false;
    if (reloadManager != null) {
      reloadManager.unload();
      reloadManager = null;
    } 
    textManager = new TextManager();
    commandManager = new CommandManager();
    friendManager = new FriendManager();
    moduleManager = new ModuleManager();
    rotationManager = new RotationManager();
    packetManager = new PacketManager();
    eventManager = new EventManager();
    speedManager = new SpeedManager();
    potionManager = new PotionManager();
    inventoryManager = new InventoryManager();
    serverManager = new ServerManager();
    fileManager = new FileManager();
    colorManager = new ColorManager();
    positionManager = new PositionManager();
    configManager = new ConfigManager();
    holeManager = new HoleManager();
    LOGGER.info("Managers loaded.");
    moduleManager.init();
    LOGGER.info("Modules loaded.");
    configManager.init();
    eventManager.init();
    LOGGER.info("EventManager loaded.");
    textManager.init(true);
    moduleManager.onLoad();
    LOGGER.info("zeus successfully loaded!\n");
  }
  
  public static void unload(boolean unload) {
    LOGGER.info("unloading zeus");
    if (unload) {
      reloadManager = new ReloadManager();
      reloadManager.init((commandManager != null) ? commandManager.getPrefix() : ".");
    } 
    onUnload();
    eventManager = null;
    friendManager = null;
    speedManager = null;
    holeManager = null;
    positionManager = null;
    rotationManager = null;
    configManager = null;
    commandManager = null;
    colorManager = null;
    serverManager = null;
    fileManager = null;
    potionManager = null;
    inventoryManager = null;
    moduleManager = null;
    textManager = null;
    LOGGER.info("zeus unloaded!\n");
  }
  
  public static void reload() {
    unload(false);
    load();
  }
  
  public static void onUnload() {
    if (!unloaded) {
      eventManager.onUnload();
      moduleManager.onUnload();
      configManager.saveConfig(configManager.config.replaceFirst("zeus/", ""));
      moduleManager.onUnloadPost();
      unloaded = true;
    } 
  }
  
  public static void setWindowIcon() {
    if (Util.getOSType() != Util.EnumOS.OSX)
      try(InputStream inputStream16x = Minecraft.class.getResourceAsStream("/assets/zori/icons/icon-16x.png"); 
          InputStream inputStream32x = Minecraft.class.getResourceAsStream("/assets/zori/icons/icon-32x.png")) {
        ByteBuffer[] icons = { IconUtil.INSTANCE.readImageToBuffer(inputStream16x), IconUtil.INSTANCE.readImageToBuffer(inputStream32x) };
        Display.setIcon(icons);
      } catch (Exception e) {
        LOGGER.error("Couldn't set Windows Icon", e);
      }  
  }
  
  private void setWindowsIcon() {
    setWindowIcon();
  }
  
  @EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new Title());
    load();
    setWindowsIcon();
  }
}
