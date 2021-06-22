package org.spongepowered.asm.transformers;

import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.service.ILegacyClassTransformer;

public abstract class TreeTransformer implements ILegacyClassTransformer {
  private ClassReader classReader;
  
  private ClassNode classNode;
  
  protected final ClassNode readClass(byte[] basicClass) {
    return readClass(basicClass, true);
  }
  
  protected final ClassNode readClass(byte[] basicClass, boolean cacheReader) {
    ClassReader classReader = new ClassReader(basicClass);
    if (cacheReader)
      this.classReader = classReader; 
    ClassNode classNode = new ClassNode();
    classReader.accept((ClassVisitor)classNode, 8);
    return classNode;
  }
  
  protected final byte[] writeClass(ClassNode classNode) {
    if (this.classReader != null && this.classNode == classNode) {
      this.classNode = null;
      ClassWriter classWriter = new MixinClassWriter(this.classReader, 3);
      this.classReader = null;
      classNode.accept((ClassVisitor)classWriter);
      return classWriter.toByteArray();
    } 
    this.classNode = null;
    ClassWriter writer = new MixinClassWriter(3);
    classNode.accept((ClassVisitor)writer);
    return writer.toByteArray();
  }
}
