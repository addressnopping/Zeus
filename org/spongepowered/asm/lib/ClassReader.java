package org.spongepowered.asm.lib;

import java.io.IOException;
import java.io.InputStream;

public class ClassReader {
  static final boolean SIGNATURES = true;
  
  static final boolean ANNOTATIONS = true;
  
  static final boolean FRAMES = true;
  
  static final boolean WRITER = true;
  
  static final boolean RESIZE = true;
  
  public static final int SKIP_CODE = 1;
  
  public static final int SKIP_DEBUG = 2;
  
  public static final int SKIP_FRAMES = 4;
  
  public static final int EXPAND_FRAMES = 8;
  
  static final int EXPAND_ASM_INSNS = 256;
  
  public final byte[] b;
  
  private final int[] items;
  
  private final String[] strings;
  
  private final int maxStringLength;
  
  public final int header;
  
  public ClassReader(byte[] b) {
    this(b, 0, b.length);
  }
  
  public ClassReader(byte[] b, int off, int len) {
    this.b = b;
    if (readShort(off + 6) > 52)
      throw new IllegalArgumentException(); 
    this.items = new int[readUnsignedShort(off + 8)];
    int n = this.items.length;
    this.strings = new String[n];
    int max = 0;
    int index = off + 10;
    for (int i = 1; i < n; i++) {
      int size;
      this.items[i] = index + 1;
      switch (b[index]) {
        case 3:
        case 4:
        case 9:
        case 10:
        case 11:
        case 12:
        case 18:
          size = 5;
          break;
        case 5:
        case 6:
          size = 9;
          i++;
          break;
        case 1:
          size = 3 + readUnsignedShort(index + 1);
          if (size > max)
            max = size; 
          break;
        case 15:
          size = 4;
          break;
        default:
          size = 3;
          break;
      } 
      index += size;
    } 
    this.maxStringLength = max;
    this.header = index;
  }
  
  public int getAccess() {
    return readUnsignedShort(this.header);
  }
  
  public String getClassName() {
    return readClass(this.header + 2, new char[this.maxStringLength]);
  }
  
  public String getSuperName() {
    return readClass(this.header + 4, new char[this.maxStringLength]);
  }
  
  public String[] getInterfaces() {
    int index = this.header + 6;
    int n = readUnsignedShort(index);
    String[] interfaces = new String[n];
    if (n > 0) {
      char[] buf = new char[this.maxStringLength];
      for (int i = 0; i < n; i++) {
        index += 2;
        interfaces[i] = readClass(index, buf);
      } 
    } 
    return interfaces;
  }
  
  void copyPool(ClassWriter classWriter) {
    char[] buf = new char[this.maxStringLength];
    int ll = this.items.length;
    Item[] items2 = new Item[ll];
    for (int i = 1; i < ll; i++) {
      int nameType;
      String s;
      int fieldOrMethodRef, index = this.items[i];
      int tag = this.b[index - 1];
      Item item = new Item(i);
      switch (tag) {
        case 9:
        case 10:
        case 11:
          nameType = this.items[readUnsignedShort(index + 2)];
          item.set(tag, readClass(index, buf), readUTF8(nameType, buf), 
              readUTF8(nameType + 2, buf));
          break;
        case 3:
          item.set(readInt(index));
          break;
        case 4:
          item.set(Float.intBitsToFloat(readInt(index)));
          break;
        case 12:
          item.set(tag, readUTF8(index, buf), readUTF8(index + 2, buf), null);
          break;
        case 5:
          item.set(readLong(index));
          i++;
          break;
        case 6:
          item.set(Double.longBitsToDouble(readLong(index)));
          i++;
          break;
        case 1:
          s = this.strings[i];
          if (s == null) {
            index = this.items[i];
            s = this.strings[i] = readUTF(index + 2, 
                readUnsignedShort(index), buf);
          } 
          item.set(tag, s, null, null);
          break;
        case 15:
          fieldOrMethodRef = this.items[readUnsignedShort(index + 1)];
          nameType = this.items[readUnsignedShort(fieldOrMethodRef + 2)];
          item.set(20 + readByte(index), 
              readClass(fieldOrMethodRef, buf), 
              readUTF8(nameType, buf), readUTF8(nameType + 2, buf));
          break;
        case 18:
          if (classWriter.bootstrapMethods == null)
            copyBootstrapMethods(classWriter, items2, buf); 
          nameType = this.items[readUnsignedShort(index + 2)];
          item.set(readUTF8(nameType, buf), readUTF8(nameType + 2, buf), 
              readUnsignedShort(index));
          break;
        default:
          item.set(tag, readUTF8(index, buf), null, null);
          break;
      } 
      int index2 = item.hashCode % items2.length;
      item.next = items2[index2];
      items2[index2] = item;
    } 
    int off = this.items[1] - 1;
    classWriter.pool.putByteArray(this.b, off, this.header - off);
    classWriter.items = items2;
    classWriter.threshold = (int)(0.75D * ll);
    classWriter.index = ll;
  }
  
  private void copyBootstrapMethods(ClassWriter classWriter, Item[] items, char[] c) {
    int u = getAttributes();
    boolean found = false;
    for (int i = readUnsignedShort(u); i > 0; i--) {
      String attrName = readUTF8(u + 2, c);
      if ("BootstrapMethods".equals(attrName)) {
        found = true;
        break;
      } 
      u += 6 + readInt(u + 4);
    } 
    if (!found)
      return; 
    int boostrapMethodCount = readUnsignedShort(u + 8);
    for (int j = 0, v = u + 10; j < boostrapMethodCount; j++) {
      int position = v - u - 10;
      int hashCode = readConst(readUnsignedShort(v), c).hashCode();
      for (int k = readUnsignedShort(v + 2); k > 0; k--) {
        hashCode ^= readConst(readUnsignedShort(v + 4), c).hashCode();
        v += 2;
      } 
      v += 4;
      Item item = new Item(j);
      item.set(position, hashCode & Integer.MAX_VALUE);
      int index = item.hashCode % items.length;
      item.next = items[index];
      items[index] = item;
    } 
    int attrSize = readInt(u + 4);
    ByteVector bootstrapMethods = new ByteVector(attrSize + 62);
    bootstrapMethods.putByteArray(this.b, u + 10, attrSize - 2);
    classWriter.bootstrapMethodsCount = boostrapMethodCount;
    classWriter.bootstrapMethods = bootstrapMethods;
  }
  
  public ClassReader(InputStream is) throws IOException {
    this(readClass(is, false));
  }
  
  public ClassReader(String name) throws IOException {
    this(readClass(
          ClassLoader.getSystemResourceAsStream(name.replace('.', '/') + ".class"), true));
  }
  
