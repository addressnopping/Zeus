package org.spongepowered.asm.lib.tree.analysis;

import java.util.List;
import org.spongepowered.asm.lib.Type;

public class SimpleVerifier extends BasicVerifier {
  private final Type currentClass;
  
  private final Type currentSuperClass;
  
  private final List<Type> currentClassInterfaces;
  
  private final boolean isInterface;
  
  private ClassLoader loader = getClass().getClassLoader();
  
  public SimpleVerifier() {
    this(null, null, false);
  }
  
  public SimpleVerifier(Type currentClass, Type currentSuperClass, boolean isInterface) {
    this(currentClass, currentSuperClass, null, isInterface);
  }
  
  public SimpleVerifier(Type currentClass, Type currentSuperClass, List<Type> currentClassInterfaces, boolean isInterface) {
    this(327680, currentClass, currentSuperClass, currentClassInterfaces, isInterface);
  }
  
  protected SimpleVerifier(int api, Type currentClass, Type currentSuperClass, List<Type> currentClassInterfaces, boolean isInterface) {
    super(api);
    this.currentClass = currentClass;
    this.currentSuperClass = currentSuperClass;
    this.currentClassInterfaces = currentClassInterfaces;
    this.isInterface = isInterface;
  }
  
  public void setClassLoader(ClassLoader loader) {
    this.loader = loader;
  }
  
  public BasicValue newValue(Type type) {
    if (type == null)
      return BasicValue.UNINITIALIZED_VALUE; 
    boolean isArray = (type.getSort() == 9);
    if (isArray)
      switch (type.getElementType().getSort()) {
        case 1:
        case 2:
        case 3:
        case 4:
          return new BasicValue(type);
      }  
    BasicValue v = super.newValue(type);
    if (BasicValue.REFERENCE_VALUE.equals(v))
      if (isArray) {
        v = newValue(type.getElementType());
        String desc = v.getType().getDescriptor();
        for (int i = 0; i < type.getDimensions(); i++)
          desc = '[' + desc; 
        v = new BasicValue(Type.getType(desc));
      } else {
        v = new BasicValue(type);
      }  
    return v;
  }
  
  protected boolean isArrayValue(BasicValue value) {
    Type t = value.getType();
    return (t != null && ("Lnull;"
      .equals(t.getDescriptor()) || t.getSort() == 9));
  }
  
  protected BasicValue getElementValue(BasicValue objectArrayValue) throws AnalyzerException {
    Type arrayType = objectArrayValue.getType();
    if (arrayType != null) {
      if (arrayType.getSort() == 9)
        return newValue(Type.getType(arrayType.getDescriptor()
              .substring(1))); 
      if ("Lnull;".equals(arrayType.getDescriptor()))
        return objectArrayValue; 
    } 
    throw new Error("Internal error");
  }
  
  protected boolean isSubTypeOf(BasicValue value, BasicValue expected) {
    Type expectedType = expected.getType();
    Type type = value.getType();
    switch (expectedType.getSort()) {
      case 5:
      case 6:
      case 7:
      case 8:
        return type.equals(expectedType);
      case 9:
      case 10:
        if ("Lnull;".equals(type.getDescriptor()))
          return true; 
        if (type.getSort() == 10 || type
          .getSort() == 9)
          return isAssignableFrom(expectedType, type); 
        return false;
    } 
    throw new Error("Internal error");
  }
  
  public BasicValue merge(BasicValue v, BasicValue w) {
    if (!v.equals(w)) {
      Type t = v.getType();
      Type u = w.getType();
      if (t != null && (t
        .getSort() == 10 || t.getSort() == 9) && 
        u != null && (u
        .getSort() == 10 || u.getSort() == 9)) {
        if ("Lnull;".equals(t.getDescriptor()))
          return w; 
        if ("Lnull;".equals(u.getDescriptor()))
          return v; 
        if (isAssignableFrom(t, u))
          return v; 
        if (isAssignableFrom(u, t))
          return w; 
        while (true) {
          if (t == null || isInterface(t))
            return BasicValue.REFERENCE_VALUE; 
          t = getSuperClass(t);
          if (isAssignableFrom(t, u))
            return newValue(t); 
        } 
      } 
      return BasicValue.UNINITIALIZED_VALUE;
    } 
    return v;
  }
  
  protected boolean isInterface(Type t) {
    if (this.currentClass != null && t.equals(this.currentClass))
      return this.isInterface; 
    return getClass(t).isInterface();
  }
  
  protected Type getSuperClass(Type t) {
    if (this.currentClass != null && t.equals(this.currentClass))
      return this.currentSuperClass; 
    Class<?> c = getClass(t).getSuperclass();
    return (c == null) ? null : Type.getType(c);
  }
  
  protected boolean isAssignableFrom(Type t, Type u) {
    if (t.equals(u))
      return true; 
    if (this.currentClass != null && t.equals(this.currentClass)) {
      if (getSuperClass(u) == null)
        return false; 
      if (this.isInterface)
        return (u.getSort() == 10 || u
          .getSort() == 9); 
      return isAssignableFrom(t, getSuperClass(u));
    } 
    if (this.currentClass != null && u.equals(this.currentClass)) {
      if (isAssignableFrom(t, this.currentSuperClass))
        return true; 
      if (this.currentClassInterfaces != null)
        for (int i = 0; i < this.currentClassInterfaces.size(); i++) {
          Type v = this.currentClassInterfaces.get(i);
          if (isAssignableFrom(t, v))
            return true; 
        }  
      return false;
    } 
    Class<?> tc = getClass(t);
    if (tc.isInterface())
      tc = Object.class; 
    return tc.isAssignableFrom(getClass(u));
  }
  
  protected Class<?> getClass(Type t) {
    try {
      if (t.getSort() == 9)
        return Class.forName(t.getDescriptor().replace('/', '.'), false, this.loader); 
      return Class.forName(t.getClassName(), false, this.loader);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e.toString());
    } 
  }
}
