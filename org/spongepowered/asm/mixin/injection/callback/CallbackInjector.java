package org.spongepowered.asm.mixin.injection.callback;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.points.BeforeReturn;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.injection.throwables.InjectionError;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.util.Locals;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.SignaturePrinter;

public class CallbackInjector extends Injector {
  private final boolean cancellable;
  
  private final LocalCapture localCapture;
  
  private final String identifier;
  
  private class Callback extends InsnList {
    private final MethodNode handler;
    
    private final AbstractInsnNode head;
    
    final Target target;
    
    final InjectionNodes.InjectionNode node;
    
    final LocalVariableNode[] locals;
    
    final Type[] localTypes;
    
    final int frameSize;
    
    final int extraArgs;
    
    final boolean canCaptureLocals;
    
    final boolean isAtReturn;
    
    final String desc;
    
    final String descl;
    
    final String[] argNames;
    
    int ctor;
    
    int invoke;
    
    private int marshalVar = -1;
    
    private boolean captureArgs = true;
    
    Callback(MethodNode handler, Target target, InjectionNodes.InjectionNode node, LocalVariableNode[] locals, boolean captureLocals) {
      this.handler = handler;
      this.target = target;
      this.head = target.insns.getFirst();
      this.node = node;
      this.locals = locals;
      this.localTypes = (locals != null) ? new Type[locals.length] : null;
      this.frameSize = Bytecode.getFirstNonArgLocalIndex(target.arguments, !CallbackInjector.this.isStatic());
      List<String> argNames = null;
      if (locals != null) {
        int baseArgIndex = CallbackInjector.this.isStatic() ? 0 : 1;
        argNames = new ArrayList<String>();
        for (int l = 0; l <= locals.length; l++) {
          if (l == this.frameSize)
            argNames.add((target.returnType == Type.VOID_TYPE) ? "ci" : "cir"); 
          if (l < locals.length && locals[l] != null) {
            this.localTypes[l] = Type.getType((locals[l]).desc);
            if (l >= baseArgIndex)
              argNames.add(CallbackInjector.meltSnowman(l, (locals[l]).name)); 
          } 
        } 
      } 
      this.extraArgs = Math.max(0, Bytecode.getFirstNonArgLocalIndex(this.handler) - this.frameSize + 1);
      this.argNames = (argNames != null) ? argNames.<String>toArray(new String[argNames.size()]) : null;
      this.canCaptureLocals = (captureLocals && locals != null && locals.length > this.frameSize);
      this.isAtReturn = (this.node.getCurrentTarget() instanceof InsnNode && isValueReturnOpcode(this.node.getCurrentTarget().getOpcode()));
      this.desc = target.getCallbackDescriptor(this.localTypes, target.arguments);
      this.descl = target.getCallbackDescriptor(true, this.localTypes, target.arguments, this.frameSize, this.extraArgs);
      this.invoke = target.arguments.length + (this.canCaptureLocals ? (this.localTypes.length - this.frameSize) : 0);
    }
    
    private boolean isValueReturnOpcode(int opcode) {
      return (opcode >= 172 && opcode < 177);
    }
    
    String getDescriptor() {
      return this.canCaptureLocals ? this.descl : this.desc;
    }
    
    String getDescriptorWithAllLocals() {
      return this.target.getCallbackDescriptor(true, this.localTypes, this.target.arguments, this.frameSize, 32767);
    }
    
    String getCallbackInfoConstructorDescriptor() {
      return this.isAtReturn ? CallbackInfo.getConstructorDescriptor(this.target.returnType) : CallbackInfo.getConstructorDescriptor();
    }
    
    void add(AbstractInsnNode insn, boolean ctorStack, boolean invokeStack) {
      add(insn, ctorStack, invokeStack, false);
    }
    
    void add(AbstractInsnNode insn, boolean ctorStack, boolean invokeStack, boolean head) {
      if (head) {
        this.target.insns.insertBefore(this.head, insn);
      } else {
        add(insn);
      } 
      this.ctor += ctorStack ? 1 : 0;
      this.invoke += invokeStack ? 1 : 0;
    }
    
