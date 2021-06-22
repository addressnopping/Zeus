package me.alpha432.oyvey.event.events;

import me.alpha432.oyvey.event.EventStage;
import net.minecraft.util.math.BlockPos;

public class BlockDestructionEvent extends EventStage {
  BlockPos nigger;
  
  public BlockDestructionEvent(BlockPos nigger) {
    nigger = nigger;
  }
  
  public BlockPos getBlockPos() {
    return this.nigger;
  }
}
