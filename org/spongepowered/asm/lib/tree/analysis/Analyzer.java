package org.spongepowered.asm.lib.tree.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.IincInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.LookupSwitchInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TableSwitchInsnNode;
import org.spongepowered.asm.lib.tree.TryCatchBlockNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;

public class Analyzer<V extends Value> implements Opcodes {
  private final Interpreter<V> interpreter;
  
  private int n;
  
  private InsnList insns;
  
  private List<TryCatchBlockNode>[] handlers;
  
  private Frame<V>[] frames;
  
  private Subroutine[] subroutines;
  
  private boolean[] queued;
  
  private int[] queue;
  
  private int top;
  
  public Analyzer(Interpreter<V> interpreter) {
    this.interpreter = interpreter;
  }
  
  public Frame<V>[] analyze(String owner, MethodNode m) throws AnalyzerException {
    if ((m.access & 0x500) != 0) {
      this.frames = (Frame<V>[])new Frame[0];
      return this.frames;
    } 
    this.n = m.instructions.size();
    this.insns = m.instructions;
    this.handlers = (List<TryCatchBlockNode>[])new List[this.n];
    this.frames = (Frame<V>[])new Frame[this.n];
    this.subroutines = new Subroutine[this.n];
    this.queued = new boolean[this.n];
    this.queue = new int[this.n];
    this.top = 0;
    for (int i = 0; i < m.tryCatchBlocks.size(); i++) {
      TryCatchBlockNode tcb = m.tryCatchBlocks.get(i);
      int begin = this.insns.indexOf((AbstractInsnNode)tcb.start);
      int end = this.insns.indexOf((AbstractInsnNode)tcb.end);
      for (int n = begin; n < end; n++) {
        List<TryCatchBlockNode> insnHandlers = this.handlers[n];
        if (insnHandlers == null) {
          insnHandlers = new ArrayList<TryCatchBlockNode>();
          this.handlers[n] = insnHandlers;
        } 
        insnHandlers.add(tcb);
      } 
    } 
    Subroutine main = new Subroutine(null, m.maxLocals, null);
    List<AbstractInsnNode> subroutineCalls = new ArrayList<AbstractInsnNode>();
    Map<LabelNode, Subroutine> subroutineHeads = new HashMap<LabelNode, Subroutine>();
    findSubroutine(0, main, subroutineCalls);
    while (!subroutineCalls.isEmpty()) {
      JumpInsnNode jsr = (JumpInsnNode)subroutineCalls.remove(0);
      Subroutine sub = subroutineHeads.get(jsr.label);
      if (sub == null) {
        sub = new Subroutine(jsr.label, m.maxLocals, jsr);
        subroutineHeads.put(jsr.label, sub);
        findSubroutine(this.insns.indexOf((AbstractInsnNode)jsr.label), sub, subroutineCalls);
        continue;
      } 
      sub.callers.add(jsr);
    } 
    for (int j = 0; j < this.n; j++) {
      if (this.subroutines[j] != null && (this.subroutines[j]).start == null)
        this.subroutines[j] = null; 
    } 
    Frame<V> current = newFrame(m.maxLocals, m.maxStack);
    Frame<V> handler = newFrame(m.maxLocals, m.maxStack);
    current.setReturn(this.interpreter.newValue(Type.getReturnType(m.desc)));
    Type[] args = Type.getArgumentTypes(m.desc);
    int local = 0;
    if ((m.access & 0x8) == 0) {
      Type ctype = Type.getObjectType(owner);
      current.setLocal(local++, this.interpreter.newValue(ctype));
    } 
    for (int k = 0; k < args.length; k++) {
      current.setLocal(local++, this.interpreter.newValue(args[k]));
      if (args[k].getSize() == 2)
        current.setLocal(local++, this.interpreter.newValue(null)); 
    } 
    while (local < m.maxLocals)
      current.setLocal(local++, this.interpreter.newValue(null)); 
    merge(0, current, null);
    init(owner, m);
    while (this.top > 0) {
      int insn = this.queue[--this.top];
      Frame<V> f = this.frames[insn];
      Subroutine subroutine = this.subroutines[insn];
      this.queued[insn] = false;
      AbstractInsnNode insnNode = null;
      try {
        insnNode = m.instructions.get(insn);
        int insnOpcode = insnNode.getOpcode();
        int insnType = insnNode.getType();
        if (insnType == 8 || insnType == 15 || insnType == 14) {
          merge(insn + 1, f, subroutine);
          newControlFlowEdge(insn, insn + 1);
        } else {
          current.init(f).execute(insnNode, this.interpreter);
          subroutine = (subroutine == null) ? null : subroutine.copy();
          if (insnNode instanceof JumpInsnNode) {
            JumpInsnNode jumpInsnNode = (JumpInsnNode)insnNode;
            if (insnOpcode != 167 && insnOpcode != 168) {
              merge(insn + 1, current, subroutine);
              newControlFlowEdge(insn, insn + 1);
            } 
            int jump = this.insns.indexOf((AbstractInsnNode)jumpInsnNode.label);
            if (insnOpcode == 168) {
              merge(jump, current, new Subroutine(jumpInsnNode.label, m.maxLocals, jumpInsnNode));
            } else {
              merge(jump, current, subroutine);
            } 
            newControlFlowEdge(insn, jump);
          } else if (insnNode instanceof LookupSwitchInsnNode) {
            LookupSwitchInsnNode lsi = (LookupSwitchInsnNode)insnNode;
            int jump = this.insns.indexOf((AbstractInsnNode)lsi.dflt);
            merge(jump, current, subroutine);
            newControlFlowEdge(insn, jump);
            for (int n = 0; n < lsi.labels.size(); n++) {
              LabelNode label = lsi.labels.get(n);
              jump = this.insns.indexOf((AbstractInsnNode)label);
              merge(jump, current, subroutine);
              newControlFlowEdge(insn, jump);
            } 
          } else if (insnNode instanceof TableSwitchInsnNode) {
            TableSwitchInsnNode tsi = (TableSwitchInsnNode)insnNode;
            int jump = this.insns.indexOf((AbstractInsnNode)tsi.dflt);
            merge(jump, current, subroutine);
            newControlFlowEdge(insn, jump);
            for (int n = 0; n < tsi.labels.size(); n++) {
              LabelNode label = tsi.labels.get(n);
              jump = this.insns.indexOf((AbstractInsnNode)label);
              merge(jump, current, subroutine);
              newControlFlowEdge(insn, jump);
            } 
          } else if (insnOpcode == 169) {
            if (subroutine == null)
              throw new AnalyzerException(insnNode, "RET instruction outside of a sub routine"); 
            for (int n = 0; n < subroutine.callers.size(); n++) {
              JumpInsnNode caller = subroutine.callers.get(n);
              int call = this.insns.indexOf((AbstractInsnNode)caller);
              if (this.frames[call] != null) {
                merge(call + 1, this.frames[call], current, this.subroutines[call], subroutine.access);
                newControlFlowEdge(insn, call + 1);
              } 
            } 
          } else if (insnOpcode != 191 && (insnOpcode < 172 || insnOpcode > 177)) {
            if (subroutine != null)
              if (insnNode instanceof VarInsnNode) {
                int var = ((VarInsnNode)insnNode).var;
                subroutine.access[var] = true;
                if (insnOpcode == 22 || insnOpcode == 24 || insnOpcode == 55 || insnOpcode == 57)
                  subroutine.access[var + 1] = true; 
              } else if (insnNode instanceof IincInsnNode) {
                int var = ((IincInsnNode)insnNode).var;
                subroutine.access[var] = true;
              }  
            merge(insn + 1, current, subroutine);
            newControlFlowEdge(insn, insn + 1);
          } 
        } 
        List<TryCatchBlockNode> insnHandlers = this.handlers[insn];
        if (insnHandlers != null)
          for (int n = 0; n < insnHandlers.size(); n++) {
            Type type;
            TryCatchBlockNode tcb = insnHandlers.get(n);
            if (tcb.type == null) {
              type = Type.getObjectType("java/lang/Throwable");
            } else {
              type = Type.getObjectType(tcb.type);
            } 
            int jump = this.insns.indexOf((AbstractInsnNode)tcb.handler);
            if (newControlFlowExceptionEdge(insn, tcb)) {
              handler.init(f);
              handler.clearStack();
              handler.push(this.interpreter.newValue(type));
              merge(jump, handler, subroutine);
            } 
          }  
      } catch (AnalyzerException e) {
        throw new AnalyzerException(e.node, "Error at instruction " + insn + ": " + e
            .getMessage(), e);
      } catch (Exception e) {
        throw new AnalyzerException(insnNode, "Error at instruction " + insn + ": " + e
            .getMessage(), e);
      } 
    } 
    return this.frames;
  }
  