  private static byte[] readClass(InputStream is, boolean close) throws IOException {
    if (is == null)
      throw new IOException("Class not found"); 
    try {
      byte[] b = new byte[is.available()];
      int len = 0;
      while (true) {
        int n = is.read(b, len, b.length - len);
        if (n == -1) {
          if (len < b.length) {
            byte[] c = new byte[len];
            System.arraycopy(b, 0, c, 0, len);
            b = c;
          } 
          return b;
        } 
        len += n;
        if (len == b.length) {
          int last = is.read();
          if (last < 0)
            return b; 
          byte[] c = new byte[b.length + 1000];
          System.arraycopy(b, 0, c, 0, len);
          c[len++] = (byte)last;
          b = c;
        } 
      } 
    } finally {
      if (close)
        is.close(); 
    } 
  }
  
  public void accept(ClassVisitor classVisitor, int flags) {
    accept(classVisitor, new Attribute[0], flags);
  }
  
  public void accept(ClassVisitor classVisitor, Attribute[] attrs, int flags) {
    int u = this.header;
    char[] c = new char[this.maxStringLength];
    Context context = new Context();
    context.attrs = attrs;
    context.flags = flags;
    context.buffer = c;
    int access = readUnsignedShort(u);
    String name = readClass(u + 2, c);
    String superClass = readClass(u + 4, c);
    String[] interfaces = new String[readUnsignedShort(u + 6)];
    u += 8;
    for (int i = 0; i < interfaces.length; i++) {
      interfaces[i] = readClass(u, c);
      u += 2;
    } 
    String signature = null;
    String sourceFile = null;
    String sourceDebug = null;
    String enclosingOwner = null;
    String enclosingName = null;
    String enclosingDesc = null;
    int anns = 0;
    int ianns = 0;
    int tanns = 0;
    int itanns = 0;
    int innerClasses = 0;
    Attribute attributes = null;
    u = getAttributes();
    int j;
    for (j = readUnsignedShort(u); j > 0; j--) {
      String attrName = readUTF8(u + 2, c);
      if ("SourceFile".equals(attrName)) {
        sourceFile = readUTF8(u + 8, c);
      } else if ("InnerClasses".equals(attrName)) {
        innerClasses = u + 8;
      } else if ("EnclosingMethod".equals(attrName)) {
        enclosingOwner = readClass(u + 8, c);
        int item = readUnsignedShort(u + 10);
        if (item != 0) {
          enclosingName = readUTF8(this.items[item], c);
          enclosingDesc = readUTF8(this.items[item] + 2, c);
        } 
      } else if ("Signature".equals(attrName)) {
        signature = readUTF8(u + 8, c);
      } else if ("RuntimeVisibleAnnotations"
        .equals(attrName)) {
        anns = u + 8;
      } else if ("RuntimeVisibleTypeAnnotations"
        .equals(attrName)) {
        tanns = u + 8;
      } else if ("Deprecated".equals(attrName)) {
        access |= 0x20000;
      } else if ("Synthetic".equals(attrName)) {
        access |= 0x41000;
      } else if ("SourceDebugExtension".equals(attrName)) {
        int len = readInt(u + 4);
        sourceDebug = readUTF(u + 8, len, new char[len]);
      } else if ("RuntimeInvisibleAnnotations"
        .equals(attrName)) {
        ianns = u + 8;
      } else if ("RuntimeInvisibleTypeAnnotations"
        .equals(attrName)) {
        itanns = u + 8;
      } else if ("BootstrapMethods".equals(attrName)) {
        int[] bootstrapMethods = new int[readUnsignedShort(u + 8)];
        for (int k = 0, v = u + 10; k < bootstrapMethods.length; k++) {
          bootstrapMethods[k] = v;
          v += 2 + readUnsignedShort(v + 2) << 1;
        } 
        context.bootstrapMethods = bootstrapMethods;
      } else {
        Attribute attr = readAttribute(attrs, attrName, u + 8, 
            readInt(u + 4), c, -1, null);
        if (attr != null) {
          attr.next = attributes;
          attributes = attr;
        } 
      } 
      u += 6 + readInt(u + 4);
    } 
    classVisitor.visit(readInt(this.items[1] - 7), access, name, signature, superClass, interfaces);
    if ((flags & 0x2) == 0 && (sourceFile != null || sourceDebug != null))
      classVisitor.visitSource(sourceFile, sourceDebug); 
    if (enclosingOwner != null)
      classVisitor.visitOuterClass(enclosingOwner, enclosingName, enclosingDesc); 
    if (anns != 0) {
      int v;
      for (j = readUnsignedShort(anns), v = anns + 2; j > 0; j--)
        v = readAnnotationValues(v + 2, c, true, classVisitor
            .visitAnnotation(readUTF8(v, c), true)); 
    } 
    if (ianns != 0) {
      int v;
      for (j = readUnsignedShort(ianns), v = ianns + 2; j > 0; j--)
        v = readAnnotationValues(v + 2, c, true, classVisitor
            .visitAnnotation(readUTF8(v, c), false)); 
    } 
    if (tanns != 0) {
      int v;
      for (j = readUnsignedShort(tanns), v = tanns + 2; j > 0; j--) {
        v = readAnnotationTarget(context, v);
        v = readAnnotationValues(v + 2, c, true, classVisitor
            .visitTypeAnnotation(context.typeRef, context.typePath, 
              readUTF8(v, c), true));
      } 
    } 
    if (itanns != 0) {
      int v;
      for (j = readUnsignedShort(itanns), v = itanns + 2; j > 0; j--) {
        v = readAnnotationTarget(context, v);
        v = readAnnotationValues(v + 2, c, true, classVisitor
            .visitTypeAnnotation(context.typeRef, context.typePath, 
              readUTF8(v, c), false));
      } 
    } 
    while (attributes != null) {
      Attribute attr = attributes.next;
      attributes.next = null;
      classVisitor.visitAttribute(attributes);
      attributes = attr;
    } 
    if (innerClasses != 0) {
      int v = innerClasses + 2;
      for (int k = readUnsignedShort(innerClasses); k > 0; k--) {
        classVisitor.visitInnerClass(readClass(v, c), 
            readClass(v + 2, c), readUTF8(v + 4, c), 
            readUnsignedShort(v + 6));
        v += 8;
      } 
    } 
    u = this.header + 10 + 2 * interfaces.length;
    for (j = readUnsignedShort(u - 2); j > 0; j--)
      u = readField(classVisitor, context, u); 
    u += 2;
    for (j = readUnsignedShort(u - 2); j > 0; j--)
      u = readMethod(classVisitor, context, u); 
    classVisitor.visitEnd();
  }
  
