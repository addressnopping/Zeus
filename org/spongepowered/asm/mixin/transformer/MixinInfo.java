package org.spongepowered.asm.mixin.transformer;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.InnerClassNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.mixin.transformer.throwables.MixinReloadException;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTargetAlreadyLoadedException;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.util.perf.Profiler;

class MixinInfo implements Comparable<MixinInfo>, IMixinInfo {
  class MixinMethodNode extends MethodNode {
    private final String originalName;
    
    public MixinMethodNode(int access, String name, String desc, String signature, String[] exceptions) {
      super(327680, access, name, desc, signature, exceptions);
      this.originalName = name;
    }
    
    public String toString() {
      return String.format("%s%s", new Object[] { this.originalName, this.desc });
    }
    
    public String getOriginalName() {
      return this.originalName;
    }
    
    public boolean isInjector() {
      return (getInjectorAnnotation() != null || isSurrogate());
    }
    
    public boolean isSurrogate() {
      return (getVisibleAnnotation((Class)Surrogate.class) != null);
    }
    
    public boolean isSynthetic() {
      return Bytecode.hasFlag(this, 4096);
    }
    
    public AnnotationNode getVisibleAnnotation(Class<? extends Annotation> annotationClass) {
      return Annotations.getVisible(this, annotationClass);
    }
    
    public AnnotationNode getInjectorAnnotation() {
      return InjectionInfo.getInjectorAnnotation(MixinInfo.this, this);
    }
    
    public IMixinInfo getOwner() {
      return MixinInfo.this;
    }
  }
  
  class MixinClassNode extends ClassNode {
    public final List<MixinInfo.MixinMethodNode> mixinMethods;
    
    public MixinClassNode(MixinInfo mixin) {
      this(327680);
    }
    
    public MixinClassNode(int api) {
      super(api);
      this.mixinMethods = this.methods;
    }
    
