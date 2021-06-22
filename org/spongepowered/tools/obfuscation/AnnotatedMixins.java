package org.spongepowered.tools.obfuscation;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.util.ITokenProvider;
import org.spongepowered.tools.obfuscation.interfaces.IJavadocProvider;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.interfaces.IMixinValidator;
import org.spongepowered.tools.obfuscation.interfaces.IObfuscationManager;
import org.spongepowered.tools.obfuscation.interfaces.ITypeHandleProvider;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeHandleSimulated;
import org.spongepowered.tools.obfuscation.mirror.TypeReference;
import org.spongepowered.tools.obfuscation.struct.InjectorRemap;
import org.spongepowered.tools.obfuscation.validation.ParentValidator;
import org.spongepowered.tools.obfuscation.validation.TargetValidator;

final class AnnotatedMixins implements IMixinAnnotationProcessor, ITokenProvider, ITypeHandleProvider, IJavadocProvider {
  private static final String MAPID_SYSTEM_PROPERTY = "mixin.target.mapid";
  
  private static Map<ProcessingEnvironment, AnnotatedMixins> instances = new HashMap<ProcessingEnvironment, AnnotatedMixins>();
  
  private final IMixinAnnotationProcessor.CompilerEnvironment env;
  
  private final ProcessingEnvironment processingEnv;
  
  private final Map<String, AnnotatedMixin> mixins = new HashMap<String, AnnotatedMixin>();
  
  private final List<AnnotatedMixin> mixinsForPass = new ArrayList<AnnotatedMixin>();
  
  private final IObfuscationManager obf;
  
  private final List<IMixinValidator> validators;
  
  private final Map<String, Integer> tokenCache = new HashMap<String, Integer>();
  
  private final TargetMap targets;
  
  private Properties properties;
  
  private AnnotatedMixins(ProcessingEnvironment processingEnv) {
    this.env = detectEnvironment(processingEnv);
    this.processingEnv = processingEnv;
    printMessage(Diagnostic.Kind.NOTE, "SpongePowered MIXIN Annotation Processor Version=0.7.11");
    this.targets = initTargetMap();
    this.obf = new ObfuscationManager(this);
    this.obf.init();
    this.validators = (List<IMixinValidator>)ImmutableList.of(new ParentValidator(this), new TargetValidator(this));
    initTokenCache(getOption("tokens"));
  }
  
  protected TargetMap initTargetMap() {
    TargetMap targets = TargetMap.create(System.getProperty("mixin.target.mapid"));
    System.setProperty("mixin.target.mapid", targets.getSessionId());
    String targetsFileName = getOption("dependencyTargetsFile");
    if (targetsFileName != null)
      try {
        targets.readImports(new File(targetsFileName));
      } catch (IOException ex) {
        printMessage(Diagnostic.Kind.WARNING, "Could not read from specified imports file: " + targetsFileName);
      }  
    return targets;
  }
  
  private void initTokenCache(String tokens) {
    if (tokens != null) {
      Pattern tokenPattern = Pattern.compile("^([A-Z0-9\\-_\\.]+)=([0-9]+)$");
      String[] tokenValues = tokens.replaceAll("\\s", "").toUpperCase().split("[;,]");
      for (String tokenValue : tokenValues) {
        Matcher tokenMatcher = tokenPattern.matcher(tokenValue);
        if (tokenMatcher.matches())
          this.tokenCache.put(tokenMatcher.group(1), Integer.valueOf(Integer.parseInt(tokenMatcher.group(2)))); 
      } 
    } 
  }
  
  public ITypeHandleProvider getTypeProvider() {
    return this;
  }
  
  public ITokenProvider getTokenProvider() {
    return this;
  }
  
  public IObfuscationManager getObfuscationManager() {
    return this.obf;
  }
  
  public IJavadocProvider getJavadocProvider() {
    return this;
  }
  
  public ProcessingEnvironment getProcessingEnvironment() {
    return this.processingEnv;
  }
  
  public IMixinAnnotationProcessor.CompilerEnvironment getCompilerEnvironment() {
    return this.env;
  }
  
  public Integer getToken(String token) {
    if (this.tokenCache.containsKey(token))
      return this.tokenCache.get(token); 
    String option = getOption(token);
    Integer value = null;
    try {
      value = Integer.valueOf(Integer.parseInt(option));
    } catch (Exception exception) {}
    this.tokenCache.put(token, value);
    return value;
  }
  
  public String getOption(String option) {
    if (option == null)
      return null; 
    String value = this.processingEnv.getOptions().get(option);
    if (value != null)
      return value; 
    return getProperties().getProperty(option);
  }
  
