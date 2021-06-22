package org.spongepowered.asm.lib.commons;

import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.signature.SignatureReader;
import org.spongepowered.asm.lib.signature.SignatureVisitor;
import org.spongepowered.asm.lib.signature.SignatureWriter;

public abstract class Remapper {
  public String mapDesc(String desc) {
    String s;
    int i;
    String newType;
    Type t = Type.getType(desc);
    switch (t.getSort()) {
      case 9:
        s = mapDesc(t.getElementType().getDescriptor());
        for (i = 0; i < t.getDimensions(); i++)
          s = '[' + s; 
        return s;
      case 10:
        newType = map(t.getInternalName());
        if (newType != null)
          return 'L' + newType + ';'; 
        break;
    } 
    return desc;
  }
  
  private Type mapType(Type t) {
    String s;
    int i;
    switch (t.getSort()) {
      case 9:
        s = mapDesc(t.getElementType().getDescriptor());
        for (i = 0; i < t.getDimensions(); i++)
          s = '[' + s; 
        return Type.getType(s);
      case 10:
        s = map(t.getInternalName());
        return (s != null) ? Type.getObjectType(s) : t;
      case 11:
        return Type.getMethodType(mapMethodDesc(t.getDescriptor()));
    } 
    return t;
  }
  
  public String mapType(String type) {
    if (type == null)
      return null; 
    return mapType(Type.getObjectType(type)).getInternalName();
  }
  
  public String[] mapTypes(String[] types) {
    String[] newTypes = null;
    boolean needMapping = false;
    for (int i = 0; i < types.length; i++) {
      String type = types[i];
      String newType = map(type);
      if (newType != null && newTypes == null) {
        newTypes = new String[types.length];
        if (i > 0)
          System.arraycopy(types, 0, newTypes, 0, i); 
        needMapping = true;
      } 
      if (needMapping)
        newTypes[i] = (newType == null) ? type : newType; 
    } 
    return needMapping ? newTypes : types;
  }
  
  public String mapMethodDesc(String desc) {
    if ("()V".equals(desc))
      return desc; 
    Type[] args = Type.getArgumentTypes(desc);
    StringBuilder sb = new StringBuilder("(");
    for (int i = 0; i < args.length; i++)
      sb.append(mapDesc(args[i].getDescriptor())); 
    Type returnType = Type.getReturnType(desc);
    if (returnType == Type.VOID_TYPE) {
      sb.append(")V");
      return sb.toString();
    } 
    sb.append(')').append(mapDesc(returnType.getDescriptor()));
    return sb.toString();
  }
  
  public Object mapValue(Object value) {
    if (value instanceof Type)
      return mapType((Type)value); 
    if (value instanceof Handle) {
      Handle h = (Handle)value;
      return new Handle(h.getTag(), mapType(h.getOwner()), mapMethodName(h
            .getOwner(), h.getName(), h.getDesc()), 
          mapMethodDesc(h.getDesc()), h.isInterface());
    } 
    return value;
  }
  
  public String mapSignature(String signature, boolean typeSignature) {
    if (signature == null)
      return null; 
    SignatureReader r = new SignatureReader(signature);
    SignatureWriter w = new SignatureWriter();
    SignatureVisitor a = createSignatureRemapper((SignatureVisitor)w);
    if (typeSignature) {
      r.acceptType(a);
    } else {
      r.accept(a);
    } 
    return w.toString();
  }
  
  @Deprecated
  protected SignatureVisitor createRemappingSignatureAdapter(SignatureVisitor v) {
    return new SignatureRemapper(v, this);
  }
  
  protected SignatureVisitor createSignatureRemapper(SignatureVisitor v) {
    return createRemappingSignatureAdapter(v);
  }
  
  public String mapMethodName(String owner, String name, String desc) {
    return name;
  }
  
  public String mapInvokeDynamicMethodName(String name, String desc) {
    return name;
  }
  
  public String mapFieldName(String owner, String name, String desc) {
    return name;
  }
  
  public String map(String typeName) {
    return typeName;
  }
}
