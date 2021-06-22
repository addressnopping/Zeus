package org.spongepowered.tools.obfuscation.mirror.mapping;

import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeUtils;

public final class ResolvableMappingMethod extends MappingMethod {
  private final TypeHandle ownerHandle;
  
  public ResolvableMappingMethod(TypeHandle owner, String name, String desc) {
    super(owner.getName(), name, desc);
    this.ownerHandle = owner;
  }
  
  public MappingMethod getSuper() {
    if (this.ownerHandle == null)
      return super.getSuper(); 
    String name = getSimpleName();
    String desc = getDesc();
    String signature = TypeUtils.getJavaSignature(desc);
    TypeHandle superClass = this.ownerHandle.getSuperclass();
    if (superClass != null)
      if (superClass.findMethod(name, signature) != null)
        return superClass.getMappingMethod(name, desc);  
    for (TypeHandle iface : this.ownerHandle.getInterfaces()) {
      if (iface.findMethod(name, signature) != null)
        return iface.getMappingMethod(name, desc); 
    } 
    if (superClass != null)
      return superClass.getMappingMethod(name, desc).getSuper(); 
    return super.getSuper();
  }
  
  public MappingMethod move(TypeHandle newOwner) {
    return new ResolvableMappingMethod(newOwner, getSimpleName(), getDesc());
  }
  
  public MappingMethod remap(String newName) {
    return new ResolvableMappingMethod(this.ownerHandle, newName, getDesc());
  }
  
  public MappingMethod transform(String newDesc) {
    return new ResolvableMappingMethod(this.ownerHandle, getSimpleName(), newDesc);
  }
  
  public MappingMethod copy() {
    return new ResolvableMappingMethod(this.ownerHandle, getSimpleName(), getDesc());
  }
}