  private int readField(ClassVisitor classVisitor, Context context, int u) {
    char[] c = context.buffer;
    int access = readUnsignedShort(u);
    String name = readUTF8(u + 2, c);
    String desc = readUTF8(u + 4, c);
    u += 6;
    String signature = null;
    int anns = 0;
    int ianns = 0;
    int tanns = 0;
    int itanns = 0;
    Object value = null;
    Attribute attributes = null;
    for (int i = readUnsignedShort(u); i > 0; i--) {
      String attrName = readUTF8(u + 2, c);
      if ("ConstantValue".equals(attrName)) {
        int item = readUnsignedShort(u + 8);
        value = (item == 0) ? null : readConst(item, c);
      } else if ("Signature".equals(attrName)) {
        signature = readUTF8(u + 8, c);
      } else if ("Deprecated".equals(attrName)) {
        access |= 0x20000;
      } else if ("Synthetic".equals(attrName)) {
        access |= 0x41000;
      } else if ("RuntimeVisibleAnnotations"
        .equals(attrName)) {
        anns = u + 8;
      } else if ("RuntimeVisibleTypeAnnotations"
        .equals(attrName)) {
        tanns = u + 8;
      } else if ("RuntimeInvisibleAnnotations"
        .equals(attrName)) {
        ianns = u + 8;
      } else if ("RuntimeInvisibleTypeAnnotations"
        .equals(attrName)) {
        itanns = u + 8;
      } else {
        Attribute attr = readAttribute(context.attrs, attrName, u + 8, 
            readInt(u + 4), c, -1, null);
        if (attr != null) {
          attr.next = attributes;
          attributes = attr;
        } 
      } 
      u += 6 + readInt(u + 4);
    } 
    u += 2;
    FieldVisitor fv = classVisitor.visitField(access, name, desc, signature, value);
    if (fv == null)
      return u; 
    if (anns != 0)
      for (int j = readUnsignedShort(anns), v = anns + 2; j > 0; j--)
        v = readAnnotationValues(v + 2, c, true, fv
            .visitAnnotation(readUTF8(v, c), true));  
    if (ianns != 0)
      for (int j = readUnsignedShort(ianns), v = ianns + 2; j > 0; j--)
        v = readAnnotationValues(v + 2, c, true, fv
            .visitAnnotation(readUTF8(v, c), false));  
    if (tanns != 0)
      for (int j = readUnsignedShort(tanns), v = tanns + 2; j > 0; j--) {
        v = readAnnotationTarget(context, v);
        v = readAnnotationValues(v + 2, c, true, fv
            .visitTypeAnnotation(context.typeRef, context.typePath, 
              readUTF8(v, c), true));
      }  
    if (itanns != 0)
      for (int j = readUnsignedShort(itanns), v = itanns + 2; j > 0; j--) {
        v = readAnnotationTarget(context, v);
        v = readAnnotationValues(v + 2, c, true, fv
            .visitTypeAnnotation(context.typeRef, context.typePath, 
              readUTF8(v, c), false));
      }  
    while (attributes != null) {
      Attribute attr = attributes.next;
      attributes.next = null;
      fv.visitAttribute(attributes);
      attributes = attr;
    } 
    fv.visitEnd();
    return u;
  }
  
  private int readMethod(ClassVisitor classVisitor, Context context, int u) {
    char[] c = context.buffer;
    context.access = readUnsignedShort(u);
    context.name = readUTF8(u + 2, c);
    context.desc = readUTF8(u + 4, c);
    u += 6;
    int code = 0;
    int exception = 0;
    String[] exceptions = null;
    String signature = null;
    int methodParameters = 0;
    int anns = 0;
    int ianns = 0;
    int tanns = 0;
    int itanns = 0;
    int dann = 0;
    int mpanns = 0;
    int impanns = 0;
    int firstAttribute = u;
    Attribute attributes = null;
    for (int i = readUnsignedShort(u); i > 0; i--) {
      String attrName = readUTF8(u + 2, c);
      if ("Code".equals(attrName)) {
        if ((context.flags & 0x1) == 0)
          code = u + 8; 
      } else if ("Exceptions".equals(attrName)) {
        exceptions = new String[readUnsignedShort(u + 8)];
        exception = u + 10;
        for (int j = 0; j < exceptions.length; j++) {
          exceptions[j] = readClass(exception, c);
          exception += 2;
        } 
      } else if ("Signature".equals(attrName)) {
        signature = readUTF8(u + 8, c);
      } else if ("Deprecated".equals(attrName)) {
        context.access |= 0x20000;
      } else if ("RuntimeVisibleAnnotations"
        .equals(attrName)) {
        anns = u + 8;
      } else if ("RuntimeVisibleTypeAnnotations"
        .equals(attrName)) {
        tanns = u + 8;
      } else if ("AnnotationDefault".equals(attrName)) {
        dann = u + 8;
      } else if ("Synthetic".equals(attrName)) {
        context.access |= 0x41000;
      } else if ("RuntimeInvisibleAnnotations"
        .equals(attrName)) {
        ianns = u + 8;
      } else if ("RuntimeInvisibleTypeAnnotations"
        .equals(attrName)) {
        itanns = u + 8;
      } else if ("RuntimeVisibleParameterAnnotations"
        .equals(attrName)) {
        mpanns = u + 8;
      } else if ("RuntimeInvisibleParameterAnnotations"
        .equals(attrName)) {
        impanns = u + 8;
      } else if ("MethodParameters".equals(attrName)) {
        methodParameters = u + 8;
      } else {
        Attribute attr = readAttribute(context.attrs, attrName, u + 8, 
            readInt(u + 4), c, -1, null);
        if (attr != null) {
          attr.next = attributes;
          attributes = attr;
        } 
      } 
      u += 6 + readInt(u + 4);
    } 
    u += 2;
    MethodVisitor mv = classVisitor.visitMethod(context.access, context.name, context.desc, signature, exceptions);
    if (mv == null)
      return u; 
    if (mv instanceof MethodWriter) {
      MethodWriter mw = (MethodWriter)mv;
      if (mw.cw.cr == this && signature == mw.signature) {
        boolean sameExceptions = false;
        if (exceptions == null) {
          sameExceptions = (mw.exceptionCount == 0);
        } else if (exceptions.length == mw.exceptionCount) {
          sameExceptions = true;
          for (int j = exceptions.length - 1; j >= 0; j--) {
            exception -= 2;
            if (mw.exceptions[j] != readUnsignedShort(exception)) {
              sameExceptions = false;
              break;
            } 
          } 
        } 
        if (sameExceptions) {
          mw.classReaderOffset = firstAttribute;
          mw.classReaderLength = u - firstAttribute;
          return u;
        } 
      } 
    } 
    if (methodParameters != 0) {
      int v;
      for (int j = this.b[methodParameters] & 0xFF; j > 0; j--, v += 4)
        mv.visitParameter(readUTF8(v, c), readUnsignedShort(v + 2)); 
    } 
    if (dann != 0) {
      AnnotationVisitor dv = mv.visitAnnotationDefault();
      readAnnotationValue(dann, c, null, dv);
      if (dv != null)
        dv.visitEnd(); 
    } 
    if (anns != 0)
      for (int j = readUnsignedShort(anns), v = anns + 2; j > 0; j--)
        v = readAnnotationValues(v + 2, c, true, mv
            .visitAnnotation(readUTF8(v, c), true));  
    if (ianns != 0)
      for (int j = readUnsignedShort(ianns), v = ianns + 2; j > 0; j--)
        v = readAnnotationValues(v + 2, c, true, mv
            .visitAnnotation(readUTF8(v, c), false));  
    if (tanns != 0)
      for (int j = readUnsignedShort(tanns), v = tanns + 2; j > 0; j--) {
        v = readAnnotationTarget(context, v);
        v = readAnnotationValues(v + 2, c, true, mv
            .visitTypeAnnotation(context.typeRef, context.typePath, 
              readUTF8(v, c), true));
      }  
    if (itanns != 0)
      for (int j = readUnsignedShort(itanns), v = itanns + 2; j > 0; j--) {
        v = readAnnotationTarget(context, v);
        v = readAnnotationValues(v + 2, c, true, mv
            .visitTypeAnnotation(context.typeRef, context.typePath, 
              readUTF8(v, c), false));
      }  
    if (mpanns != 0)
      readParameterAnnotations(mv, context, mpanns, true); 
    if (impanns != 0)
      readParameterAnnotations(mv, context, impanns, false); 
    while (attributes != null) {
      Attribute attr = attributes.next;
      attributes.next = null;
      mv.visitAttribute(attributes);
      attributes = attr;
    } 
    if (code != 0) {
      mv.visitCode();
      readCode(mv, context, code);
    } 
    mv.visitEnd();
    return u;
  }
  