  private void findSubroutine(int insn, Subroutine sub, List<AbstractInsnNode> calls) throws AnalyzerException {
    while (true) {
      if (insn < 0 || insn >= this.n)
        throw new AnalyzerException(null, "Execution can fall off end of the code"); 
      if (this.subroutines[insn] != null)
        return; 
      this.subroutines[insn] = sub.copy();
      AbstractInsnNode node = this.insns.get(insn);
      if (node instanceof JumpInsnNode) {
        if (node.getOpcode() == 168) {
          calls.add(node);
        } else {
          JumpInsnNode jnode = (JumpInsnNode)node;
          findSubroutine(this.insns.indexOf((AbstractInsnNode)jnode.label), sub, calls);
        } 
      } else if (node instanceof TableSwitchInsnNode) {
        TableSwitchInsnNode tsnode = (TableSwitchInsnNode)node;
        findSubroutine(this.insns.indexOf((AbstractInsnNode)tsnode.dflt), sub, calls);
        for (int i = tsnode.labels.size() - 1; i >= 0; i--) {
          LabelNode l = tsnode.labels.get(i);
          findSubroutine(this.insns.indexOf((AbstractInsnNode)l), sub, calls);
        } 
      } else if (node instanceof LookupSwitchInsnNode) {
        LookupSwitchInsnNode lsnode = (LookupSwitchInsnNode)node;
        findSubroutine(this.insns.indexOf((AbstractInsnNode)lsnode.dflt), sub, calls);
        for (int i = lsnode.labels.size() - 1; i >= 0; i--) {
          LabelNode l = lsnode.labels.get(i);
          findSubroutine(this.insns.indexOf((AbstractInsnNode)l), sub, calls);
        } 
      } 
      List<TryCatchBlockNode> insnHandlers = this.handlers[insn];
      if (insnHandlers != null)
        for (int i = 0; i < insnHandlers.size(); i++) {
          TryCatchBlockNode tcb = insnHandlers.get(i);
          findSubroutine(this.insns.indexOf((AbstractInsnNode)tcb.handler), sub, calls);
        }  
      switch (node.getOpcode()) {
        case 167:
        case 169:
        case 170:
        case 171:
        case 172:
        case 173:
        case 174:
        case 175:
        case 176:
        case 177:
        case 191:
          return;
      } 
      insn++;
    } 
  }
  
