package org.spongepowered.asm.lib.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.lib.Attribute;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.TypePath;

public abstract class Printer {
  static {
    String s = "NOP,ACONST_NULL,ICONST_M1,ICONST_0,ICONST_1,ICONST_2,ICONST_3,ICONST_4,ICONST_5,LCONST_0,LCONST_1,FCONST_0,FCONST_1,FCONST_2,DCONST_0,DCONST_1,BIPUSH,SIPUSH,LDC,,,ILOAD,LLOAD,FLOAD,DLOAD,ALOAD,,,,,,,,,,,,,,,,,,,,,IALOAD,LALOAD,FALOAD,DALOAD,AALOAD,BALOAD,CALOAD,SALOAD,ISTORE,LSTORE,FSTORE,DSTORE,ASTORE,,,,,,,,,,,,,,,,,,,,,IASTORE,LASTORE,FASTORE,DASTORE,AASTORE,BASTORE,CASTORE,SASTORE,POP,POP2,DUP,DUP_X1,DUP_X2,DUP2,DUP2_X1,DUP2_X2,SWAP,IADD,LADD,FADD,DADD,ISUB,LSUB,FSUB,DSUB,IMUL,LMUL,FMUL,DMUL,IDIV,LDIV,FDIV,DDIV,IREM,LREM,FREM,DREM,INEG,LNEG,FNEG,DNEG,ISHL,LSHL,ISHR,LSHR,IUSHR,LUSHR,IAND,LAND,IOR,LOR,IXOR,LXOR,IINC,I2L,I2F,I2D,L2I,L2F,L2D,F2I,F2L,F2D,D2I,D2L,D2F,I2B,I2C,I2S,LCMP,FCMPL,FCMPG,DCMPL,DCMPG,IFEQ,IFNE,IFLT,IFGE,IFGT,IFLE,IF_ICMPEQ,IF_ICMPNE,IF_ICMPLT,IF_ICMPGE,IF_ICMPGT,IF_ICMPLE,IF_ACMPEQ,IF_ACMPNE,GOTO,JSR,RET,TABLESWITCH,LOOKUPSWITCH,IRETURN,LRETURN,FRETURN,DRETURN,ARETURN,RETURN,GETSTATIC,PUTSTATIC,GETFIELD,PUTFIELD,INVOKEVIRTUAL,INVOKESPECIAL,INVOKESTATIC,INVOKEINTERFACE,INVOKEDYNAMIC,NEW,NEWARRAY,ANEWARRAY,ARRAYLENGTH,ATHROW,CHECKCAST,INSTANCEOF,MONITORENTER,MONITOREXIT,,MULTIANEWARRAY,IFNULL,IFNONNULL,";
  }
  
  public static final String[] OPCODES = new String[200];
  
  static {
    int i = 0;
    int j = 0;
    int l;
    while ((l = s.indexOf(',', j)) > 0) {
      OPCODES[i++] = (j + 1 == l) ? null : s.substring(j, l);
      j = l + 1;
    } 
    s = "T_BOOLEAN,T_CHAR,T_FLOAT,T_DOUBLE,T_BYTE,T_SHORT,T_INT,T_LONG,";
  }
  
  public static final String[] TYPES = new String[12];
  
  static {
    j = 0;
    i = 4;
    while ((l = s.indexOf(',', j)) > 0) {
      TYPES[i++] = s.substring(j, l);
      j = l + 1;
    } 
    s = "H_GETFIELD,H_GETSTATIC,H_PUTFIELD,H_PUTSTATIC,H_INVOKEVIRTUAL,H_INVOKESTATIC,H_INVOKESPECIAL,H_NEWINVOKESPECIAL,H_INVOKEINTERFACE,";
  }
  
  public static final String[] HANDLE_TAG = new String[10];
  
  protected final int api;
  
  protected final StringBuffer buf;
  
  public final List<Object> text;
  
  static {
    j = 0;
    i = 1;
    while ((l = s.indexOf(',', j)) > 0) {
      HANDLE_TAG[i++] = s.substring(j, l);
      j = l + 1;
    } 
  }
  
  protected Printer(int api) {
    this.api = api;
    this.buf = new StringBuffer();
    this.text = new ArrayList();
  }
  
