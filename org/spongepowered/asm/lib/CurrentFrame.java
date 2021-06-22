package org.spongepowered.asm.lib;

class CurrentFrame extends Frame {
  void execute(int opcode, int arg, ClassWriter cw, Item item) {
    super.execute(opcode, arg, cw, item);
    Frame successor = new Frame();
    merge(cw, successor, 0);
    set(successor);
    this.owner.inputStackTop = 0;
  }
}
