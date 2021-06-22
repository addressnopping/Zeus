package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class FakeKick extends Module {
  public FakeKick() {
    super("FakeKick", "Say you were ddosed (with the clip!)", Module.Category.PLAYER, true, true, false);
  }
  
  public void onEnable() {
    Minecraft.getMinecraft().getConnection().handleDisconnect(new SPacketDisconnect((ITextComponent)new TextComponentString("Internal Exception: java.lang.NullPointerException")));
    disable();
  }
}