  public Printer visitClassTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    throw new RuntimeException("Must be overriden");
  }
  
  public Printer visitFieldTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    throw new RuntimeException("Must be overriden");
  }
  
  public void visitParameter(String name, int access) {
    throw new RuntimeException("Must be overriden");
  }
  
  public Printer visitMethodTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    throw new RuntimeException("Must be overriden");
  }
  
  @Deprecated
  public void visitMethodInsn(int opcode, String owner, String name, String desc) {
    if (this.api >= 327680) {
      boolean itf = (opcode == 185);
      visitMethodInsn(opcode, owner, name, desc, itf);
      return;
    } 
    throw new RuntimeException("Must be overriden");
  }
  
  public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    if (this.api < 327680) {
      if (itf != ((opcode == 185)))
        throw new IllegalArgumentException("INVOKESPECIAL/STATIC on interfaces require ASM 5"); 
      visitMethodInsn(opcode, owner, name, desc);
      return;
    } 
    throw new RuntimeException("Must be overriden");
  }
  
  public Printer visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    throw new RuntimeException("Must be overriden");
  }
  
  public Printer visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    throw new RuntimeException("Must be overriden");
  }
  
  public Printer visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
    throw new RuntimeException("Must be overriden");
  }
  
  public List<Object> getText() {
    return this.text;
  }
  
  public void print(PrintWriter pw) {
    printList(pw, this.text);
  }
  
  public static void appendString(StringBuffer buf, String s) {
    buf.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\n') {
        buf.append("\\n");
      } else if (c == '\r') {
        buf.append("\\r");
      } else if (c == '\\') {
        buf.append("\\\\");
      } else if (c == '"') {
        buf.append("\\\"");
      } else if (c < ' ' || c > '') {
        buf.append("\\u");
        if (c < '\020') {
          buf.append("000");
        } else if (c < 'Ā') {
          buf.append("00");
        } else if (c < 'က') {
          buf.append('0');
        } 
        buf.append(Integer.toString(c, 16));
      } else {
        buf.append(c);
      } 
    } 
    buf.append('"');
  }
  
  static void printList(PrintWriter pw, List<?> l) {
    for (int i = 0; i < l.size(); i++) {
      Object o = l.get(i);
      if (o instanceof List) {
        printList(pw, (List)o);
      } else {
        pw.print(o.toString());
      } 
    } 
  }
  
  public abstract void visit(int paramInt1, int paramInt2, String paramString1, String paramString2, String paramString3, String[] paramArrayOfString);
  
  public abstract void visitSource(String paramString1, String paramString2);
  
  public abstract void visitOuterClass(String paramString1, String paramString2, String paramString3);
  
  public abstract Printer visitClassAnnotation(String paramString, boolean paramBoolean);
  
  public abstract void visitClassAttribute(Attribute paramAttribute);
  
  public abstract void visitInnerClass(String paramString1, String paramString2, String paramString3, int paramInt);
  
  public abstract Printer visitField(int paramInt, String paramString1, String paramString2, String paramString3, Object paramObject);
  
  public abstract Printer visitMethod(int paramInt, String paramString1, String paramString2, String paramString3, String[] paramArrayOfString);
  
  public abstract void visitClassEnd();
  
  public abstract void visit(String paramString, Object paramObject);
  
  public abstract void visitEnum(String paramString1, String paramString2, String paramString3);
  
  public abstract Printer visitAnnotation(String paramString1, String paramString2);
  
  public abstract Printer visitArray(String paramString);
  
  public abstract void visitAnnotationEnd();
  
  public abstract Printer visitFieldAnnotation(String paramString, boolean paramBoolean);
  
  public abstract void visitFieldAttribute(Attribute paramAttribute);
  
  public abstract void visitFieldEnd();
  
  public abstract Printer visitAnnotationDefault();
  
  public abstract Printer visitMethodAnnotation(String paramString, boolean paramBoolean);
  
  public abstract Printer visitParameterAnnotation(int paramInt, String paramString, boolean paramBoolean);
  
  public abstract void visitMethodAttribute(Attribute paramAttribute);
  
  public abstract void visitCode();
  
  public abstract void visitFrame(int paramInt1, int paramInt2, Object[] paramArrayOfObject1, int paramInt3, Object[] paramArrayOfObject2);
  
  public abstract void visitInsn(int paramInt);
  
  public abstract void visitIntInsn(int paramInt1, int paramInt2);
  
  public abstract void visitVarInsn(int paramInt1, int paramInt2);
  
  public abstract void visitTypeInsn(int paramInt, String paramString);
  
  public abstract void visitFieldInsn(int paramInt, String paramString1, String paramString2, String paramString3);
  
  public abstract void visitInvokeDynamicInsn(String paramString1, String paramString2, Handle paramHandle, Object... paramVarArgs);
  
  public abstract void visitJumpInsn(int paramInt, Label paramLabel);
  
  public abstract void visitLabel(Label paramLabel);
  
  public abstract void visitLdcInsn(Object paramObject);
  
  public abstract void visitIincInsn(int paramInt1, int paramInt2);
  
  public abstract void visitTableSwitchInsn(int paramInt1, int paramInt2, Label paramLabel, Label... paramVarArgs);
  
  public abstract void visitLookupSwitchInsn(Label paramLabel, int[] paramArrayOfint, Label[] paramArrayOfLabel);
  
  public abstract void visitMultiANewArrayInsn(String paramString, int paramInt);
  
  public abstract void visitTryCatchBlock(Label paramLabel1, Label paramLabel2, Label paramLabel3, String paramString);
  
  public abstract void visitLocalVariable(String paramString1, String paramString2, String paramString3, Label paramLabel1, Label paramLabel2, int paramInt);
  
  public abstract void visitLineNumber(int paramInt, Label paramLabel);
  
  public abstract void visitMaxs(int paramInt1, int paramInt2);
  
  public abstract void visitMethodEnd();
}
