package me.alpha432.oyvey.features.modules.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class FakePlayer extends Module {
  public Setting<String> fakename;
  
  private EntityOtherPlayerMP clonedPlayer;
  
  public FakePlayer() {
    super("FakePlayer", "Spawns a literal fake player", Module.Category.PLAYER, false, true, false);
    this.fakename = register(new Setting("Name", "popbob"));
  }
  
  public void onEnable() {
    Command.sendMessage("FakePlayer by the name of " + this.fakename.getValueAsString() + " has been spawned!");
    if (mc.player == null || mc.player.isDead) {
      disable();
      return;
    } 
    this.clonedPlayer = new EntityOtherPlayerMP((World)mc.world, new GameProfile(UUID.fromString("0f75a81d-70e5-43c5-b892-f33c524284f2"), this.fakename.getValueAsString()));
    this.clonedPlayer.copyLocationAndAnglesFrom((Entity)mc.player);
    this.clonedPlayer.rotationYawHead = mc.player.rotationYawHead;
    this.clonedPlayer.rotationYaw = mc.player.rotationYaw;
    this.clonedPlayer.rotationPitch = mc.player.rotationPitch;
    this.clonedPlayer.setGameType(GameType.SURVIVAL);
    this.clonedPlayer.setHealth(20.0F);
    mc.world.addEntityToWorld(-12345, (Entity)this.clonedPlayer);
    this.clonedPlayer.onLivingUpdate();
  }
  
  public void onDisable() {
    if (mc.world != null)
      mc.world.removeEntityFromWorld(-12345); 
  }
  
  @SubscribeEvent
  public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    if (isEnabled())
      disable(); 
  }
}