    void inject() {
      this.target.insertBefore(this.node, this);
      this.target.addToStack(Math.max(this.invoke, this.ctor));
    }
    
    boolean checkDescriptor(String desc) {
      if (getDescriptor().equals(desc))
        return true; 
      if (this.target.getSimpleCallbackDescriptor().equals(desc) && !this.canCaptureLocals) {
        this.captureArgs = false;
        return true;
      } 
      Type[] inTypes = Type.getArgumentTypes(desc);
      Type[] myTypes = Type.getArgumentTypes(this.descl);
      if (inTypes.length != myTypes.length)
        return false; 
      for (int arg = 0; arg < myTypes.length; arg++) {
        Type type = inTypes[arg];
        if (!type.equals(myTypes[arg])) {
          if (type.getSort() == 9)
            return false; 
          if (Annotations.getInvisibleParameter(this.handler, Coerce.class, arg) == null)
            return false; 
          if (!Injector.canCoerce(inTypes[arg], myTypes[arg]))
            return false; 
        } 
      } 
      return true;
    }
    
    boolean captureArgs() {
      return this.captureArgs;
    }
    
    int marshalVar() {
      if (this.marshalVar < 0)
        this.marshalVar = this.target.allocateLocal(); 
      return this.marshalVar;
    }
  }
  
  private final Map<Integer, String> ids = new HashMap<Integer, String>();
  
  private int totalInjections = 0;
  
  private int callbackInfoVar = -1;
  
  private String lastId;
  
  private String lastDesc;
  
  private Target lastTarget;
  
  private String callbackInfoClass;
  
  public CallbackInjector(InjectionInfo info, boolean cancellable, LocalCapture localCapture, String identifier) {
    super(info);
    this.cancellable = cancellable;
    this.localCapture = localCapture;
    this.identifier = identifier;
  }
  
  protected void sanityCheck(Target target, List<InjectionPoint> injectionPoints) {
    super.sanityCheck(target, injectionPoints);
    if (target.isStatic != this.isStatic)
      throw new InvalidInjectionException(this.info, "'static' modifier of callback method does not match target in " + this); 
    if ("<init>".equals(target.method.name))
      for (InjectionPoint injectionPoint : injectionPoints) {
        if (!injectionPoint.getClass().equals(BeforeReturn.class))
          throw new InvalidInjectionException(this.info, "Found injection point type " + injectionPoint.getClass().getSimpleName() + " targetting a ctor in " + this + ". Only RETURN allowed for a ctor target"); 
      }  
  }
  
  protected void addTargetNode(Target target, List<InjectionNodes.InjectionNode> myNodes, AbstractInsnNode node, Set<InjectionPoint> nominators) {
    InjectionNodes.InjectionNode injectionNode = target.addInjectionNode(node);
    for (InjectionPoint ip : nominators) {
      String id = ip.getId();
      if (Strings.isNullOrEmpty(id))
        continue; 
      String existingId = this.ids.get(Integer.valueOf(injectionNode.getId()));
      if (existingId != null && !existingId.equals(id)) {
        Injector.logger.warn("Conflicting id for {} insn in {}, found id {} on {}, previously defined as {}", new Object[] { Bytecode.getOpcodeName(node), target
              .toString(), id, this.info, existingId });
        break;
      } 
      this.ids.put(Integer.valueOf(injectionNode.getId()), id);
    } 
    myNodes.add(injectionNode);
    this.totalInjections++;
  }
  
  protected void inject(Target target, InjectionNodes.InjectionNode node) {
    LocalVariableNode[] locals = null;
    if (this.localCapture.isCaptureLocals() || this.localCapture.isPrintLocals())
      locals = Locals.getLocalsAt(this.classNode, target.method, node.getCurrentTarget()); 
    inject(new Callback(this.methodNode, target, node, locals, this.localCapture.isCaptureLocals()));
  }
  