  private void readCode(MethodVisitor mv, Context context, int u) {
    byte[] b = this.b;
    char[] c = context.buffer;
    int maxStack = readUnsignedShort(u);
    int maxLocals = readUnsignedShort(u + 2);
    int codeLength = readInt(u + 4);
    u += 8;
    int codeStart = u;
    int codeEnd = u + codeLength;
    Label[] labels = context.labels = new Label[codeLength + 2];
    readLabel(codeLength + 1, labels);
    while (u < codeEnd) {
      int k, offset = u - codeStart;
      int opcode = b[u] & 0xFF;
      switch (ClassWriter.TYPE[opcode]) {
        case 0:
        case 4:
          u++;
          continue;
        case 9:
          readLabel(offset + readShort(u + 1), labels);
          u += 3;
          continue;
        case 18:
          readLabel(offset + readUnsignedShort(u + 1), labels);
          u += 3;
          continue;
        case 10:
          readLabel(offset + readInt(u + 1), labels);
          u += 5;
          continue;
        case 17:
          opcode = b[u + 1] & 0xFF;
          if (opcode == 132) {
            u += 6;
            continue;
          } 
          u += 4;
          continue;
        case 14:
          u = u + 4 - (offset & 0x3);
          readLabel(offset + readInt(u), labels);
          for (k = readInt(u + 8) - readInt(u + 4) + 1; k > 0; k--) {
            readLabel(offset + readInt(u + 12), labels);
            u += 4;
          } 
          u += 12;
          continue;
        case 15:
          u = u + 4 - (offset & 0x3);
          readLabel(offset + readInt(u), labels);
          for (k = readInt(u + 4); k > 0; k--) {
            readLabel(offset + readInt(u + 12), labels);
            u += 8;
          } 
          u += 8;
          continue;
        case 1:
        case 3:
        case 11:
          u += 2;
          continue;
        case 2:
        case 5:
        case 6:
        case 12:
        case 13:
          u += 3;
          continue;
        case 7:
        case 8:
          u += 5;
          continue;
      } 
      u += 4;
    } 
    for (int i = readUnsignedShort(u); i > 0; i--) {
      Label start = readLabel(readUnsignedShort(u + 2), labels);
      Label end = readLabel(readUnsignedShort(u + 4), labels);
      Label handler = readLabel(readUnsignedShort(u + 6), labels);
      String type = readUTF8(this.items[readUnsignedShort(u + 8)], c);
      mv.visitTryCatchBlock(start, end, handler, type);
      u += 8;
    } 
    u += 2;
    int[] tanns = null;
    int[] itanns = null;
    int tann = 0;
    int itann = 0;
    int ntoff = -1;
    int nitoff = -1;
    int varTable = 0;
    int varTypeTable = 0;
    boolean zip = true;
    boolean unzip = ((context.flags & 0x8) != 0);
    int stackMap = 0;
    int stackMapSize = 0;
    int frameCount = 0;
    Context frame = null;
    Attribute attributes = null;
    int j;
    for (j = readUnsignedShort(u); j > 0; j--) {
      String attrName = readUTF8(u + 2, c);
      if ("LocalVariableTable".equals(attrName)) {
        if ((context.flags & 0x2) == 0) {
          varTable = u + 8;
          for (int k = readUnsignedShort(u + 8), v = u; k > 0; k--) {
            int label = readUnsignedShort(v + 10);
            if (labels[label] == null)
              (readLabel(label, labels)).status |= 0x1; 
            label += readUnsignedShort(v + 12);
            if (labels[label] == null)
              (readLabel(label, labels)).status |= 0x1; 
            v += 10;
          } 
        } 
      } else if ("LocalVariableTypeTable".equals(attrName)) {
        varTypeTable = u + 8;
      } else if ("LineNumberTable".equals(attrName)) {
        if ((context.flags & 0x2) == 0)
          for (int k = readUnsignedShort(u + 8), v = u; k > 0; k--) {
            int label = readUnsignedShort(v + 10);
            if (labels[label] == null)
              (readLabel(label, labels)).status |= 0x1; 
            Label l = labels[label];
            while (l.line > 0) {
              if (l.next == null)
                l.next = new Label(); 
              l = l.next;
            } 
            l.line = readUnsignedShort(v + 12);
            v += 4;
          }  
      } else if ("RuntimeVisibleTypeAnnotations"
        .equals(attrName)) {
        tanns = readTypeAnnotations(mv, context, u + 8, true);
        ntoff = (tanns.length == 0 || readByte(tanns[0]) < 67) ? -1 : readUnsignedShort(tanns[0] + 1);
      } else if ("RuntimeInvisibleTypeAnnotations"
        .equals(attrName)) {
        itanns = readTypeAnnotations(mv, context, u + 8, false);
        nitoff = (itanns.length == 0 || readByte(itanns[0]) < 67) ? -1 : readUnsignedShort(itanns[0] + 1);
      } else if ("StackMapTable".equals(attrName)) {
        if ((context.flags & 0x4) == 0) {
          stackMap = u + 10;
          stackMapSize = readInt(u + 4);
          frameCount = readUnsignedShort(u + 8);
        } 
      } else if ("StackMap".equals(attrName)) {
        if ((context.flags & 0x4) == 0) {
          zip = false;
          stackMap = u + 10;
          stackMapSize = readInt(u + 4);
          frameCount = readUnsignedShort(u + 8);
        } 
      } else {
        for (int k = 0; k < context.attrs.length; k++) {
          if ((context.attrs[k]).type.equals(attrName)) {
            Attribute attr = context.attrs[k].read(this, u + 8, 
                readInt(u + 4), c, codeStart - 8, labels);
            if (attr != null) {
              attr.next = attributes;
              attributes = attr;
            } 
          } 
        } 
      } 
      u += 6 + readInt(u + 4);
    } 
    u += 2;
    if (stackMap != 0) {
      frame = context;
      frame.offset = -1;
      frame.mode = 0;
      frame.localCount = 0;
      frame.localDiff = 0;
      frame.stackCount = 0;
      frame.local = new Object[maxLocals];
      frame.stack = new Object[maxStack];
      if (unzip)
        getImplicitFrame(context); 
      for (j = stackMap; j < stackMap + stackMapSize - 2; j++) {
        if (b[j] == 8) {
          int v = readUnsignedShort(j + 1);
          if (v >= 0 && v < codeLength && (
            b[codeStart + v] & 0xFF) == 187)
            readLabel(v, labels); 
        } 
      } 
    } 
    if ((context.flags & 0x100) != 0)
      mv.visitFrame(-1, maxLocals, null, 0, null); 
    int opcodeDelta = ((context.flags & 0x100) == 0) ? -33 : 0;
    u = codeStart;
    while (u < codeEnd) {
      Label target;
      int label, cpIndex, min, len;
      boolean itf;
      int bsmIndex, max, keys[];
      String iowner;
      Handle bsm;
      Label[] table, values;
      String iname;
      int bsmArgCount, k;
      String idesc;
      Object[] bsmArgs;
      int m;
      String str1, str2;
      int offset = u - codeStart;
      Label l = labels[offset];
      if (l != null) {
        Label next = l.next;
        l.next = null;
        mv.visitLabel(l);
        if ((context.flags & 0x2) == 0 && l.line > 0) {
          mv.visitLineNumber(l.line, l);
          while (next != null) {
            mv.visitLineNumber(next.line, l);
            next = next.next;
          } 
        } 
      } 
      while (frame != null && (frame.offset == offset || frame.offset == -1)) {
        if (frame.offset != -1)
          if (!zip || unzip) {
            mv.visitFrame(-1, frame.localCount, frame.local, frame.stackCount, frame.stack);
          } else {
            mv.visitFrame(frame.mode, frame.localDiff, frame.local, frame.stackCount, frame.stack);
          }  
        if (frameCount > 0) {
          stackMap = readFrame(stackMap, zip, unzip, frame);
          frameCount--;
          continue;
        } 
        frame = null;
      } 
      int opcode = b[u] & 0xFF;
      switch (ClassWriter.TYPE[opcode]) {
        case 0:
          mv.visitInsn(opcode);
          u++;
          break;
        case 4:
          if (opcode > 54) {
            opcode -= 59;
            mv.visitVarInsn(54 + (opcode >> 2), opcode & 0x3);
          } else {
            opcode -= 26;
            mv.visitVarInsn(21 + (opcode >> 2), opcode & 0x3);
          } 
          u++;
          break;
        case 9:
          mv.visitJumpInsn(opcode, labels[offset + readShort(u + 1)]);
          u += 3;
          break;
        case 10:
          mv.visitJumpInsn(opcode + opcodeDelta, labels[offset + 
                readInt(u + 1)]);
          u += 5;
          break;
        case 18:
          opcode = (opcode < 218) ? (opcode - 49) : (opcode - 20);
          target = labels[offset + readUnsignedShort(u + 1)];
          if (opcode == 167 || opcode == 168) {
            mv.visitJumpInsn(opcode + 33, target);
          } else {
            opcode = (opcode <= 166) ? ((opcode + 1 ^ 0x1) - 1) : (opcode ^ 0x1);
            Label endif = new Label();
            mv.visitJumpInsn(opcode, endif);
            mv.visitJumpInsn(200, target);
            mv.visitLabel(endif);
            if (stackMap != 0 && (frame == null || frame.offset != offset + 3))
              mv.visitFrame(256, 0, null, 0, null); 
          } 
          u += 3;
          break;
        case 17:
          opcode = b[u + 1] & 0xFF;
          if (opcode == 132) {
            mv.visitIincInsn(readUnsignedShort(u + 2), readShort(u + 4));
            u += 6;
            break;
          } 
          mv.visitVarInsn(opcode, readUnsignedShort(u + 2));
          u += 4;
          break;
        case 14:
          u = u + 4 - (offset & 0x3);
          label = offset + readInt(u);
          min = readInt(u + 4);
          max = readInt(u + 8);
          table = new Label[max - min + 1];
          u += 12;
          for (k = 0; k < table.length; k++) {
            table[k] = labels[offset + readInt(u)];
            u += 4;
          } 
          mv.visitTableSwitchInsn(min, max, labels[label], table);
          break;
        case 15:
          u = u + 4 - (offset & 0x3);
          label = offset + readInt(u);
          len = readInt(u + 4);
          keys = new int[len];
          values = new Label[len];
          u += 8;
          for (k = 0; k < len; k++) {
            keys[k] = readInt(u);
            values[k] = labels[offset + readInt(u + 4)];
            u += 8;
          } 
          mv.visitLookupSwitchInsn(labels[label], keys, values);
          break;
        case 3:
          mv.visitVarInsn(opcode, b[u + 1] & 0xFF);
          u += 2;
          break;
        case 1:
          mv.visitIntInsn(opcode, b[u + 1]);
          u += 2;
          break;
        case 2:
          mv.visitIntInsn(opcode, readShort(u + 1));
          u += 3;
          break;
        case 11:
          mv.visitLdcInsn(readConst(b[u + 1] & 0xFF, c));
          u += 2;
          break;
        case 12:
          mv.visitLdcInsn(readConst(readUnsignedShort(u + 1), c));
          u += 3;
          break;
        case 6:
        case 7:
          cpIndex = this.items[readUnsignedShort(u + 1)];
          itf = (b[cpIndex - 1] == 11);
          iowner = readClass(cpIndex, c);
          cpIndex = this.items[readUnsignedShort(cpIndex + 2)];
          iname = readUTF8(cpIndex, c);
          idesc = readUTF8(cpIndex + 2, c);
          if (opcode < 182) {
            mv.visitFieldInsn(opcode, iowner, iname, idesc);
          } else {
            mv.visitMethodInsn(opcode, iowner, iname, idesc, itf);
          } 
          if (opcode == 185) {
            u += 5;
            break;
          } 
          u += 3;
          break;
        case 8:
          cpIndex = this.items[readUnsignedShort(u + 1)];
          bsmIndex = context.bootstrapMethods[readUnsignedShort(cpIndex)];
          bsm = (Handle)readConst(readUnsignedShort(bsmIndex), c);
          bsmArgCount = readUnsignedShort(bsmIndex + 2);
          bsmArgs = new Object[bsmArgCount];
          bsmIndex += 4;
          for (m = 0; m < bsmArgCount; m++) {
            bsmArgs[m] = readConst(readUnsignedShort(bsmIndex), c);
            bsmIndex += 2;
          } 
          cpIndex = this.items[readUnsignedShort(cpIndex + 2)];
          str1 = readUTF8(cpIndex, c);
          str2 = readUTF8(cpIndex + 2, c);
          mv.visitInvokeDynamicInsn(str1, str2, bsm, bsmArgs);
          u += 5;
          break;
        case 5:
          mv.visitTypeInsn(opcode, readClass(u + 1, c));
          u += 3;
          break;
        case 13:
          mv.visitIincInsn(b[u + 1] & 0xFF, b[u + 2]);
          u += 3;
          break;
        default:
          mv.visitMultiANewArrayInsn(readClass(u + 1, c), b[u + 3] & 0xFF);
          u += 4;
          break;
      } 
      while (tanns != null && tann < tanns.length && ntoff <= offset) {
        if (ntoff == offset) {
          int v = readAnnotationTarget(context, tanns[tann]);
          readAnnotationValues(v + 2, c, true, mv
              .visitInsnAnnotation(context.typeRef, context.typePath, 
                readUTF8(v, c), true));
        } 
        ntoff = (++tann >= tanns.length || readByte(tanns[tann]) < 67) ? -1 : readUnsignedShort(tanns[tann] + 1);
      } 
      while (itanns != null && itann < itanns.length && nitoff <= offset) {
        if (nitoff == offset) {
          int v = readAnnotationTarget(context, itanns[itann]);
          readAnnotationValues(v + 2, c, true, mv
              .visitInsnAnnotation(context.typeRef, context.typePath, 
                readUTF8(v, c), false));
        } 
        nitoff = (++itann >= itanns.length || readByte(itanns[itann]) < 67) ? -1 : readUnsignedShort(itanns[itann] + 1);
      } 
    } 
    if (labels[codeLength] != null)
      mv.visitLabel(labels[codeLength]); 
    if ((context.flags & 0x2) == 0 && varTable != 0) {
      int[] typeTable = null;
      if (varTypeTable != 0) {
        u = varTypeTable + 2;
        typeTable = new int[readUnsignedShort(varTypeTable) * 3];
        for (int m = typeTable.length; m > 0; ) {
          typeTable[--m] = u + 6;
          typeTable[--m] = readUnsignedShort(u + 8);
          typeTable[--m] = readUnsignedShort(u);
          u += 10;
        } 
      } 
      u = varTable + 2;
      for (int k = readUnsignedShort(varTable); k > 0; k--) {
        int start = readUnsignedShort(u);
        int length = readUnsignedShort(u + 2);
        int index = readUnsignedShort(u + 8);
        String vsignature = null;
        if (typeTable != null)
          for (int m = 0; m < typeTable.length; m += 3) {
            if (typeTable[m] == start && typeTable[m + 1] == index) {
              vsignature = readUTF8(typeTable[m + 2], c);
              break;
            } 
          }  
        mv.visitLocalVariable(readUTF8(u + 4, c), readUTF8(u + 6, c), vsignature, labels[start], labels[start + length], index);
        u += 10;
      } 
    } 
    if (tanns != null)
      for (int k = 0; k < tanns.length; k++) {
        if (readByte(tanns[k]) >> 1 == 32) {
          int v = readAnnotationTarget(context, tanns[k]);
          v = readAnnotationValues(v + 2, c, true, mv
              .visitLocalVariableAnnotation(context.typeRef, context.typePath, context.start, context.end, context.index, 
                
                readUTF8(v, c), true));
        } 
      }  
    if (itanns != null)
      for (int k = 0; k < itanns.length; k++) {
        if (readByte(itanns[k]) >> 1 == 32) {
          int v = readAnnotationTarget(context, itanns[k]);
          v = readAnnotationValues(v + 2, c, true, mv
              .visitLocalVariableAnnotation(context.typeRef, context.typePath, context.start, context.end, context.index, 
                
                readUTF8(v, c), false));
        } 
      }  
    while (attributes != null) {
      Attribute attr = attributes.next;
      attributes.next = null;
      mv.visitAttribute(attributes);
      attributes = attr;
    } 
    mv.visitMaxs(maxStack, maxLocals);
  }
  
