package org.spongepowered.asm.mixin.transformer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.struct.MemberRef;

abstract class ClassContext {
  private final Set<ClassInfo.Method> upgradedMethods = new HashSet<ClassInfo.Method>();
  
  abstract String getClassRef();
  
  abstract ClassNode getClassNode();
  
  abstract ClassInfo getClassInfo();
  
  void addUpgradedMethod(MethodNode method) {
    ClassInfo.Method md = getClassInfo().findMethod(method);
    if (md == null)
      throw new IllegalStateException("Meta method for " + method.name + " not located in " + this); 
    this.upgradedMethods.add(md);
  }
  
  protected void upgradeMethods() {
    for (MethodNode method : (getClassNode()).methods)
      upgradeMethod(method); 
  }
  
  private void upgradeMethod(MethodNode method) {
    for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext(); ) {
      AbstractInsnNode insn = iter.next();
      if (!(insn instanceof MethodInsnNode))
        continue; 
      MemberRef.Method method1 = new MemberRef.Method((MethodInsnNode)insn);
      if (method1.getOwner().equals(getClassRef())) {
        ClassInfo.Method md = getClassInfo().findMethod(method1.getName(), method1.getDesc(), 10);
        upgradeMethodRef(method, (MemberRef)method1, md);
      } 
    } 
  }
  
  protected void upgradeMethodRef(MethodNode containingMethod, MemberRef methodRef, ClassInfo.Method method) {
    if (methodRef.getOpcode() != 183)
      return; 
    if (this.upgradedMethods.contains(method))
      methodRef.setOpcode(182); 
  }
}
