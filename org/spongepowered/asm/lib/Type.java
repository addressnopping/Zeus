package org.spongepowered.asm.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Type {
  public static final int VOID = 0;
  
  public static final int BOOLEAN = 1;
  
  public static final int CHAR = 2;
  
  public static final int BYTE = 3;
  
  public static final int SHORT = 4;
  
  public static final int INT = 5;
  
  public static final int FLOAT = 6;
  
  public static final int LONG = 7;
  
  public static final int DOUBLE = 8;
  
  public static final int ARRAY = 9;
  
  public static final int OBJECT = 10;
  
  public static final int METHOD = 11;
  
  public static final Type VOID_TYPE = new Type(0, null, 1443168256, 1);
  
  public static final Type BOOLEAN_TYPE = new Type(1, null, 1509950721, 1);
  
  public static final Type CHAR_TYPE = new Type(2, null, 1124075009, 1);
  
  public static final Type BYTE_TYPE = new Type(3, null, 1107297537, 1);
  
  public static final Type SHORT_TYPE = new Type(4, null, 1392510721, 1);
  
  public static final Type INT_TYPE = new Type(5, null, 1224736769, 1);
  
  public static final Type FLOAT_TYPE = new Type(6, null, 1174536705, 1);
  
  public static final Type LONG_TYPE = new Type(7, null, 1241579778, 1);
  
  public static final Type DOUBLE_TYPE = new Type(8, null, 1141048066, 1);
  
  private final int sort;
  
  private final char[] buf;
  
  private final int off;
  
  private final int len;
  
  private Type(int sort, char[] buf, int off, int len) {
    this.sort = sort;
    this.buf = buf;
    this.off = off;
    this.len = len;
  }
  
  public static Type getType(String typeDescriptor) {
    return getType(typeDescriptor.toCharArray(), 0);
  }
  
  public static Type getObjectType(String internalName) {
    char[] buf = internalName.toCharArray();
    return new Type((buf[0] == '[') ? 9 : 10, buf, 0, buf.length);
  }
  
  public static Type getMethodType(String methodDescriptor) {
    return getType(methodDescriptor.toCharArray(), 0);
  }
  
  public static Type getMethodType(Type returnType, Type... argumentTypes) {
    return getType(getMethodDescriptor(returnType, argumentTypes));
  }
  
  public static Type getType(Class<?> c) {
    if (c.isPrimitive()) {
      if (c == int.class)
        return INT_TYPE; 
      if (c == void.class)
        return VOID_TYPE; 
      if (c == boolean.class)
        return BOOLEAN_TYPE; 
      if (c == byte.class)
        return BYTE_TYPE; 
      if (c == char.class)
        return CHAR_TYPE; 
      if (c == short.class)
        return SHORT_TYPE; 
      if (c == double.class)
        return DOUBLE_TYPE; 
      if (c == float.class)
        return FLOAT_TYPE; 
      return LONG_TYPE;
    } 
    return getType(getDescriptor(c));
  }
  
  public static Type getType(Constructor<?> c) {
    return getType(getConstructorDescriptor(c));
  }
  
  public static Type getType(Method m) {
    return getType(getMethodDescriptor(m));
  }
  
  public static Type[] getArgumentTypes(String methodDescriptor) {
    char[] buf = methodDescriptor.toCharArray();
    int off = 1;
    int size = 0;
    while (true) {
      char car = buf[off++];
      if (car == ')')
        break; 
      if (car == 'L') {
        while (buf[off++] != ';');
        size++;
        continue;
      } 
      if (car != '[')
        size++; 
    } 
    Type[] args = new Type[size];
    off = 1;
    size = 0;
    while (buf[off] != ')') {
      args[size] = getType(buf, off);
      off += (args[size]).len + (((args[size]).sort == 10) ? 2 : 0);
      size++;
    } 
    return args;
  }
  
  public static Type[] getArgumentTypes(Method method) {
    Class<?>[] classes = method.getParameterTypes();
    Type[] types = new Type[classes.length];
    for (int i = classes.length - 1; i >= 0; i--)
      types[i] = getType(classes[i]); 
    return types;
  }
  
  public static Type getReturnType(String methodDescriptor) {
    char[] buf = methodDescriptor.toCharArray();
    int off = 1;
    while (true) {
      char car = buf[off++];
      if (car == ')')
        return getType(buf, off); 
      if (car == 'L')
        while (buf[off++] != ';'); 
    } 
  }
  
  public static Type getReturnType(Method method) {
    return getType(method.getReturnType());
  }
  
  public static int getArgumentsAndReturnSizes(String desc) {
    int n = 1;
    int c = 1;
    while (true) {
      char car = desc.charAt(c++);
      if (car == ')') {
        car = desc.charAt(c);
        return n << 2 | ((car == 'V') ? 0 : ((car == 'D' || car == 'J') ? 2 : 1));
      } 
      if (car == 'L') {
        while (desc.charAt(c++) != ';');
        n++;
        continue;
      } 
      if (car == '[') {
        while ((car = desc.charAt(c)) == '[')
          c++; 
        if (car == 'D' || car == 'J')
          n--; 
        continue;
      } 
      if (car == 'D' || car == 'J') {
        n += 2;
        continue;
      } 
      n++;
    } 
  }
  
  private static Type getType(char[] buf, int off) {
    int len;
    switch (buf[off]) {
      case 'V':
        return VOID_TYPE;
      case 'Z':
        return BOOLEAN_TYPE;
      case 'C':
        return CHAR_TYPE;
      case 'B':
        return BYTE_TYPE;
      case 'S':
        return SHORT_TYPE;
      case 'I':
        return INT_TYPE;
      case 'F':
        return FLOAT_TYPE;
      case 'J':
        return LONG_TYPE;
      case 'D':
        return DOUBLE_TYPE;
      case '[':
        len = 1;
        while (buf[off + len] == '[')
          len++; 
        if (buf[off + len] == 'L') {
          len++;
          while (buf[off + len] != ';')
            len++; 
        } 
        return new Type(9, buf, off, len + 1);
      case 'L':
        len = 1;
        while (buf[off + len] != ';')
          len++; 
        return new Type(10, buf, off + 1, len - 1);
    } 
    return new Type(11, buf, off, buf.length - off);
  }
  
  public int getSort() {
    return this.sort;
  }
  
  public int getDimensions() {
    int i = 1;
    while (this.buf[this.off + i] == '[')
      i++; 
    return i;
  }
  
  public Type getElementType() {
    return getType(this.buf, this.off + getDimensions());
  }
  
  public String getClassName() {
    StringBuilder sb;
    int i;
    switch (this.sort) {
      case 0:
        return "void";
      case 1:
        return "boolean";
      case 2:
        return "char";
      case 3:
        return "byte";
      case 4:
        return "short";
      case 5:
        return "int";
      case 6:
        return "float";
      case 7:
        return "long";
      case 8:
        return "double";
      case 9:
        sb = new StringBuilder(getElementType().getClassName());
        for (i = getDimensions(); i > 0; i--)
          sb.append("[]"); 
        return sb.toString();
      case 10:
        return (new String(this.buf, this.off, this.len)).replace('/', '.');
    } 
    return null;
  }
  
  public String getInternalName() {
    return new String(this.buf, this.off, this.len);
  }
  
  public Type[] getArgumentTypes() {
    return getArgumentTypes(getDescriptor());
  }
  
  public Type getReturnType() {
    return getReturnType(getDescriptor());
  }
  
  public int getArgumentsAndReturnSizes() {
    return getArgumentsAndReturnSizes(getDescriptor());
  }
  
  public String getDescriptor() {
    StringBuilder buf = new StringBuilder();
    getDescriptor(buf);
    return buf.toString();
  }
  
  public static String getMethodDescriptor(Type returnType, Type... argumentTypes) {
    StringBuilder buf = new StringBuilder();
    buf.append('(');
    for (int i = 0; i < argumentTypes.length; i++)
      argumentTypes[i].getDescriptor(buf); 
    buf.append(')');
    returnType.getDescriptor(buf);
    return buf.toString();
  }
  
  private void getDescriptor(StringBuilder buf) {
    if (this.buf == null) {
      buf.append((char)((this.off & 0xFF000000) >>> 24));
    } else if (this.sort == 10) {
      buf.append('L');
      buf.append(this.buf, this.off, this.len);
      buf.append(';');
    } else {
      buf.append(this.buf, this.off, this.len);
    } 
  }
  
  public static String getInternalName(Class<?> c) {
    return c.getName().replace('.', '/');
  }
  
  public static String getDescriptor(Class<?> c) {
    StringBuilder buf = new StringBuilder();
    getDescriptor(buf, c);
    return buf.toString();
  }
  
  public static String getConstructorDescriptor(Constructor<?> c) {
    Class<?>[] parameters = c.getParameterTypes();
    StringBuilder buf = new StringBuilder();
    buf.append('(');
    for (int i = 0; i < parameters.length; i++)
      getDescriptor(buf, parameters[i]); 
    return buf.append(")V").toString();
  }
  
  public static String getMethodDescriptor(Method m) {
    Class<?>[] parameters = m.getParameterTypes();
    StringBuilder buf = new StringBuilder();
    buf.append('(');
    for (int i = 0; i < parameters.length; i++)
      getDescriptor(buf, parameters[i]); 
    buf.append(')');
    getDescriptor(buf, m.getReturnType());
    return buf.toString();
  }
  
  private static void getDescriptor(StringBuilder buf, Class<?> c) {
    Class<?> d = c;
    while (true) {
      if (d.isPrimitive()) {
        char car;
        if (d == int.class) {
          car = 'I';
        } else if (d == void.class) {
          car = 'V';
        } else if (d == boolean.class) {
          car = 'Z';
        } else if (d == byte.class) {
          car = 'B';
        } else if (d == char.class) {
          car = 'C';
        } else if (d == short.class) {
          car = 'S';
        } else if (d == double.class) {
          car = 'D';
        } else if (d == float.class) {
          car = 'F';
        } else {
          car = 'J';
        } 
        buf.append(car);
        return;
      } 
      if (d.isArray()) {
        buf.append('[');
        d = d.getComponentType();
        continue;
      } 
      break;
    } 
    buf.append('L');
    String name = d.getName();
    int len = name.length();
    for (int i = 0; i < len; i++) {
      char car = name.charAt(i);
      buf.append((car == '.') ? 47 : car);
    } 
    buf.append(';');
  }
  
  public int getSize() {
    return (this.buf == null) ? (this.off & 0xFF) : 1;
  }
  
  public int getOpcode(int opcode) {
    if (opcode == 46 || opcode == 79)
      return opcode + ((this.buf == null) ? ((this.off & 0xFF00) >> 8) : 4); 
    return opcode + ((this.buf == null) ? ((this.off & 0xFF0000) >> 16) : 4);
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (!(o instanceof Type))
      return false; 
    Type t = (Type)o;
    if (this.sort != t.sort)
      return false; 
    if (this.sort >= 9) {
      if (this.len != t.len)
        return false; 
      for (int i = this.off, j = t.off, end = i + this.len; i < end; i++, j++) {
        if (this.buf[i] != t.buf[j])
          return false; 
      } 
    } 
    return true;
  }
  
  public int hashCode() {
    int hc = 13 * this.sort;
    if (this.sort >= 9)
      for (int i = this.off, end = i + this.len; i < end; i++)
        hc = 17 * (hc + this.buf[i]);  
    return hc;
  }
  
  public String toString() {
    return getDescriptor();
  }
}
