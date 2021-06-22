package org.spongepowered.tools.obfuscation.mirror;

import com.google.common.base.Strings;
import javax.lang.model.element.ExecutableElement;
import org.spongepowered.asm.obfuscation.mapping.IMapping;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.mirror.mapping.ResolvableMappingMethod;

public class MethodHandle extends MemberHandle<MappingMethod> {
  private final ExecutableElement element;
  
  private final TypeHandle ownerHandle;
  
  public MethodHandle(TypeHandle owner, ExecutableElement element) {
    this(owner, element, TypeUtils.getName(element), TypeUtils.getDescriptor(element));
  }
  
  public MethodHandle(TypeHandle owner, String name, String desc) {
    this(owner, (ExecutableElement)null, name, desc);
  }
  
  private MethodHandle(TypeHandle owner, ExecutableElement element, String name, String desc) {
    super((owner != null) ? owner.getName() : null, name, desc);
    this.element = element;
    this.ownerHandle = owner;
  }
  
  public boolean isImaginary() {
    return (this.element == null);
  }
  
  public ExecutableElement getElement() {
    return this.element;
  }
  
  public Visibility getVisibility() {
    return TypeUtils.getVisibility(this.element);
  }
  
  public MappingMethod asMapping(boolean includeOwner) {
    if (includeOwner) {
      if (this.ownerHandle != null)
        return (MappingMethod)new ResolvableMappingMethod(this.ownerHandle, getName(), getDesc()); 
      return new MappingMethod(getOwner(), getName(), getDesc());
    } 
    return new MappingMethod(null, getName(), getDesc());
  }
  
  public String toString() {
    String owner = (getOwner() != null) ? ("L" + getOwner() + ";") : "";
    String name = Strings.nullToEmpty(getName());
    String desc = Strings.nullToEmpty(getDesc());
    return String.format("%s%s%s", new Object[] { owner, name, desc });
  }
}