  private int[] readTypeAnnotations(MethodVisitor mv, Context context, int u, boolean visible) {
    char[] c = context.buffer;
    int[] offsets = new int[readUnsignedShort(u)];
    u += 2;
    for (int i = 0; i < offsets.length; i++) {
      int j;
      offsets[i] = u;
      int target = readInt(u);
      switch (target >>> 24) {
        case 0:
        case 1:
        case 22:
          u += 2;
          break;
        case 19:
        case 20:
        case 21:
          u++;
          break;
        case 64:
        case 65:
          for (j = readUnsignedShort(u + 1); j > 0; j--) {
            int start = readUnsignedShort(u + 3);
            int length = readUnsignedShort(u + 5);
            readLabel(start, context.labels);
            readLabel(start + length, context.labels);
            u += 6;
          } 
          u += 3;
          break;
        case 71:
        case 72:
        case 73:
        case 74:
        case 75:
          u += 4;
          break;
        default:
          u += 3;
          break;
      } 
      int pathLength = readByte(u);
      if (target >>> 24 == 66) {
        TypePath path = (pathLength == 0) ? null : new TypePath(this.b, u);
        u += 1 + 2 * pathLength;
        u = readAnnotationValues(u + 2, c, true, mv
            .visitTryCatchAnnotation(target, path, 
              readUTF8(u, c), visible));
      } else {
        u = readAnnotationValues(u + 3 + 2 * pathLength, c, true, null);
      } 
    } 
    return offsets;
  }
  