  public Frame<V>[] getFrames() {
    return this.frames;
  }
  
  public List<TryCatchBlockNode> getHandlers(int insn) {
    return this.handlers[insn];
  }
  
  protected void init(String owner, MethodNode m) throws AnalyzerException {}
  
  protected Frame<V> newFrame(int nLocals, int nStack) {
    return new Frame<V>(nLocals, nStack);
  }
  
  protected Frame<V> newFrame(Frame<? extends V> src) {
    return new Frame<V>(src);
  }
  
  protected void newControlFlowEdge(int insn, int successor) {}
  
  protected boolean newControlFlowExceptionEdge(int insn, int successor) {
    return true;
  }
  
  protected boolean newControlFlowExceptionEdge(int insn, TryCatchBlockNode tcb) {
    return newControlFlowExceptionEdge(insn, this.insns.indexOf((AbstractInsnNode)tcb.handler));
  }
  
  private void merge(int insn, Frame<V> frame, Subroutine subroutine) throws AnalyzerException {
    boolean bool;
    Frame<V> oldFrame = this.frames[insn];
    Subroutine oldSubroutine = this.subroutines[insn];
    if (oldFrame == null) {
      this.frames[insn] = newFrame(frame);
      bool = true;
    } else {
      bool = oldFrame.merge(frame, this.interpreter);
    } 
    if (oldSubroutine == null) {
      if (subroutine != null) {
        this.subroutines[insn] = subroutine.copy();
        bool = true;
      } 
    } else if (subroutine != null) {
      bool |= oldSubroutine.merge(subroutine);
    } 
    if (bool && !this.queued[insn]) {
      this.queued[insn] = true;
      this.queue[this.top++] = insn;
    } 
  }
  
  private void merge(int insn, Frame<V> beforeJSR, Frame<V> afterRET, Subroutine subroutineBeforeJSR, boolean[] access) throws AnalyzerException {
    boolean changes;
    Frame<V> oldFrame = this.frames[insn];
    Subroutine oldSubroutine = this.subroutines[insn];
    afterRET.merge(beforeJSR, access);
    if (oldFrame == null) {
      this.frames[insn] = newFrame(afterRET);
      changes = true;
    } else {
      changes = oldFrame.merge(afterRET, this.interpreter);
    } 
    if (oldSubroutine != null && subroutineBeforeJSR != null)
      changes |= oldSubroutine.merge(subroutineBeforeJSR); 
    if (changes && !this.queued[insn]) {
      this.queued[insn] = true;
      this.queue[this.top++] = insn;
    } 
  }
}
