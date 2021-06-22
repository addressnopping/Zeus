package me.alpha432.oyvey.features.modules.combat;

import java.util.List;
import java.util.Objects;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;

public class Quiver extends Module {
  private final Setting<Integer> tickDelay = register(new Setting("TickDelay", Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(8)));
  
  public Quiver() {
    super("Quiver", "Rotates and shoots yourself with good potion effects", Module.Category.COMBAT, true, true, false);
  }
  
  public void onUpdate() {
    if (mc.player != null) {
      if (mc.player.inventory.getCurrentItem().getItem() instanceof net.minecraft.item.ItemBow && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= ((Integer)this.tickDelay.getValue()).intValue()) {
        mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(mc.player.cameraYaw, -90.0F, mc.player.onGround));
        mc.playerController.onStoppedUsingItem((EntityPlayer)mc.player);
      } 
      List<Integer> arrowSlots = InventoryUtil.getItemInventory(Items.TIPPED_ARROW);
      if (((Integer)arrowSlots.get(0)).intValue() == -1)
        return; 
      int speedSlot = -1;
      int strengthSlot = -1;
      for (Integer slot : arrowSlots) {
        if (PotionUtils.getPotionFromItem(mc.player.inventory.getStackInSlot(slot.intValue())).getRegistryName().getPath().contains("swiftness")) {
          speedSlot = slot.intValue();
          continue;
        } 
        if (((ResourceLocation)Objects.<ResourceLocation>requireNonNull(PotionUtils.getPotionFromItem(mc.player.inventory.getStackInSlot(slot.intValue())).getRegistryName())).getPath().contains("strength"))
          strengthSlot = slot.intValue(); 
      } 
    } 
  }
  
  public void onEnable() {}
  
  private int findBow() {
    return InventoryUtil.getItemHotbar((Item)Items.BOW);
  }
}