  private int readAnnotationTarget(Context context, int u) {
    int n, i, target = readInt(u);
    switch (target >>> 24) {
      case 0:
      case 1:
      case 22:
        target &= 0xFFFF0000;
        u += 2;
        break;
      case 19:
      case 20:
      case 21:
        target &= 0xFF000000;
        u++;
        break;
      case 64:
      case 65:
        target &= 0xFF000000;
        n = readUnsignedShort(u + 1);
        context.start = new Label[n];
        context.end = new Label[n];
        context.index = new int[n];
        u += 3;
        for (i = 0; i < n; i++) {
          int start = readUnsignedShort(u);
          int length = readUnsignedShort(u + 2);
          context.start[i] = readLabel(start, context.labels);
          context.end[i] = readLabel(start + length, context.labels);
          context.index[i] = readUnsignedShort(u + 4);
          u += 6;
        } 
        break;
      case 71:
      case 72:
      case 73:
      case 74:
      case 75:
        target &= 0xFF0000FF;
        u += 4;
        break;
      default:
        target &= (target >>> 24 < 67) ? -256 : -16777216;
        u += 3;
        break;
    } 
    int pathLength = readByte(u);
    context.typeRef = target;
    context.typePath = (pathLength == 0) ? null : new TypePath(this.b, u);
    return u + 1 + 2 * pathLength;
  }
  
  private void readParameterAnnotations(MethodVisitor mv, Context context, int v, boolean visible) {
    int n = this.b[v++] & 0xFF;
    int synthetics = (Type.getArgumentTypes(context.desc)).length - n;
    int i;
    for (i = 0; i < synthetics; i++) {
      AnnotationVisitor av = mv.visitParameterAnnotation(i, "Ljava/lang/Synthetic;", false);
      if (av != null)
        av.visitEnd(); 
    } 
    char[] c = context.buffer;
    for (; i < n + synthetics; i++) {
      int j = readUnsignedShort(v);
      v += 2;
      for (; j > 0; j--) {
        AnnotationVisitor av = mv.visitParameterAnnotation(i, readUTF8(v, c), visible);
        v = readAnnotationValues(v + 2, c, true, av);
      } 
    } 
  }
  
