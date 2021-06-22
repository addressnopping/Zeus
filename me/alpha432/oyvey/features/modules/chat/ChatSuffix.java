package me.alpha432.oyvey.features.modules.chat;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatSuffix extends Module {
  private static ChatSuffix INSTANCE = new ChatSuffix();
  
  private final Timer timer = new Timer();
  
  public Setting<Suffix> suffix = register(new Setting("Suffix", Suffix.ZEUS, "Your Suffix."));
  
  public ChatSuffix() {
    super("ChatSuffix", "Modifies your chat", Module.Category.CHAT, true, false, false);
    setInstance();
  }
  
  public static ChatSuffix getInstance() {
    if (INSTANCE == null)
      INSTANCE = new ChatSuffix(); 
    return INSTANCE;
  }
  
  private void setInstance() {
    INSTANCE = this;
  }
  
  @SubscribeEvent
  public void onPacketSend(PacketEvent.Send event) {
    if (event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
      CPacketChatMessage packet = (CPacketChatMessage)event.getPacket();
      String s = packet.getMessage();
      if (s.startsWith("/"))
        return; 
      switch ((Suffix)this.suffix.getValue()) {
        case ZEUS:
          s = s + " ⏐ ᴢᴇᴜꜱ";
          break;
      } 
      if (s.length() >= 256)
        s = s.substring(0, 256); 
      packet.message = s;
    } 
  }
  
  public enum Suffix {
    ZEUS;
  }
}