    public MixinInfo getMixin() {
      return MixinInfo.this;
    }
    
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      MethodNode method = new MixinInfo.MixinMethodNode(access, name, desc, signature, exceptions);
      this.methods.add(method);
      return (MethodVisitor)method;
    }
  }
  
  class State {
    private byte[] mixinBytes;
    
    private final ClassInfo classInfo;
    
    private boolean detachedSuper;
    
    private boolean unique;
    
    protected final Set<String> interfaces = new HashSet<String>();
    
    protected final List<InterfaceInfo> softImplements = new ArrayList<InterfaceInfo>();
    
    protected final Set<String> syntheticInnerClasses = new HashSet<String>();
    
    protected final Set<String> innerClasses = new HashSet<String>();
    
    protected MixinInfo.MixinClassNode classNode;
    
    State(byte[] mixinBytes) {
      this(mixinBytes, null);
    }
    
    State(byte[] mixinBytes, ClassInfo classInfo) {
      this.mixinBytes = mixinBytes;
      connect();
      this.classInfo = (classInfo != null) ? classInfo : ClassInfo.fromClassNode(getClassNode());
    }
    
    private void connect() {
      this.classNode = createClassNode(0);
    }
    
    private void complete() {
      this.classNode = null;
    }
    
    ClassInfo getClassInfo() {
      return this.classInfo;
    }
    
    byte[] getClassBytes() {
      return this.mixinBytes;
    }
    
    MixinInfo.MixinClassNode getClassNode() {
      return this.classNode;
    }
    
    boolean isDetachedSuper() {
      return this.detachedSuper;
    }
    
    boolean isUnique() {
      return this.unique;
    }
    
    List<? extends InterfaceInfo> getSoftImplements() {
      return this.softImplements;
    }
    
    Set<String> getSyntheticInnerClasses() {
      return this.syntheticInnerClasses;
    }
    
    Set<String> getInnerClasses() {
      return this.innerClasses;
    }
    
    Set<String> getInterfaces() {
      return this.interfaces;
    }
    
    MixinInfo.MixinClassNode createClassNode(int flags) {
      MixinInfo.MixinClassNode classNode = new MixinInfo.MixinClassNode(MixinInfo.this);
      ClassReader classReader = new ClassReader(this.mixinBytes);
      classReader.accept((ClassVisitor)classNode, flags);
      return classNode;
    }
    
    void validate(MixinInfo.SubType type, List<ClassInfo> targetClasses) {
      MixinPreProcessorStandard preProcessor = type.createPreProcessor(getClassNode()).prepare();
      for (ClassInfo target : targetClasses)
        preProcessor.conform(target); 
      type.validate(this, targetClasses);
      this.detachedSuper = type.isDetachedSuper();
      this.unique = (Annotations.getVisible(getClassNode(), Unique.class) != null);
      validateInner();
      validateClassVersion();
      validateRemappables(targetClasses);
      readImplementations(type);
      readInnerClasses();
      validateChanges(type, targetClasses);
      complete();
    }
    
    private void validateInner() {
      if (!this.classInfo.isProbablyStatic())
        throw new InvalidMixinException(MixinInfo.this, "Inner class mixin must be declared static"); 
    }
    
    private void validateClassVersion() {
      if (this.classNode.version > MixinEnvironment.getCompatibilityLevel().classVersion()) {
        String helpText = ".";
        for (MixinEnvironment.CompatibilityLevel level : MixinEnvironment.CompatibilityLevel.values()) {
          if (level.classVersion() >= this.classNode.version)
            helpText = String.format(". Mixin requires compatibility level %s or above.", new Object[] { level.name() }); 
        } 
        throw new InvalidMixinException(MixinInfo.this, "Unsupported mixin class version " + this.classNode.version + helpText);
      } 
    }
    
    private void validateRemappables(List<ClassInfo> targetClasses) {
      if (targetClasses.size() > 1) {
        for (FieldNode field : this.classNode.fields)
          validateRemappable(Shadow.class, field.name, Annotations.getVisible(field, Shadow.class)); 
        for (MethodNode method : this.classNode.methods) {
          validateRemappable(Shadow.class, method.name, Annotations.getVisible(method, Shadow.class));
          AnnotationNode overwrite = Annotations.getVisible(method, Overwrite.class);
          if (overwrite != null && ((method.access & 0x8) == 0 || (method.access & 0x1) == 0))
            throw new InvalidMixinException(MixinInfo.this, "Found @Overwrite annotation on " + method.name + " in " + MixinInfo.this); 
        } 
      } 
    }
    
    private void validateRemappable(Class<Shadow> annotationClass, String name, AnnotationNode annotation) {
      if (annotation != null && ((Boolean)Annotations.getValue(annotation, "remap", Boolean.TRUE)).booleanValue())
        throw new InvalidMixinException(MixinInfo.this, "Found a remappable @" + annotationClass.getSimpleName() + " annotation on " + name + " in " + this); 
    }
    
    void readImplementations(MixinInfo.SubType type) {
      this.interfaces.addAll(this.classNode.interfaces);
      this.interfaces.addAll(type.getInterfaces());
      AnnotationNode implementsAnnotation = Annotations.getInvisible(this.classNode, Implements.class);
      if (implementsAnnotation == null)
        return; 
      List<AnnotationNode> interfaces = (List<AnnotationNode>)Annotations.getValue(implementsAnnotation);
      if (interfaces == null)
        return; 
      for (AnnotationNode interfaceNode : interfaces) {
        InterfaceInfo interfaceInfo = InterfaceInfo.fromAnnotation(MixinInfo.this, interfaceNode);
        this.softImplements.add(interfaceInfo);
        this.interfaces.add(interfaceInfo.getInternalName());
        if (!(this instanceof MixinInfo.Reloaded))
          this.classInfo.addInterface(interfaceInfo.getInternalName()); 
      } 
    }
    
    void readInnerClasses() {
      for (InnerClassNode inner : this.classNode.innerClasses) {
        ClassInfo innerClass = ClassInfo.forName(inner.name);
        if ((inner.outerName != null && inner.outerName.equals(this.classInfo.getName())) || inner.name
          .startsWith(this.classNode.name + "$")) {
          if (innerClass.isProbablyStatic() && innerClass.isSynthetic()) {
            this.syntheticInnerClasses.add(inner.name);
            continue;
          } 
          this.innerClasses.add(inner.name);
        } 
      } 
    }
    
    protected void validateChanges(MixinInfo.SubType type, List<ClassInfo> targetClasses) {
      type.createPreProcessor(this.classNode).prepare();
    }
  }
  
  class Reloaded extends State {
    private final MixinInfo.State previous;
    
    Reloaded(MixinInfo.State previous, byte[] mixinBytes) {
      super(mixinBytes, previous.getClassInfo());
      this.previous = previous;
    }
    
    protected void validateChanges(MixinInfo.SubType type, List<ClassInfo> targetClasses) {
      if (!this.syntheticInnerClasses.equals(this.previous.syntheticInnerClasses))
        throw new MixinReloadException(MixinInfo.this, "Cannot change inner classes"); 
      if (!this.interfaces.equals(this.previous.interfaces))
        throw new MixinReloadException(MixinInfo.this, "Cannot change interfaces"); 
      if (!(new HashSet(this.softImplements)).equals(new HashSet<InterfaceInfo>(this.previous.softImplements)))
        throw new MixinReloadException(MixinInfo.this, "Cannot change soft interfaces"); 
      List<ClassInfo> targets = MixinInfo.this.readTargetClasses(this.classNode, true);
      if (!(new HashSet(targets)).equals(new HashSet<ClassInfo>(targetClasses)))
        throw new MixinReloadException(MixinInfo.this, "Cannot change target classes"); 
      int priority = MixinInfo.this.readPriority(this.classNode);
      if (priority != MixinInfo.this.getPriority())
        throw new MixinReloadException(MixinInfo.this, "Cannot change mixin priority"); 
    }
  }
  
  static abstract class SubType {
    protected final MixinInfo mixin;
    
    protected final String annotationType;
    
    protected final boolean targetMustBeInterface;
    
    protected boolean detached;
    
    SubType(MixinInfo info, String annotationType, boolean targetMustBeInterface) {
      this.mixin = info;
      this.annotationType = annotationType;
      this.targetMustBeInterface = targetMustBeInterface;
    }
    
    Collection<String> getInterfaces() {
      return Collections.emptyList();
    }
    
    boolean isDetachedSuper() {
      return this.detached;
    }
    
    boolean isLoadable() {
      return false;
    }
    
    void validateTarget(String targetName, ClassInfo targetInfo) {
      boolean targetIsInterface = targetInfo.isInterface();
      if (targetIsInterface != this.targetMustBeInterface) {
        String not = targetIsInterface ? "" : "not ";
        throw new InvalidMixinException(this.mixin, this.annotationType + " target type mismatch: " + targetName + " is " + not + "an interface in " + this);
      } 
    }
    
    abstract void validate(MixinInfo.State param1State, List<ClassInfo> param1List);
    
    abstract MixinPreProcessorStandard createPreProcessor(MixinInfo.MixinClassNode param1MixinClassNode);
    
    static class Standard extends SubType {
      Standard(MixinInfo info) {
        super(info, "@Mixin", false);
      }
      
      void validate(MixinInfo.State state, List<ClassInfo> targetClasses) {
        ClassNode classNode = state.getClassNode();
        for (ClassInfo targetClass : targetClasses) {
          if (classNode.superName.equals(targetClass.getSuperName()))
            continue; 
          if (!targetClass.hasSuperClass(classNode.superName, ClassInfo.Traversal.SUPER)) {
            ClassInfo superClass = ClassInfo.forName(classNode.superName);
            if (superClass.isMixin())
              for (ClassInfo superTarget : superClass.getTargets()) {
                if (targetClasses.contains(superTarget))
                  throw new InvalidMixinException(this.mixin, "Illegal hierarchy detected. Derived mixin " + this + " targets the same class " + superTarget
                      .getClassName() + " as its superclass " + superClass
                      .getClassName()); 
              }  
            throw new InvalidMixinException(this.mixin, "Super class '" + classNode.superName.replace('/', '.') + "' of " + this.mixin
                .getName() + " was not found in the hierarchy of target class '" + targetClass + "'");
          } 
          this.detached = true;
        } 
      }
      
      MixinPreProcessorStandard createPreProcessor(MixinInfo.MixinClassNode classNode) {
        return new MixinPreProcessorStandard(this.mixin, classNode);
      }
    }
    
    static class Interface extends SubType {
      Interface(MixinInfo info) {
        super(info, "@Mixin", true);
      }
      
      void validate(MixinInfo.State state, List<ClassInfo> targetClasses) {
        if (!MixinEnvironment.getCompatibilityLevel().supportsMethodsInInterfaces())
          throw new InvalidMixinException(this.mixin, "Interface mixin not supported in current enviromnment"); 
        ClassNode classNode = state.getClassNode();
        if (!"java/lang/Object".equals(classNode.superName))
          throw new InvalidMixinException(this.mixin, "Super class of " + this + " is invalid, found " + classNode.superName
              .replace('/', '.')); 
      }
      
      MixinPreProcessorStandard createPreProcessor(MixinInfo.MixinClassNode classNode) {
        return new MixinPreProcessorInterface(this.mixin, classNode);
      }
    }
    
    static class Accessor extends SubType {
      private final Collection<String> interfaces = new ArrayList<String>();
      
      Accessor(MixinInfo info) {
        super(info, "@Mixin", false);
        this.interfaces.add(info.getClassRef());
      }
      
      boolean isLoadable() {
        return true;
      }
      
      Collection<String> getInterfaces() {
        return this.interfaces;
      }
      
      void validateTarget(String targetName, ClassInfo targetInfo) {
        boolean targetIsInterface = targetInfo.isInterface();
        if (targetIsInterface && !MixinEnvironment.getCompatibilityLevel().supportsMethodsInInterfaces())
          throw new InvalidMixinException(this.mixin, "Accessor mixin targetting an interface is not supported in current enviromnment"); 
      }
      
      void validate(MixinInfo.State state, List<ClassInfo> targetClasses) {
        ClassNode classNode = state.getClassNode();
        if (!"java/lang/Object".equals(classNode.superName))
          throw new InvalidMixinException(this.mixin, "Super class of " + this + " is invalid, found " + classNode.superName
              .replace('/', '.')); 
      }
      
      MixinPreProcessorStandard createPreProcessor(MixinInfo.MixinClassNode classNode) {
        return new MixinPreProcessorAccessor(this.mixin, classNode);
      }
    }
    
    static SubType getTypeFor(MixinInfo mixin) {
      int i;
      if (!mixin.getClassInfo().isInterface())
        return new Standard(mixin); 
      boolean containsNonAccessorMethod = false;
      for (ClassInfo.Method method : mixin.getClassInfo().getMethods())
        i = containsNonAccessorMethod | (!method.isAccessor() ? 1 : 0); 
      if (i != 0)
        return new Interface(mixin); 
      return new Accessor(mixin);
    }
  }
  
  static class Accessor extends SubType {
    private final Collection<String> interfaces = new ArrayList<String>();
    
    Accessor(MixinInfo info) {
      super(info, "@Mixin", false);
      this.interfaces.add(info.getClassRef());
    }
    
    boolean isLoadable() {
      return true;
    }
    
    Collection<String> getInterfaces() {
      return this.interfaces;
    }
    
    void validateTarget(String targetName, ClassInfo targetInfo) {
      boolean targetIsInterface = targetInfo.isInterface();
      if (targetIsInterface && !MixinEnvironment.getCompatibilityLevel().supportsMethodsInInterfaces())
        throw new InvalidMixinException(this.mixin, "Accessor mixin targetting an interface is not supported in current enviromnment"); 
    }
    
    void validate(MixinInfo.State state, List<ClassInfo> targetClasses) {
      ClassNode classNode = state.getClassNode();
      if (!"java/lang/Object".equals(classNode.superName))
        throw new InvalidMixinException(this.mixin, "Super class of " + this + " is invalid, found " + classNode.superName.replace('/', '.')); 
    }
    
    MixinPreProcessorStandard createPreProcessor(MixinInfo.MixinClassNode classNode) {
      return new MixinPreProcessorAccessor(this.mixin, classNode);
    }
  }
  
  private static final IMixinService classLoaderUtil = MixinService.getService();
  
  static int mixinOrder = 0;
  
  private final transient Logger logger = LogManager.getLogger("mixin");
  
  private final transient Profiler profiler = MixinEnvironment.getProfiler();
  
  private final transient MixinConfig parent;
  
  private final String name;
  
  private final String className;
  
  private final int priority;
  
  private final boolean virtual;
  
  private final List<ClassInfo> targetClasses;
  
  private final List<String> targetClassNames;
  
  private final transient int order = mixinOrder++;
  
  private final transient IMixinService service;
  
  private final transient IMixinConfigPlugin plugin;
  
  private final transient MixinEnvironment.Phase phase;
  
  private final transient ClassInfo info;
  
  private final transient SubType type;
  
  private final transient boolean strict;
  
  private transient State pendingState;
  
  private transient State state;
  
  MixinInfo(IMixinService service, MixinConfig parent, String name, boolean runTransformers, IMixinConfigPlugin plugin, boolean suppressPlugin) {
    this.service = service;
    this.parent = parent;
    this.name = name;
    this.className = parent.getMixinPackage() + name;
    this.plugin = plugin;
    this.phase = parent.getEnvironment().getPhase();
    this.strict = parent.getEnvironment().getOption(MixinEnvironment.Option.DEBUG_TARGETS);
    try {
      byte[] mixinBytes = loadMixinClass(this.className, runTransformers);
      this.pendingState = new State(mixinBytes);
      this.info = this.pendingState.getClassInfo();
      this.type = SubType.getTypeFor(this);
    } catch (InvalidMixinException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new InvalidMixinException(this, ex);
    } 
    if (!this.type.isLoadable())
      classLoaderUtil.registerInvalidClass(this.className); 
    try {
      this.priority = readPriority(this.pendingState.getClassNode());
      this.virtual = readPseudo(this.pendingState.getClassNode());
      this.targetClasses = readTargetClasses(this.pendingState.getClassNode(), suppressPlugin);
      this.targetClassNames = Collections.unmodifiableList(Lists.transform(this.targetClasses, Functions.toStringFunction()));
    } catch (InvalidMixinException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new InvalidMixinException(this, ex);
    } 
  }
  
  void validate() {
    if (this.pendingState == null)
      throw new IllegalStateException("No pending validation state for " + this); 
    try {
      this.pendingState.validate(this.type, this.targetClasses);
      this.state = this.pendingState;
    } finally {
      this.pendingState = null;
    } 
  }
  
  protected List<ClassInfo> readTargetClasses(MixinClassNode classNode, boolean suppressPlugin) {
    if (classNode == null)
      return Collections.emptyList(); 
    AnnotationNode mixin = Annotations.getInvisible(classNode, Mixin.class);
    if (mixin == null)
      throw new InvalidMixinException(this, String.format("The mixin '%s' is missing an @Mixin annotation", new Object[] { this.className })); 
    List<ClassInfo> targets = new ArrayList<ClassInfo>();
    List<Type> publicTargets = (List<Type>)Annotations.getValue(mixin, "value");
    List<String> privateTargets = (List<String>)Annotations.getValue(mixin, "targets");
    if (publicTargets != null)
      readTargets(targets, Lists.transform(publicTargets, new Function<Type, String>() {
              public String apply(Type input) {
                return input.getClassName();
              }
            }), suppressPlugin, false); 
    if (privateTargets != null)
      readTargets(targets, Lists.transform(privateTargets, new Function<String, String>() {
              public String apply(String input) {
                return MixinInfo.this.getParent().remapClassName(MixinInfo.this.getClassRef(), input);
              }
            }), suppressPlugin, true); 
    return targets;
  }
  
  private void readTargets(Collection<ClassInfo> outTargets, Collection<String> inTargets, boolean suppressPlugin, boolean checkPublic) {
    for (String targetRef : inTargets) {
      String targetName = targetRef.replace('/', '.');
      if (classLoaderUtil.isClassLoaded(targetName) && !isReloading()) {
        String message = String.format("Critical problem: %s target %s was already transformed.", new Object[] { this, targetName });
        if (this.parent.isRequired())
          throw new MixinTargetAlreadyLoadedException(this, message, targetName); 
        this.logger.error(message);
      } 
      if (shouldApplyMixin(suppressPlugin, targetName)) {
        ClassInfo targetInfo = getTarget(targetName, checkPublic);
        if (targetInfo != null && !outTargets.contains(targetInfo)) {
          outTargets.add(targetInfo);
          targetInfo.addMixin(this);
        } 
      } 
    } 
  }
  
  private boolean shouldApplyMixin(boolean suppressPlugin, String targetName) {
    Profiler.Section pluginTimer = this.profiler.begin("plugin");
    boolean result = (this.plugin == null || suppressPlugin || this.plugin.shouldApplyMixin(targetName, this.className));
    pluginTimer.end();
    return result;
  }
  
  private ClassInfo getTarget(String targetName, boolean checkPublic) throws InvalidMixinException {
    ClassInfo targetInfo = ClassInfo.forName(targetName);
    if (targetInfo == null) {
      if (isVirtual()) {
        this.logger.debug("Skipping virtual target {} for {}", new Object[] { targetName, this });
      } else {
        handleTargetError(String.format("@Mixin target %s was not found %s", new Object[] { targetName, this }));
      } 
      return null;
    } 
    this.type.validateTarget(targetName, targetInfo);
    if (checkPublic && targetInfo.isPublic() && !isVirtual())
      handleTargetError(String.format("@Mixin target %s is public in %s and should be specified in value", new Object[] { targetName, this })); 
    return targetInfo;
  }
  
  private void handleTargetError(String message) {
    if (this.strict) {
      this.logger.error(message);
      throw new InvalidMixinException(this, message);
    } 
    this.logger.warn(message);
  }
  
  protected int readPriority(ClassNode classNode) {
    if (classNode == null)
      return this.parent.getDefaultMixinPriority(); 
    AnnotationNode mixin = Annotations.getInvisible(classNode, Mixin.class);
    if (mixin == null)
      throw new InvalidMixinException(this, String.format("The mixin '%s' is missing an @Mixin annotation", new Object[] { this.className })); 
    Integer priority = (Integer)Annotations.getValue(mixin, "priority");
    return (priority == null) ? this.parent.getDefaultMixinPriority() : priority.intValue();
  }
  
  protected boolean readPseudo(ClassNode classNode) {
    return (Annotations.getInvisible(classNode, Pseudo.class) != null);
  }
  
  private boolean isReloading() {
    return this.pendingState instanceof Reloaded;
  }
  
  private State getState() {
    return (this.state != null) ? this.state : this.pendingState;
  }
  
  ClassInfo getClassInfo() {
    return this.info;
  }
  
  public IMixinConfig getConfig() {
    return this.parent;
  }
  
  MixinConfig getParent() {
    return this.parent;
  }
  
  public int getPriority() {
    return this.priority;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getClassName() {
    return this.className;
  }
  
  public String getClassRef() {
    return getClassInfo().getName();
  }
  
  public byte[] getClassBytes() {
    return getState().getClassBytes();
  }
  
  public boolean isDetachedSuper() {
    return getState().isDetachedSuper();
  }
  
  public boolean isUnique() {
    return getState().isUnique();
  }
  
  public boolean isVirtual() {
    return this.virtual;
  }
  
  public boolean isAccessor() {
    return this.type instanceof SubType.Accessor;
  }
  
  public boolean isLoadable() {
    return this.type.isLoadable();
  }
  
  public Level getLoggingLevel() {
    return this.parent.getLoggingLevel();
  }
  
  public MixinEnvironment.Phase getPhase() {
    return this.phase;
  }
  
  public MixinClassNode getClassNode(int flags) {
    return getState().createClassNode(flags);
  }
  
  public List<String> getTargetClasses() {
    return this.targetClassNames;
  }
  
  List<InterfaceInfo> getSoftImplements() {
    return Collections.unmodifiableList(getState().getSoftImplements());
  }
  
  Set<String> getSyntheticInnerClasses() {
    return Collections.unmodifiableSet(getState().getSyntheticInnerClasses());
  }
  
  Set<String> getInnerClasses() {
    return Collections.unmodifiableSet(getState().getInnerClasses());
  }
  
  List<ClassInfo> getTargets() {
    return Collections.unmodifiableList(this.targetClasses);
  }
  
  Set<String> getInterfaces() {
    return getState().getInterfaces();
  }
  
  MixinTargetContext createContextFor(TargetClassContext target) {
    MixinClassNode classNode = getClassNode(8);
    Profiler.Section preTimer = this.profiler.begin("pre");
    MixinTargetContext preProcessor = this.type.createPreProcessor(classNode).prepare().createContextFor(target);
    preTimer.end();
    return preProcessor;
  }
  
  private byte[] loadMixinClass(String mixinClassName, boolean runTransformers) throws ClassNotFoundException {
    byte[] mixinBytes = null;
    try {
      if (runTransformers) {
        String restrictions = this.service.getClassRestrictions(mixinClassName);
        if (restrictions.length() > 0)
          this.logger.error("Classloader restrictions [{}] encountered loading {}, name: {}", new Object[] { restrictions, this, mixinClassName }); 
      } 
      mixinBytes = this.service.getBytecodeProvider().getClassBytes(mixinClassName, runTransformers);
    } catch (ClassNotFoundException ex) {
      throw new ClassNotFoundException(String.format("The specified mixin '%s' was not found", new Object[] { mixinClassName }));
    } catch (IOException ex) {
      this.logger.warn("Failed to load mixin {}, the specified mixin will not be applied", new Object[] { mixinClassName });
      throw new InvalidMixinException(this, "An error was encountered whilst loading the mixin class", ex);
    } 
    return mixinBytes;
  }
  
  void reloadMixin(byte[] mixinBytes) {
    if (this.pendingState != null)
      throw new IllegalStateException("Cannot reload mixin while it is initialising"); 
    this.pendingState = new Reloaded(this.state, mixinBytes);
    validate();
  }
  
  public int compareTo(MixinInfo other) {
    if (other == null)
      return 0; 
    if (other.priority == this.priority)
      return this.order - other.order; 
    return this.priority - other.priority;
  }
  
  public void preApply(String transformedName, ClassNode targetClass) {
    if (this.plugin != null) {
      Profiler.Section pluginTimer = this.profiler.begin("plugin");
      this.plugin.preApply(transformedName, targetClass, this.className, this);
      pluginTimer.end();
    } 
  }
  
  public void postApply(String transformedName, ClassNode targetClass) {
    if (this.plugin != null) {
      Profiler.Section pluginTimer = this.profiler.begin("plugin");
      this.plugin.postApply(transformedName, targetClass, this.className, this);
      pluginTimer.end();
    } 
    this.parent.postApply(transformedName, targetClass);
  }
  
  public String toString() {
    return String.format("%s:%s", new Object[] { this.parent.getName(), this.name });
  }
}