  private int readAnnotationValues(int v, char[] buf, boolean named, AnnotationVisitor av) {
    int i = readUnsignedShort(v);
    v += 2;
    if (named) {
      for (; i > 0; i--)
        v = readAnnotationValue(v + 2, buf, readUTF8(v, buf), av); 
    } else {
      for (; i > 0; i--)
        v = readAnnotationValue(v, buf, null, av); 
    } 
    if (av != null)
      av.visitEnd(); 
    return v;
  }
  
  private int readAnnotationValue(int v, char[] buf, String name, AnnotationVisitor av) {
    int size;
    byte[] bv;
    int i;
    boolean[] zv;
    short[] sv;
    char[] cv;
    int[] iv;
    long[] lv;
    float[] fv;
    double[] dv;
    if (av == null) {
      switch (this.b[v] & 0xFF) {
        case 101:
          return v + 5;
        case 64:
          return readAnnotationValues(v + 3, buf, true, null);
        case 91:
          return readAnnotationValues(v + 1, buf, false, null);
      } 
      return v + 3;
    } 
    switch (this.b[v++] & 0xFF) {
      case 68:
      case 70:
      case 73:
      case 74:
        av.visit(name, readConst(readUnsignedShort(v), buf));
        v += 2;
        break;
      case 66:
        av.visit(name, Byte.valueOf((byte)readInt(this.items[readUnsignedShort(v)])));
        v += 2;
        break;
      case 90:
        av.visit(name, 
            (readInt(this.items[readUnsignedShort(v)]) == 0) ? Boolean.FALSE : Boolean.TRUE);
        v += 2;
        break;
      case 83:
        av.visit(name, Short.valueOf((short)readInt(this.items[readUnsignedShort(v)])));
        v += 2;
        break;
      case 67:
        av.visit(name, Character.valueOf((char)readInt(this.items[readUnsignedShort(v)])));
        v += 2;
        break;
      case 115:
        av.visit(name, readUTF8(v, buf));
        v += 2;
        break;
      case 101:
        av.visitEnum(name, readUTF8(v, buf), readUTF8(v + 2, buf));
        v += 4;
        break;
      case 99:
        av.visit(name, Type.getType(readUTF8(v, buf)));
        v += 2;
        break;
      case 64:
        v = readAnnotationValues(v + 2, buf, true, av
            .visitAnnotation(name, readUTF8(v, buf)));
        break;
      case 91:
        size = readUnsignedShort(v);
        v += 2;
        if (size == 0)
          return readAnnotationValues(v - 2, buf, false, av
              .visitArray(name)); 
        switch (this.b[v++] & 0xFF) {
          case 66:
            bv = new byte[size];
            for (i = 0; i < size; i++) {
              bv[i] = (byte)readInt(this.items[readUnsignedShort(v)]);
              v += 3;
            } 
            av.visit(name, bv);
            v--;
            break;
          case 90:
            zv = new boolean[size];
            for (i = 0; i < size; i++) {
              zv[i] = (readInt(this.items[readUnsignedShort(v)]) != 0);
              v += 3;
            } 
            av.visit(name, zv);
            v--;
            break;
          case 83:
            sv = new short[size];
            for (i = 0; i < size; i++) {
              sv[i] = (short)readInt(this.items[readUnsignedShort(v)]);
              v += 3;
            } 
            av.visit(name, sv);
            v--;
            break;
          case 67:
            cv = new char[size];
            for (i = 0; i < size; i++) {
              cv[i] = (char)readInt(this.items[readUnsignedShort(v)]);
              v += 3;
            } 
            av.visit(name, cv);
            v--;
            break;
          case 73:
            iv = new int[size];
            for (i = 0; i < size; i++) {
              iv[i] = readInt(this.items[readUnsignedShort(v)]);
              v += 3;
            } 
            av.visit(name, iv);
            v--;
            break;
          case 74:
            lv = new long[size];
            for (i = 0; i < size; i++) {
              lv[i] = readLong(this.items[readUnsignedShort(v)]);
              v += 3;
            } 
            av.visit(name, lv);
            v--;
            break;
          case 70:
            fv = new float[size];
            for (i = 0; i < size; i++) {
              fv[i] = 
                Float.intBitsToFloat(readInt(this.items[readUnsignedShort(v)]));
              v += 3;
            } 
            av.visit(name, fv);
            v--;
            break;
          case 68:
            dv = new double[size];
            for (i = 0; i < size; i++) {
              dv[i] = 
                Double.longBitsToDouble(readLong(this.items[readUnsignedShort(v)]));
              v += 3;
            } 
            av.visit(name, dv);
            v--;
            break;
        } 
        v = readAnnotationValues(v - 3, buf, false, av.visitArray(name));
        break;
    } 
    return v;
  }
  
  private void getImplicitFrame(Context frame) {
    String desc = frame.desc;
    Object[] locals = frame.local;
    int local = 0;
    if ((frame.access & 0x8) == 0)
      if ("<init>".equals(frame.name)) {
        locals[local++] = Opcodes.UNINITIALIZED_THIS;
      } else {
        locals[local++] = readClass(this.header + 2, frame.buffer);
      }  
    int i = 1;
    while (true) {
      int j = i;
      switch (desc.charAt(i++)) {
        case 'B':
        case 'C':
        case 'I':
        case 'S':
        case 'Z':
          locals[local++] = Opcodes.INTEGER;
          continue;
        case 'F':
          locals[local++] = Opcodes.FLOAT;
          continue;
        case 'J':
          locals[local++] = Opcodes.LONG;
          continue;
        case 'D':
          locals[local++] = Opcodes.DOUBLE;
          continue;
        case '[':
          while (desc.charAt(i) == '[')
            i++; 
          if (desc.charAt(i) == 'L') {
            i++;
            while (desc.charAt(i) != ';')
              i++; 
          } 
          locals[local++] = desc.substring(j, ++i);
          continue;
        case 'L':
          while (desc.charAt(i) != ';')
            i++; 
          locals[local++] = desc.substring(j + 1, i++);
          continue;
      } 
      break;
    } 
    frame.localCount = local;
  }
  