  private void inject(Callback callback) {
    if (this.localCapture.isPrintLocals()) {
      printLocals(callback);
      this.info.addCallbackInvocation(this.methodNode);
      return;
    } 
    MethodNode callbackMethod = this.methodNode;
    if (!callback.checkDescriptor(this.methodNode.desc)) {
      if (this.info.getTargets().size() > 1)
        return; 
      if (callback.canCaptureLocals) {
        MethodNode surrogateHandler = Bytecode.findMethod(this.classNode, this.methodNode.name, callback.getDescriptor());
        if (surrogateHandler != null && Annotations.getVisible(surrogateHandler, Surrogate.class) != null) {
          callbackMethod = surrogateHandler;
        } else {
          String message = generateBadLVTMessage(callback);
          switch (this.localCapture) {
            case CAPTURE_FAILEXCEPTION:
              Injector.logger.error("Injection error: {}", new Object[] { message });
              callbackMethod = generateErrorMethod(callback, "org/spongepowered/asm/mixin/injection/throwables/InjectionError", message);
              break;
            case CAPTURE_FAILSOFT:
              Injector.logger.warn("Injection warning: {}", new Object[] { message });
              return;
            default:
              Injector.logger.error("Critical injection failure: {}", new Object[] { message });
              throw new InjectionError(message);
          } 
        } 
      } else {
        String returnableSig = this.methodNode.desc.replace("Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;", "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;");
        if (callback.checkDescriptor(returnableSig))
          throw new InvalidInjectionException(this.info, "Invalid descriptor on " + this.info + "! CallbackInfoReturnable is required!"); 
        MethodNode surrogateHandler = Bytecode.findMethod(this.classNode, this.methodNode.name, callback.getDescriptor());
        if (surrogateHandler != null && Annotations.getVisible(surrogateHandler, Surrogate.class) != null) {
          callbackMethod = surrogateHandler;
        } else {
          throw new InvalidInjectionException(this.info, "Invalid descriptor on " + this.info + "! Expected " + callback.getDescriptor() + " but found " + this.methodNode.desc);
        } 
      } 
    } 
    dupReturnValue(callback);
    if (this.cancellable || this.totalInjections > 1)
      createCallbackInfo(callback, true); 
    invokeCallback(callback, callbackMethod);
    injectCancellationCode(callback);
    callback.inject();
    this.info.notifyInjected(callback.target);
  }
  
  private String generateBadLVTMessage(Callback callback) {
    int position = callback.target.indexOf(callback.node);
    List<String> expected = summariseLocals(this.methodNode.desc, callback.target.arguments.length + 1);
    List<String> found = summariseLocals(callback.getDescriptorWithAllLocals(), callback.frameSize);
    return String.format("LVT in %s has incompatible changes at opcode %d in callback %s.\nExpected: %s\n   Found: %s", new Object[] { callback.target, 
          Integer.valueOf(position), this, expected, found });
  }
  
  private MethodNode generateErrorMethod(Callback callback, String errorClass, String message) {
    MethodNode method = this.info.addMethod(this.methodNode.access, this.methodNode.name + "$missing", callback.getDescriptor());
    method.maxLocals = Bytecode.getFirstNonArgLocalIndex(Type.getArgumentTypes(callback.getDescriptor()), !this.isStatic);
    method.maxStack = 3;
    InsnList insns = method.instructions;
    insns.add((AbstractInsnNode)new TypeInsnNode(187, errorClass));
    insns.add((AbstractInsnNode)new InsnNode(89));
    insns.add((AbstractInsnNode)new LdcInsnNode(message));
    insns.add((AbstractInsnNode)new MethodInsnNode(183, errorClass, "<init>", "(Ljava/lang/String;)V", false));
    insns.add((AbstractInsnNode)new InsnNode(191));
    return method;
  }
  
