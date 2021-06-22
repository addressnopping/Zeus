package org.spongepowered.asm.obfuscation.mapping;

public interface IMapping<TMapping> {
  Type getType();
  
  TMapping move(String paramString);
  
  TMapping remap(String paramString);
  
  TMapping transform(String paramString);
  
  TMapping copy();
  
  String getName();
  
  String getSimpleName();
  
  String getOwner();
  
  String getDesc();
  
  TMapping getSuper();
  
  String serialise();
  
  public enum Type {
    FIELD, METHOD, CLASS, PACKAGE;
  }
}