  private int readFrame(int stackMap, boolean zip, boolean unzip, Context frame) {
    int tag, delta;
    char[] c = frame.buffer;
    Label[] labels = frame.labels;
    if (zip) {
      tag = this.b[stackMap++] & 0xFF;
    } else {
      tag = 255;
      frame.offset = -1;
    } 
    frame.localDiff = 0;
    if (tag < 64) {
      delta = tag;
      frame.mode = 3;
      frame.stackCount = 0;
    } else if (tag < 128) {
      delta = tag - 64;
      stackMap = readFrameType(frame.stack, 0, stackMap, c, labels);
      frame.mode = 4;
      frame.stackCount = 1;
    } else {
      delta = readUnsignedShort(stackMap);
      stackMap += 2;
      if (tag == 247) {
        stackMap = readFrameType(frame.stack, 0, stackMap, c, labels);
        frame.mode = 4;
        frame.stackCount = 1;
      } else if (tag >= 248 && tag < 251) {
        frame.mode = 2;
        frame.localDiff = 251 - tag;
        frame.localCount -= frame.localDiff;
        frame.stackCount = 0;
      } else if (tag == 251) {
        frame.mode = 3;
        frame.stackCount = 0;
      } else if (tag < 255) {
        int local = unzip ? frame.localCount : 0;
        for (int i = tag - 251; i > 0; i--)
          stackMap = readFrameType(frame.local, local++, stackMap, c, labels); 
        frame.mode = 1;
        frame.localDiff = tag - 251;
        frame.localCount += frame.localDiff;
        frame.stackCount = 0;
      } else {
        frame.mode = 0;
        int n = readUnsignedShort(stackMap);
        stackMap += 2;
        frame.localDiff = n;
        frame.localCount = n;
        for (int local = 0; n > 0; n--)
          stackMap = readFrameType(frame.local, local++, stackMap, c, labels); 
        n = readUnsignedShort(stackMap);
        stackMap += 2;
        frame.stackCount = n;
        for (int stack = 0; n > 0; n--)
          stackMap = readFrameType(frame.stack, stack++, stackMap, c, labels); 
      } 
    } 
    frame.offset += delta + 1;
    readLabel(frame.offset, labels);
    return stackMap;
  }
  
  private int readFrameType(Object[] frame, int index, int v, char[] buf, Label[] labels) {
    int type = this.b[v++] & 0xFF;
    switch (type) {
      case 0:
        frame[index] = Opcodes.TOP;
        return v;
      case 1:
        frame[index] = Opcodes.INTEGER;
        return v;
      case 2:
        frame[index] = Opcodes.FLOAT;
        return v;
      case 3:
        frame[index] = Opcodes.DOUBLE;
        return v;
      case 4:
        frame[index] = Opcodes.LONG;
        return v;
      case 5:
        frame[index] = Opcodes.NULL;
        return v;
      case 6:
        frame[index] = Opcodes.UNINITIALIZED_THIS;
        return v;
      case 7:
        frame[index] = readClass(v, buf);
        v += 2;
        return v;
    } 
    frame[index] = readLabel(readUnsignedShort(v), labels);
    v += 2;
    return v;
  }
  
  protected Label readLabel(int offset, Label[] labels) {
    if (labels[offset] == null)
      labels[offset] = new Label(); 
    return labels[offset];
  }
  
  private int getAttributes() {
    int u = this.header + 8 + readUnsignedShort(this.header + 6) * 2;
    int i;
    for (i = readUnsignedShort(u); i > 0; i--) {
      for (int j = readUnsignedShort(u + 8); j > 0; j--)
        u += 6 + readInt(u + 12); 
      u += 8;
    } 
    u += 2;
    for (i = readUnsignedShort(u); i > 0; i--) {
      for (int j = readUnsignedShort(u + 8); j > 0; j--)
        u += 6 + readInt(u + 12); 
      u += 8;
    } 
    return u + 2;
  }
  
  private Attribute readAttribute(Attribute[] attrs, String type, int off, int len, char[] buf, int codeOff, Label[] labels) {
    for (int i = 0; i < attrs.length; i++) {
      if ((attrs[i]).type.equals(type))
        return attrs[i].read(this, off, len, buf, codeOff, labels); 
    } 
    return (new Attribute(type)).read(this, off, len, null, -1, null);
  }
  
  public int getItemCount() {
    return this.items.length;
  }
  
  public int getItem(int item) {
    return this.items[item];
  }
  
  public int getMaxStringLength() {
    return this.maxStringLength;
  }
  
  public int readByte(int index) {
    return this.b[index] & 0xFF;
  }
  
  public int readUnsignedShort(int index) {
    byte[] b = this.b;
    return (b[index] & 0xFF) << 8 | b[index + 1] & 0xFF;
  }
  
  public short readShort(int index) {
    byte[] b = this.b;
    return (short)((b[index] & 0xFF) << 8 | b[index + 1] & 0xFF);
  }
  
  public int readInt(int index) {
    byte[] b = this.b;
    return (b[index] & 0xFF) << 24 | (b[index + 1] & 0xFF) << 16 | (b[index + 2] & 0xFF) << 8 | b[index + 3] & 0xFF;
  }
  
  public long readLong(int index) {
    long l1 = readInt(index);
    long l0 = readInt(index + 4) & 0xFFFFFFFFL;
    return l1 << 32L | l0;
  }
  
  public String readUTF8(int index, char[] buf) {
    int item = readUnsignedShort(index);
    if (index == 0 || item == 0)
      return null; 
    String s = this.strings[item];
    if (s != null)
      return s; 
    index = this.items[item];
    this.strings[item] = readUTF(index + 2, readUnsignedShort(index), buf);
    return readUTF(index + 2, readUnsignedShort(index), buf);
  }
  
  private String readUTF(int index, int utfLen, char[] buf) {
    int endIndex = index + utfLen;
    byte[] b = this.b;
    int strLen = 0;
    int st = 0;
    char cc = Character.MIN_VALUE;
    while (index < endIndex) {
      int c = b[index++];
      switch (st) {
        case 0:
          c &= 0xFF;
          if (c < 128) {
            buf[strLen++] = (char)c;
            continue;
          } 
          if (c < 224 && c > 191) {
            cc = (char)(c & 0x1F);
            st = 1;
            continue;
          } 
          cc = (char)(c & 0xF);
          st = 2;
        case 1:
          buf[strLen++] = (char)(cc << 6 | c & 0x3F);
          st = 0;
        case 2:
          cc = (char)(cc << 6 | c & 0x3F);
          st = 1;
      } 
    } 
    return new String(buf, 0, strLen);
  }
  
  public String readClass(int index, char[] buf) {
    return readUTF8(this.items[readUnsignedShort(index)], buf);
  }
  
  public Object readConst(int item, char[] buf) {
    int index = this.items[item];
    switch (this.b[index - 1]) {
      case 3:
        return Integer.valueOf(readInt(index));
      case 4:
        return Float.valueOf(Float.intBitsToFloat(readInt(index)));
      case 5:
        return Long.valueOf(readLong(index));
      case 6:
        return Double.valueOf(Double.longBitsToDouble(readLong(index)));
      case 7:
        return Type.getObjectType(readUTF8(index, buf));
      case 8:
        return readUTF8(index, buf);
      case 16:
        return Type.getMethodType(readUTF8(index, buf));
    } 
    int tag = readByte(index);
    int[] items = this.items;
    int cpIndex = items[readUnsignedShort(index + 1)];
    boolean itf = (this.b[cpIndex - 1] == 11);
    String owner = readClass(cpIndex, buf);
    cpIndex = items[readUnsignedShort(cpIndex + 2)];
    String name = readUTF8(cpIndex, buf);
    String desc = readUTF8(cpIndex + 2, buf);
    return new Handle(tag, owner, name, desc, itf);
  }
}