  private void printLocals(Callback callback) {
    Type[] args = Type.getArgumentTypes(callback.getDescriptorWithAllLocals());
    SignaturePrinter methodSig = new SignaturePrinter(callback.target.method, callback.argNames);
    SignaturePrinter handlerSig = new SignaturePrinter(this.methodNode.name, callback.target.returnType, args, callback.argNames);
    handlerSig.setModifiers(this.methodNode);
    PrettyPrinter printer = new PrettyPrinter();
    printer.kv("Target Class", this.classNode.name.replace('/', '.'));
    printer.kv("Target Method", methodSig);
    printer.kv("Target Max LOCALS", Integer.valueOf(callback.target.getMaxLocals()));
    printer.kv("Initial Frame Size", Integer.valueOf(callback.frameSize));
    printer.kv("Callback Name", this.methodNode.name);
    printer.kv("Instruction", "%s %s", new Object[] { callback.node.getClass().getSimpleName(), 
          Bytecode.getOpcodeName(callback.node.getCurrentTarget().getOpcode()) });
    printer.hr();
    if (callback.locals.length > callback.frameSize) {
      printer.add("  %s  %20s  %s", new Object[] { "LOCAL", "TYPE", "NAME" });
      for (int l = 0; l < callback.locals.length; l++) {
        String marker = (l == callback.frameSize) ? ">" : " ";
        if (callback.locals[l] != null) {
          printer.add("%s [%3d]  %20s  %-50s %s", new Object[] { marker, Integer.valueOf(l), SignaturePrinter.getTypeName(callback.localTypes[l], false), 
                meltSnowman(l, (callback.locals[l]).name), (l >= callback.frameSize) ? "<capture>" : "" });
        } else {
          boolean isTop = (l > 0 && callback.localTypes[l - 1] != null && callback.localTypes[l - 1].getSize() > 1);
          printer.add("%s [%3d]  %20s", new Object[] { marker, Integer.valueOf(l), isTop ? "<top>" : "-" });
        } 
      } 
      printer.hr();
    } 
    printer.add().add("/**").add(" * Expected callback signature").add(" * /");
    printer.add("%s {", new Object[] { handlerSig });
    printer.add("    // Method body").add("}").add().print(System.err);
  }
  
  private void createCallbackInfo(Callback callback, boolean store) {
    if (callback.target != this.lastTarget) {
      this.lastId = null;
      this.lastDesc = null;
    } 
    this.lastTarget = callback.target;
    String id = getIdentifier(callback);
    String desc = callback.getCallbackInfoConstructorDescriptor();
    if (id.equals(this.lastId) && desc.equals(this.lastDesc) && !callback.isAtReturn && !this.cancellable)
      return; 
    instanceCallbackInfo(callback, id, desc, store);
  }
  
  private void loadOrCreateCallbackInfo(Callback callback) {
    if (this.cancellable || this.totalInjections > 1) {
      callback.add((AbstractInsnNode)new VarInsnNode(25, this.callbackInfoVar), false, true);
    } else {
      createCallbackInfo(callback, false);
    } 
  }
  
  private void dupReturnValue(Callback callback) {
    if (!callback.isAtReturn)
      return; 
    callback.add((AbstractInsnNode)new InsnNode(89));
    callback.add((AbstractInsnNode)new VarInsnNode(callback.target.returnType.getOpcode(54), callback.marshalVar()));
  }
  