  public Properties getProperties() {
    if (this.properties == null) {
      this.properties = new Properties();
      try {
        Filer filer = this.processingEnv.getFiler();
        FileObject propertyFile = filer.getResource(StandardLocation.SOURCE_PATH, "", "mixin.properties");
        if (propertyFile != null) {
          InputStream inputStream = propertyFile.openInputStream();
          this.properties.load(inputStream);
          inputStream.close();
        } 
      } catch (Exception exception) {}
    } 
    return this.properties;
  }
  
  private IMixinAnnotationProcessor.CompilerEnvironment detectEnvironment(ProcessingEnvironment processingEnv) {
    if (processingEnv.getClass().getName().contains("jdt"))
      return IMixinAnnotationProcessor.CompilerEnvironment.JDT; 
    return IMixinAnnotationProcessor.CompilerEnvironment.JAVAC;
  }
  
  public void writeMappings() {
    this.obf.writeMappings();
  }
  
  public void writeReferences() {
    this.obf.writeReferences();
  }
  
  public void clear() {
    this.mixins.clear();
  }
  
  public void registerMixin(TypeElement mixinType) {
    String name = mixinType.getQualifiedName().toString();
    if (!this.mixins.containsKey(name)) {
      AnnotatedMixin mixin = new AnnotatedMixin(this, mixinType);
      this.targets.registerTargets(mixin);
      mixin.runValidators(IMixinValidator.ValidationPass.EARLY, this.validators);
      this.mixins.put(name, mixin);
      this.mixinsForPass.add(mixin);
    } 
  }
  
  public AnnotatedMixin getMixin(TypeElement mixinType) {
    return getMixin(mixinType.getQualifiedName().toString());
  }
  
  public AnnotatedMixin getMixin(String mixinType) {
    return this.mixins.get(mixinType);
  }
  
  public Collection<TypeMirror> getMixinsTargeting(TypeMirror targetType) {
    return getMixinsTargeting((TypeElement)((DeclaredType)targetType).asElement());
  }
  
  public Collection<TypeMirror> getMixinsTargeting(TypeElement targetType) {
    List<TypeMirror> minions = new ArrayList<TypeMirror>();
    for (TypeReference mixin : this.targets.getMixinsTargeting(targetType)) {
      TypeHandle handle = mixin.getHandle(this.processingEnv);
      if (handle != null)
        minions.add(handle.getType()); 
    } 
    return minions;
  }
  
  public void registerAccessor(TypeElement mixinType, ExecutableElement method) {
    AnnotatedMixin mixinClass = getMixin(mixinType);
    if (mixinClass == null) {
      printMessage(Diagnostic.Kind.ERROR, "Found @Accessor annotation on a non-mixin method", method);
      return;
    } 
    AnnotationHandle accessor = AnnotationHandle.of(method, Accessor.class);
    mixinClass.registerAccessor(method, accessor, shouldRemap(mixinClass, accessor));
  }
  
  public void registerInvoker(TypeElement mixinType, ExecutableElement method) {
    AnnotatedMixin mixinClass = getMixin(mixinType);
    if (mixinClass == null) {
      printMessage(Diagnostic.Kind.ERROR, "Found @Accessor annotation on a non-mixin method", method);
      return;
    } 
    AnnotationHandle invoker = AnnotationHandle.of(method, Invoker.class);
    mixinClass.registerInvoker(method, invoker, shouldRemap(mixinClass, invoker));
  }
  
  public void registerOverwrite(TypeElement mixinType, ExecutableElement method) {
    AnnotatedMixin mixinClass = getMixin(mixinType);
    if (mixinClass == null) {
      printMessage(Diagnostic.Kind.ERROR, "Found @Overwrite annotation on a non-mixin method", method);
      return;
    } 
    AnnotationHandle overwrite = AnnotationHandle.of(method, Overwrite.class);
    mixinClass.registerOverwrite(method, overwrite, shouldRemap(mixinClass, overwrite));
  }
  
  public void registerShadow(TypeElement mixinType, VariableElement field, AnnotationHandle shadow) {
    AnnotatedMixin mixinClass = getMixin(mixinType);
    if (mixinClass == null) {
      printMessage(Diagnostic.Kind.ERROR, "Found @Shadow annotation on a non-mixin field", field);
      return;
    } 
    mixinClass.registerShadow(field, shadow, shouldRemap(mixinClass, shadow));
  }
  
