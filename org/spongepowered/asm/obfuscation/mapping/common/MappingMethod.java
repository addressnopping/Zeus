package org.spongepowered.asm.obfuscation.mapping.common;

import com.google.common.base.Objects;
import org.spongepowered.asm.obfuscation.mapping.IMapping;

public class MappingMethod implements IMapping<MappingMethod> {
  private final String owner;
  
  private final String name;
  
  private final String desc;
  
  public MappingMethod(String fullyQualifiedName, String desc) {
    this(getOwnerFromName(fullyQualifiedName), getBaseName(fullyQualifiedName), desc);
  }
  
  public MappingMethod(String owner, String simpleName, String desc) {
    this.owner = owner;
    this.name = simpleName;
    this.desc = desc;
  }
  
  public IMapping.Type getType() {
    return IMapping.Type.METHOD;
  }
  
  public String getName() {
    if (this.name == null)
      return null; 
    return ((this.owner != null) ? (this.owner + "/") : "") + this.name;
  }
  
  public String getSimpleName() {
    return this.name;
  }
  
  public String getOwner() {
    return this.owner;
  }
  
  public String getDesc() {
    return this.desc;
  }
  
  public MappingMethod getSuper() {
    return null;
  }
  
  public boolean isConstructor() {
    return "<init>".equals(this.name);
  }
  
  public MappingMethod move(String newOwner) {
    return new MappingMethod(newOwner, getSimpleName(), getDesc());
  }
  
  public MappingMethod remap(String newName) {
    return new MappingMethod(getOwner(), newName, getDesc());
  }
  
  public MappingMethod transform(String newDesc) {
    return new MappingMethod(getOwner(), getSimpleName(), newDesc);
  }
  
  public MappingMethod copy() {
    return new MappingMethod(getOwner(), getSimpleName(), getDesc());
  }
  
  public MappingMethod addPrefix(String prefix) {
    String simpleName = getSimpleName();
    if (simpleName == null || simpleName.startsWith(prefix))
      return this; 
    return new MappingMethod(getOwner(), prefix + simpleName, getDesc());
  }
  
  public int hashCode() {
    return Objects.hashCode(new Object[] { getName(), getDesc() });
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj instanceof MappingMethod)
      return (Objects.equal(this.name, ((MappingMethod)obj).name) && Objects.equal(this.desc, ((MappingMethod)obj).desc)); 
    return false;
  }
  
  public String serialise() {
    return toString();
  }
  
  public String toString() {
    String desc = getDesc();
    return String.format("%s%s%s", new Object[] { getName(), (desc != null) ? " " : "", (desc != null) ? desc : "" });
  }
  
  private static String getBaseName(String name) {
    if (name == null)
      return null; 
    int pos = name.lastIndexOf('/');
    return (pos > -1) ? name.substring(pos + 1) : name;
  }
  
  private static String getOwnerFromName(String name) {
    if (name == null)
      return null; 
    int pos = name.lastIndexOf('/');
    return (pos > -1) ? name.substring(0, pos) : null;
  }
}
