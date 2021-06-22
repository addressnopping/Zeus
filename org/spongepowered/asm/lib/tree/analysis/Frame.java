package org.spongepowered.asm.lib.tree.analysis;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.IincInsnNode;
import org.spongepowered.asm.lib.tree.InvokeDynamicInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MultiANewArrayInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;

public class Frame<V extends Value> {
  private V returnValue;
  
  private V[] values;
  
  private int locals;
  
  private int top;
  
  public Frame(int nLocals, int nStack) {
    this.values = (V[])new Value[nLocals + nStack];
    this.locals = nLocals;
  }
  
  public Frame(Frame<? extends V> src) {
    this(src.locals, src.values.length - src.locals);
    init(src);
  }
  
  public Frame<V> init(Frame<? extends V> src) {
    this.returnValue = src.returnValue;
    System.arraycopy(src.values, 0, this.values, 0, this.values.length);
    this.top = src.top;
    return this;
  }
  
  public void setReturn(V v) {
    this.returnValue = v;
  }
  
  public int getLocals() {
    return this.locals;
  }
  
  public int getMaxStackSize() {
    return this.values.length - this.locals;
  }
  
  public V getLocal(int i) throws IndexOutOfBoundsException {
    if (i >= this.locals)
      throw new IndexOutOfBoundsException("Trying to access an inexistant local variable"); 
    return this.values[i];
  }
  
  public void setLocal(int i, V value) throws IndexOutOfBoundsException {
    if (i >= this.locals)
      throw new IndexOutOfBoundsException("Trying to access an inexistant local variable " + i); 
    this.values[i] = value;
  }
  
  public int getStackSize() {
    return this.top;
  }
  
  public V getStack(int i) throws IndexOutOfBoundsException {
    return this.values[i + this.locals];
  }
  
  public void clearStack() {
    this.top = 0;
  }
  
  public V pop() throws IndexOutOfBoundsException {
    if (this.top == 0)
      throw new IndexOutOfBoundsException("Cannot pop operand off an empty stack."); 
    return this.values[--this.top + this.locals];
  }
  
  public void push(V value) throws IndexOutOfBoundsException {
    if (this.top + this.locals >= this.values.length)
      throw new IndexOutOfBoundsException("Insufficient maximum stack size."); 
    this.values[this.top++ + this.locals] = value;
  }
  
