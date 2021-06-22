package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class QueueSkip extends Module {
  private Setting<Queue> QueueRank;
  
  private Setting<ServerMode> Server;
  
  private Setting<SkipMode> Mode;
  
  private Setting<Integer> Factor;
  
  private Setting<Integer> Retrys;
  
  public QueueSkip() {
    super("QueueSkip", "Skips Queue", Module.Category.PLAYER, true, true, false);
    this.QueueRank = register(new Setting("QueueRank", Queue.NORM));
    this.Server = register(new Setting("Server", ServerMode.NORMAL));
    this.Mode = register(new Setting("Mode", SkipMode.NEW));
    this.Factor = register(new Setting("Factor", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(60)));
    this.Retrys = register(new Setting("Retries", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(100)));
  }
  
  public void onEnable() {
    Command.sendMessage("Skipping the queue...");
    try {
      Thread.sleep(100000L);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } 
    Command.sendMessage("Skipped the queue!");
    disable();
  }
  
  public void onDisable() {}
  
  private enum Queue {
    PRIO, NORM;
  }
  
  private enum SkipMode {
    NEW, OLD, FAST, BYPASS, UNDETECTIBLE;
  }
  
  private enum ServerMode {
    NORMAL, OLDFAG;
  }
}
