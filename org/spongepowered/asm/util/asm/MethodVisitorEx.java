package org.spongepowered.asm.util.asm;

import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.util.Bytecode;

public class MethodVisitorEx extends MethodVisitor {
  public MethodVisitorEx(MethodVisitor mv) {
    super(327680, mv);
  }
  
  public void visitConstant(byte constant) {
    if (constant > -2 && constant < 6) {
      visitInsn(Bytecode.CONSTANTS_INT[constant + 1]);
      return;
    } 
    visitIntInsn(16, constant);
  }
}