  public void execute(AbstractInsnNode insn, Interpreter<V> interpreter) throws AnalyzerException {
    V value2;
    V value1;
    int var;
    String desc;
    int i;
    V value3;
    List<V> values;
    int j;
    switch (insn.getOpcode()) {
      case 0:
        return;
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
      case 17:
      case 18:
        push(interpreter.newOperation(insn));
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
        push(interpreter.copyOperation(insn, 
              getLocal(((VarInsnNode)insn).var)));
      case 46:
      case 47:
      case 48:
      case 49:
      case 50:
      case 51:
      case 52:
      case 53:
        value2 = pop();
        value1 = pop();
        push(interpreter.binaryOperation(insn, value1, value2));
      case 54:
      case 55:
      case 56:
      case 57:
      case 58:
        value1 = interpreter.copyOperation(insn, pop());
        var = ((VarInsnNode)insn).var;
        setLocal(var, value1);
        if (value1.getSize() == 2)
          setLocal(var + 1, interpreter.newValue(null)); 
        if (var > 0) {
          Value local = (Value)getLocal(var - 1);
          if (local != null && local.getSize() == 2)
            setLocal(var - 1, interpreter.newValue(null)); 
        } 
      case 79:
      case 80:
      case 81:
      case 82:
      case 83:
      case 84:
      case 85:
      case 86:
        value3 = pop();
        value2 = pop();
        value1 = pop();
        interpreter.ternaryOperation(insn, value1, value2, value3);
      case 87:
        if (pop().getSize() == 2)
          throw new AnalyzerException(insn, "Illegal use of POP"); 
      case 88:
        if (pop().getSize() == 1 && 
          pop().getSize() != 1)
          throw new AnalyzerException(insn, "Illegal use of POP2"); 
      case 89:
        value1 = pop();
        if (value1.getSize() != 1)
          throw new AnalyzerException(insn, "Illegal use of DUP"); 
        push(value1);
        push(interpreter.copyOperation(insn, value1));
      case 90:
        value1 = pop();
        value2 = pop();
        if (value1.getSize() != 1 || value2.getSize() != 1)
          throw new AnalyzerException(insn, "Illegal use of DUP_X1"); 
        push(interpreter.copyOperation(insn, value1));
        push(value2);
        push(value1);
      case 91:
        value1 = pop();
        if (value1.getSize() == 1) {
          value2 = pop();
          if (value2.getSize() == 1) {
            value3 = pop();
            if (value3.getSize() == 1) {
              push(interpreter.copyOperation(insn, value1));
              push(value3);
              push(value2);
              push(value1);
            } else {
              throw new AnalyzerException(insn, "Illegal use of DUP_X2");
            } 
          } else {
            push(interpreter.copyOperation(insn, value1));
            push(value2);
            push(value1);
          } 
        } else {
          throw new AnalyzerException(insn, "Illegal use of DUP_X2");
        } 
      case 92:
        value1 = pop();
        if (value1.getSize() == 1) {
          value2 = pop();
          if (value2.getSize() == 1) {
            push(value2);
            push(value1);
            push(interpreter.copyOperation(insn, value2));
            push(interpreter.copyOperation(insn, value1));
          } else {
            throw new AnalyzerException(insn, "Illegal use of DUP2");
          } 
        } else {
          push(value1);
          push(interpreter.copyOperation(insn, value1));
        } 
      case 93:
        value1 = pop();
        if (value1.getSize() == 1) {
          value2 = pop();
          if (value2.getSize() == 1) {
            value3 = pop();
            if (value3.getSize() == 1) {
              push(interpreter.copyOperation(insn, value2));
              push(interpreter.copyOperation(insn, value1));
              push(value3);
              push(value2);
              push(value1);
            } else {
              throw new AnalyzerException(insn, "Illegal use of DUP2_X1");
            } 
          } else {
            throw new AnalyzerException(insn, "Illegal use of DUP2_X1");
          } 
        } else {
          value2 = pop();
          if (value2.getSize() == 1) {
            push(interpreter.copyOperation(insn, value1));
            push(value2);
            push(value1);
          } else {
            throw new AnalyzerException(insn, "Illegal use of DUP2_X1");
          } 
        } 
      case 94:
        value1 = pop();
        if (value1.getSize() == 1) {
          value2 = pop();
          if (value2.getSize() == 1) {
            value3 = pop();
            if (value3.getSize() == 1) {
              V value4 = pop();
              if (value4.getSize() == 1) {
                push(interpreter.copyOperation(insn, value2));
                push(interpreter.copyOperation(insn, value1));
                push(value4);
                push(value3);
                push(value2);
                push(value1);
              } else {
                throw new AnalyzerException(insn, "Illegal use of DUP2_X2");
              } 
            } else {
              push(interpreter.copyOperation(insn, value2));
              push(interpreter.copyOperation(insn, value1));
              push(value3);
              push(value2);
              push(value1);
            } 
          } else {
            throw new AnalyzerException(insn, "Illegal use of DUP2_X2");
          } 
        } else {
          value2 = pop();
          if (value2.getSize() == 1) {
            value3 = pop();
            if (value3.getSize() == 1) {
              push(interpreter.copyOperation(insn, value1));
              push(value3);
              push(value2);
              push(value1);
            } else {
              throw new AnalyzerException(insn, "Illegal use of DUP2_X2");
            } 
          } else {
            push(interpreter.copyOperation(insn, value1));
            push(value2);
            push(value1);
          } 
        } 
      case 95:
        value2 = pop();
        value1 = pop();
        if (value1.getSize() != 1 || value2.getSize() != 1)
          throw new AnalyzerException(insn, "Illegal use of SWAP"); 
        push(interpreter.copyOperation(insn, value2));
        push(interpreter.copyOperation(insn, value1));
      case 96:
      case 97:
      case 98:
      case 99:
      case 100:
      case 101:
      case 102:
      case 103:
      case 104:
      case 105:
      case 106:
      case 107:
      case 108:
      case 109:
      case 110:
      case 111:
      case 112:
      case 113:
      case 114:
      case 115:
        value2 = pop();
        value1 = pop();
        push(interpreter.binaryOperation(insn, value1, value2));
      case 116:
      case 117:
      case 118:
      case 119:
        push(interpreter.unaryOperation(insn, pop()));
      case 120:
      case 121:
      case 122:
      case 123:
      case 124:
      case 125:
      case 126:
      case 127:
      case 128:
      case 129:
      case 130:
      case 131:
        value2 = pop();
        value1 = pop();
        push(interpreter.binaryOperation(insn, value1, value2));
      case 132:
        var = ((IincInsnNode)insn).var;
        setLocal(var, interpreter.unaryOperation(insn, getLocal(var)));
      case 133:
      case 134:
      case 135:
      case 136:
      case 137:
      case 138:
      case 139:
      case 140:
      case 141:
      case 142:
      case 143:
      case 144:
      case 145:
      case 146:
      case 147:
        push(interpreter.unaryOperation(insn, pop()));
      case 148:
      case 149:
      case 150:
      case 151:
      case 152:
        value2 = pop();
        value1 = pop();
        push(interpreter.binaryOperation(insn, value1, value2));
      case 153:
      case 154:
      case 155:
      case 156:
      case 157:
      case 158:
        interpreter.unaryOperation(insn, pop());
      case 159:
      case 160:
      case 161:
      case 162:
      case 163:
      case 164:
      case 165:
      case 166:
        value2 = pop();
        value1 = pop();
        interpreter.binaryOperation(insn, value1, value2);
      case 167:
        return;
      case 168:
        push(interpreter.newOperation(insn));
      case 169:
        return;
      case 170:
      case 171:
        interpreter.unaryOperation(insn, pop());
      case 172:
      case 173:
      case 174:
      case 175:
      case 176:
        value1 = pop();
        interpreter.unaryOperation(insn, value1);
        interpreter.returnOperation(insn, value1, this.returnValue);
      case 177:
        if (this.returnValue != null)
          throw new AnalyzerException(insn, "Incompatible return type"); 
      case 178:
        push(interpreter.newOperation(insn));
      case 179:
        interpreter.unaryOperation(insn, pop());
      case 180:
        push(interpreter.unaryOperation(insn, pop()));
      case 181:
        value2 = pop();
        value1 = pop();
        interpreter.binaryOperation(insn, value1, value2);
      case 182:
      case 183:
      case 184:
      case 185:
        values = new ArrayList<V>();
        desc = ((MethodInsnNode)insn).desc;
        for (j = (Type.getArgumentTypes(desc)).length; j > 0; j--)
          values.add(0, pop()); 
        if (insn.getOpcode() != 184)
          values.add(0, pop()); 
        if (Type.getReturnType(desc) == Type.VOID_TYPE) {
          interpreter.naryOperation(insn, values);
        } else {
          push(interpreter.naryOperation(insn, values));
        } 
      case 186:
        values = new ArrayList<V>();
        desc = ((InvokeDynamicInsnNode)insn).desc;
        for (j = (Type.getArgumentTypes(desc)).length; j > 0; j--)
          values.add(0, pop()); 
        if (Type.getReturnType(desc) == Type.VOID_TYPE) {
          interpreter.naryOperation(insn, values);
        } else {
          push(interpreter.naryOperation(insn, values));
        } 
      case 187:
        push(interpreter.newOperation(insn));
      case 188:
      case 189:
      case 190:
        push(interpreter.unaryOperation(insn, pop()));
      case 191:
        interpreter.unaryOperation(insn, pop());
      case 192:
      case 193:
        push(interpreter.unaryOperation(insn, pop()));
      case 194:
      case 195:
        interpreter.unaryOperation(insn, pop());
      case 197:
        values = new ArrayList<V>();
        for (i = ((MultiANewArrayInsnNode)insn).dims; i > 0; i--)
          values.add(0, pop()); 
        push(interpreter.naryOperation(insn, values));
      case 198:
      case 199:
        interpreter.unaryOperation(insn, pop());
    } 
    throw new RuntimeException("Illegal opcode " + insn.getOpcode());
  }
  
  public boolean merge(Frame<? extends V> frame, Interpreter<V> interpreter) throws AnalyzerException {
    if (this.top != frame.top)
      throw new AnalyzerException(null, "Incompatible stack heights"); 
    boolean changes = false;
    for (int i = 0; i < this.locals + this.top; i++) {
      V v = interpreter.merge(this.values[i], frame.values[i]);
      if (!v.equals(this.values[i])) {
        this.values[i] = v;
        changes = true;
      } 
    } 
    return changes;
  }
  
  public boolean merge(Frame<? extends V> frame, boolean[] access) {
    boolean changes = false;
    for (int i = 0; i < this.locals; i++) {
      if (!access[i] && !this.values[i].equals(frame.values[i])) {
        this.values[i] = frame.values[i];
        changes = true;
      } 
    } 
    return changes;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int i;
    for (i = 0; i < getLocals(); i++)
      sb.append(getLocal(i)); 
    sb.append(' ');
    for (i = 0; i < getStackSize(); i++)
      sb.append(getStack(i).toString()); 
    return sb.toString();
  }
}
