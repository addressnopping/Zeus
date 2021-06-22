package me.alpha432.oyvey.features.modules.misc;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Set;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.combat.PhobosCrystal;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSoundLag extends Module {
  private static final Set<SoundEvent> BLACKLIST = Sets.newHashSet((Object[])new SoundEvent[] { SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER });
  
  private static NoSoundLag instance;
  
  public Setting<Boolean> crystals = register(new Setting("Crystals", Boolean.valueOf(true)));
  
  public Setting<Boolean> armor = register(new Setting("Armor", Boolean.valueOf(true)));
  
  public Setting<Float> soundRange = register(new Setting("SoundRange", Float.valueOf(12.0F), Float.valueOf(0.0F), Float.valueOf(12.0F)));
  
  public NoSoundLag() {
    super("NoSoundLag", "Prevents Lag through sound spam.", Module.Category.MISC, true, true, false);
    instance = this;
  }
  
  public static NoSoundLag getInstance() {
    if (instance == null)
      instance = new NoSoundLag(); 
    return instance;
  }
  
  public static void removeEntities(SPacketSoundEffect packet, float range) {
    BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
    ArrayList<Entity> toRemove = new ArrayList<>();
    for (Entity entity : mc.world.loadedEntityList) {
      if (!(entity instanceof net.minecraft.entity.item.EntityEnderCrystal) || entity.getDistanceSq(pos) > MathUtil.square(range))
        continue; 
      toRemove.add(entity);
    } 
    for (Entity entity : toRemove)
      entity.setDead(); 
  }
  
  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Receive event) {
    if (event != null && event.getPacket() != null && mc.player != null && mc.world != null && event.getPacket() instanceof SPacketSoundEffect) {
      SPacketSoundEffect packet = (SPacketSoundEffect)event.getPacket();
      if (((Boolean)this.crystals.getValue()).booleanValue() && packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE && (PhobosCrystal.getInstance().isOff() || (!((Boolean)(PhobosCrystal.getInstance()).sound.getValue()).booleanValue() && (PhobosCrystal.getInstance()).threadMode.getValue() != PhobosCrystal.ThreadMode.SOUND)))
        removeEntities(packet, ((Float)this.soundRange.getValue()).floatValue()); 
      if (BLACKLIST.contains(packet.getSound()) && ((Boolean)this.armor.getValue()).booleanValue())
        event.setCanceled(true); 
    } 
  }
}
