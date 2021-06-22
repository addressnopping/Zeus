package org.spongepowered.tools.obfuscation;

import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeUtils;

@SupportedAnnotationTypes({"org.spongepowered.asm.mixin.Mixin", "org.spongepowered.asm.mixin.Shadow", "org.spongepowered.asm.mixin.Overwrite", "org.spongepowered.asm.mixin.gen.Accessor", "org.spongepowered.asm.mixin.Implements"})
public class MixinObfuscationProcessorTargets extends MixinObfuscationProcessor {
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      postProcess(roundEnv);
      return true;
    } 
    processMixins(roundEnv);
    processShadows(roundEnv);
    processOverwrites(roundEnv);
    processAccessors(roundEnv);
    processInvokers(roundEnv);
    processImplements(roundEnv);
    postProcess(roundEnv);
    return true;
  }
  
  protected void postProcess(RoundEnvironment roundEnv) {
    super.postProcess(roundEnv);
    try {
      this.mixins.writeReferences();
      this.mixins.writeMappings();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  private void processShadows(RoundEnvironment roundEnv) {
    for (Element elem : roundEnv.getElementsAnnotatedWith((Class)Shadow.class)) {
      Element parent = elem.getEnclosingElement();
      if (!(parent instanceof TypeElement)) {
        this.mixins.printMessage(Diagnostic.Kind.ERROR, "Unexpected parent with type " + TypeUtils.getElementType(parent), elem);
        continue;
      } 
      AnnotationHandle shadow = AnnotationHandle.of(elem, Shadow.class);
      if (elem.getKind() == ElementKind.FIELD) {
        this.mixins.registerShadow((TypeElement)parent, (VariableElement)elem, shadow);
        continue;
      } 
      if (elem.getKind() == ElementKind.METHOD) {
        this.mixins.registerShadow((TypeElement)parent, (ExecutableElement)elem, shadow);
        continue;
      } 
      this.mixins.printMessage(Diagnostic.Kind.ERROR, "Element is not a method or field", elem);
    } 
  }
  
  private void processOverwrites(RoundEnvironment roundEnv) {
    for (Element elem : roundEnv.getElementsAnnotatedWith((Class)Overwrite.class)) {
      Element parent = elem.getEnclosingElement();
      if (!(parent instanceof TypeElement)) {
        this.mixins.printMessage(Diagnostic.Kind.ERROR, "Unexpected parent with type " + TypeUtils.getElementType(parent), elem);
        continue;
      } 
      if (elem.getKind() == ElementKind.METHOD) {
        this.mixins.registerOverwrite((TypeElement)parent, (ExecutableElement)elem);
        continue;
      } 
      this.mixins.printMessage(Diagnostic.Kind.ERROR, "Element is not a method", elem);
    } 
  }
  
  private void processAccessors(RoundEnvironment roundEnv) {
    for (Element elem : roundEnv.getElementsAnnotatedWith((Class)Accessor.class)) {
      Element parent = elem.getEnclosingElement();
      if (!(parent instanceof TypeElement)) {
        this.mixins.printMessage(Diagnostic.Kind.ERROR, "Unexpected parent with type " + TypeUtils.getElementType(parent), elem);
        continue;
      } 
      if (elem.getKind() == ElementKind.METHOD) {
        this.mixins.registerAccessor((TypeElement)parent, (ExecutableElement)elem);
        continue;
      } 
      this.mixins.printMessage(Diagnostic.Kind.ERROR, "Element is not a method", elem);
    } 
  }
  
  private void processInvokers(RoundEnvironment roundEnv) {
    for (Element elem : roundEnv.getElementsAnnotatedWith((Class)Invoker.class)) {
      Element parent = elem.getEnclosingElement();
      if (!(parent instanceof TypeElement)) {
        this.mixins.printMessage(Diagnostic.Kind.ERROR, "Unexpected parent with type " + TypeUtils.getElementType(parent), elem);
        continue;
      } 
      if (elem.getKind() == ElementKind.METHOD) {
        this.mixins.registerInvoker((TypeElement)parent, (ExecutableElement)elem);
        continue;
      } 
      this.mixins.printMessage(Diagnostic.Kind.ERROR, "Element is not a method", elem);
    } 
  }
  
  private void processImplements(RoundEnvironment roundEnv) {
    for (Element elem : roundEnv.getElementsAnnotatedWith((Class)Implements.class)) {
      if (elem.getKind() == ElementKind.CLASS || elem.getKind() == ElementKind.INTERFACE) {
        AnnotationHandle implementsAnnotation = AnnotationHandle.of(elem, Implements.class);
        this.mixins.registerSoftImplements((TypeElement)elem, implementsAnnotation);
        continue;
      } 
      this.mixins.printMessage(Diagnostic.Kind.ERROR, "Found an @Implements annotation on an element which is not a class or interface", elem);
    } 
  }
}