  protected void instanceCallbackInfo(Callback callback, String id, String desc, boolean store) {
    this.lastId = id;
    this.lastDesc = desc;
    this.callbackInfoVar = callback.marshalVar();
    this.callbackInfoClass = callback.target.getCallbackInfoClass();
    boolean head = (store && this.totalInjections > 1 && !callback.isAtReturn && !this.cancellable);
    callback.add((AbstractInsnNode)new TypeInsnNode(187, this.callbackInfoClass), true, !store, head);
    callback.add((AbstractInsnNode)new InsnNode(89), true, true, head);
    callback.add((AbstractInsnNode)new LdcInsnNode(id), true, !store, head);
    callback.add((AbstractInsnNode)new InsnNode(this.cancellable ? 4 : 3), true, !store, head);
    if (callback.isAtReturn) {
      callback.add((AbstractInsnNode)new VarInsnNode(callback.target.returnType.getOpcode(21), callback.marshalVar()), true, !store);
      callback.add((AbstractInsnNode)new MethodInsnNode(183, this.callbackInfoClass, "<init>", desc, false));
    } else {
      callback.add((AbstractInsnNode)new MethodInsnNode(183, this.callbackInfoClass, "<init>", desc, false), false, false, head);
    } 
    if (store) {
      callback.target.addLocalVariable(this.callbackInfoVar, "callbackInfo" + this.callbackInfoVar, "L" + this.callbackInfoClass + ";");
      callback.add((AbstractInsnNode)new VarInsnNode(58, this.callbackInfoVar), false, false, head);
    } 
  }
  
  private void invokeCallback(Callback callback, MethodNode callbackMethod) {
    if (!this.isStatic)
      callback.add((AbstractInsnNode)new VarInsnNode(25, 0), false, true); 
    if (callback.captureArgs())
      Bytecode.loadArgs(callback.target.arguments, callback, this.isStatic ? 0 : 1, -1); 
    loadOrCreateCallbackInfo(callback);
    if (callback.canCaptureLocals)
      Locals.loadLocals(callback.localTypes, callback, callback.frameSize, callback.extraArgs); 
    invokeHandler(callback, callbackMethod);
  }
  
  private String getIdentifier(Callback callback) {
    String baseId = Strings.isNullOrEmpty(this.identifier) ? callback.target.method.name : this.identifier;
    String locationId = this.ids.get(Integer.valueOf(callback.node.getId()));
    return baseId + (Strings.isNullOrEmpty(locationId) ? "" : (":" + locationId));
  }
  
  protected void injectCancellationCode(Callback callback) {
    if (!this.cancellable)
      return; 
    callback.add((AbstractInsnNode)new VarInsnNode(25, this.callbackInfoVar));
    callback.add((AbstractInsnNode)new MethodInsnNode(182, this.callbackInfoClass, CallbackInfo.getIsCancelledMethodName(), 
          CallbackInfo.getIsCancelledMethodSig(), false));
    LabelNode notCancelled = new LabelNode();
    callback.add((AbstractInsnNode)new JumpInsnNode(153, notCancelled));
    injectReturnCode(callback);
    callback.add((AbstractInsnNode)notCancelled);
  }
  
  protected void injectReturnCode(Callback callback) {
    if (callback.target.returnType.equals(Type.VOID_TYPE)) {
      callback.add((AbstractInsnNode)new InsnNode(177));
    } else {
      callback.add((AbstractInsnNode)new VarInsnNode(25, callback.marshalVar()));
      String accessor = CallbackInfoReturnable.getReturnAccessor(callback.target.returnType);
      String descriptor = CallbackInfoReturnable.getReturnDescriptor(callback.target.returnType);
      callback.add((AbstractInsnNode)new MethodInsnNode(182, this.callbackInfoClass, accessor, descriptor, false));
      if (callback.target.returnType.getSort() == 10)
        callback.add((AbstractInsnNode)new TypeInsnNode(192, callback.target.returnType.getInternalName())); 
      callback.add((AbstractInsnNode)new InsnNode(callback.target.returnType.getOpcode(172)));
    } 
  }
  
  protected boolean isStatic() {
    return this.isStatic;
  }
  
  private static List<String> summariseLocals(String desc, int pos) {
    return summariseLocals(Type.getArgumentTypes(desc), pos);
  }
  
  private static List<String> summariseLocals(Type[] locals, int pos) {
    List<String> list = new ArrayList<String>();
    if (locals != null)
      for (; pos < locals.length; pos++) {
        if (locals[pos] != null)
          list.add(locals[pos].toString()); 
      }  
    return list;
  }
  
  static String meltSnowman(int index, String varName) {
    return (varName != null && '☃' == varName.charAt(0)) ? ("var" + index) : varName;
  }
}