  public void registerShadow(TypeElement mixinType, ExecutableElement method, AnnotationHandle shadow) {
    AnnotatedMixin mixinClass = getMixin(mixinType);
    if (mixinClass == null) {
      printMessage(Diagnostic.Kind.ERROR, "Found @Shadow annotation on a non-mixin method", method);
      return;
    } 
    mixinClass.registerShadow(method, shadow, shouldRemap(mixinClass, shadow));
  }
  
  public void registerInjector(TypeElement mixinType, ExecutableElement method, AnnotationHandle inject) {
    AnnotatedMixin mixinClass = getMixin(mixinType);
    if (mixinClass == null) {
      printMessage(Diagnostic.Kind.ERROR, "Found " + inject + " annotation on a non-mixin method", method);
      return;
    } 
    InjectorRemap remap = new InjectorRemap(shouldRemap(mixinClass, inject));
    mixinClass.registerInjector(method, inject, remap);
    remap.dispatchPendingMessages((Messager)this);
  }
  
  public void registerSoftImplements(TypeElement mixin, AnnotationHandle implementsAnnotation) {
    AnnotatedMixin mixinClass = getMixin(mixin);
    if (mixinClass == null) {
      printMessage(Diagnostic.Kind.ERROR, "Found @Implements annotation on a non-mixin class");
      return;
    } 
    mixinClass.registerSoftImplements(implementsAnnotation);
  }
  
  public void onPassStarted() {
    this.mixinsForPass.clear();
  }
  
  public void onPassCompleted(RoundEnvironment roundEnv) {
    if (!"true".equalsIgnoreCase(getOption("disableTargetExport")))
      this.targets.write(true); 
    for (AnnotatedMixin mixin : roundEnv.processingOver() ? (Object<?>)this.mixins.values() : (Object<?>)this.mixinsForPass)
      mixin.runValidators(roundEnv.processingOver() ? IMixinValidator.ValidationPass.FINAL : IMixinValidator.ValidationPass.LATE, this.validators); 
  }
  
  private boolean shouldRemap(AnnotatedMixin mixinClass, AnnotationHandle annotation) {
    return annotation.getBoolean("remap", mixinClass.remap());
  }
  
  public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
    if (this.env == IMixinAnnotationProcessor.CompilerEnvironment.JAVAC || kind != Diagnostic.Kind.NOTE)
      this.processingEnv.getMessager().printMessage(kind, msg); 
  }
  
  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element element) {
    this.processingEnv.getMessager().printMessage(kind, msg, element);
  }
  
  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element element, AnnotationMirror annotation) {
    this.processingEnv.getMessager().printMessage(kind, msg, element, annotation);
  }
  
  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element element, AnnotationMirror annotation, AnnotationValue value) {
    this.processingEnv.getMessager().printMessage(kind, msg, element, annotation, value);
  }
  
  public TypeHandle getTypeHandle(String name) {
    name = name.replace('/', '.');
    Elements elements = this.processingEnv.getElementUtils();
    TypeElement element = elements.getTypeElement(name);
    if (element != null)
      try {
        return new TypeHandle(element);
      } catch (NullPointerException nullPointerException) {} 
    int lastDotPos = name.lastIndexOf('.');
    if (lastDotPos > -1) {
      String pkg = name.substring(0, lastDotPos);
      PackageElement packageElement = elements.getPackageElement(pkg);
      if (packageElement != null)
        return new TypeHandle(packageElement, name); 
    } 
    return null;
  }
  
  public TypeHandle getSimulatedHandle(String name, TypeMirror simulatedTarget) {
    name = name.replace('/', '.');
    int lastDotPos = name.lastIndexOf('.');
    if (lastDotPos > -1) {
      String pkg = name.substring(0, lastDotPos);
      PackageElement packageElement = this.processingEnv.getElementUtils().getPackageElement(pkg);
      if (packageElement != null)
        return (TypeHandle)new TypeHandleSimulated(packageElement, name, simulatedTarget); 
    } 
    return (TypeHandle)new TypeHandleSimulated(name, simulatedTarget);
  }
  
  public String getJavadoc(Element element) {
    Elements elements = this.processingEnv.getElementUtils();
    return elements.getDocComment(element);
  }
  
  public static AnnotatedMixins getMixinsForEnvironment(ProcessingEnvironment processingEnv) {
    AnnotatedMixins mixins = instances.get(processingEnv);
    if (mixins == null) {
      mixins = new AnnotatedMixins(processingEnv);
      instances.put(processingEnv, mixins);
    } 
    return mixins;
  }
}
