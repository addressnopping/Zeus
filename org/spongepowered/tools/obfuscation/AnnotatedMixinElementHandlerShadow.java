package org.spongepowered.tools.obfuscation;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import org.spongepowered.asm.obfuscation.mapping.IMapping;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.interfaces.IObfuscationDataProvider;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;

class AnnotatedMixinElementHandlerShadow extends AnnotatedMixinElementHandler {
  static abstract class AnnotatedElementShadow<E extends Element, M extends IMapping<M>> extends AnnotatedMixinElementHandler.AnnotatedElement<E> {
    private final boolean shouldRemap;
    
    private final AnnotatedMixinElementHandler.ShadowElementName name;
    
    private final IMapping.Type type;
    
    protected AnnotatedElementShadow(E element, AnnotationHandle annotation, boolean shouldRemap, IMapping.Type type) {
      super(element, annotation);
      this.shouldRemap = shouldRemap;
      this.name = new AnnotatedMixinElementHandler.ShadowElementName((Element)element, annotation);
      this.type = type;
    }
    
    public boolean shouldRemap() {
      return this.shouldRemap;
    }
    
    public AnnotatedMixinElementHandler.ShadowElementName getName() {
      return this.name;
    }
    
    public IMapping.Type getElementType() {
      return this.type;
    }
    
    public String toString() {
      return getElementType().name().toLowerCase();
    }
    
    public AnnotatedMixinElementHandler.ShadowElementName setObfuscatedName(IMapping<?> name) {
      return setObfuscatedName(name.getSimpleName());
    }
    
    public AnnotatedMixinElementHandler.ShadowElementName setObfuscatedName(String name) {
      return getName().setObfuscatedName(name);
    }
    
    public ObfuscationData<M> getObfuscationData(IObfuscationDataProvider provider, TypeHandle owner) {
      return provider.getObfEntry((IMapping)getMapping(owner, getName().toString(), getDesc()));
    }
    
    public abstract M getMapping(TypeHandle param1TypeHandle, String param1String1, String param1String2);
    
    public abstract void addMapping(ObfuscationType param1ObfuscationType, IMapping<?> param1IMapping);
  }
  
  class AnnotatedElementShadowField extends AnnotatedElementShadow<VariableElement, MappingField> {
    public AnnotatedElementShadowField(VariableElement element, AnnotationHandle annotation, boolean shouldRemap) {
      super(element, annotation, shouldRemap, IMapping.Type.FIELD);
    }
    
    public MappingField getMapping(TypeHandle owner, String name, String desc) {
      return new MappingField(owner.getName(), name, desc);
    }
    
    public void addMapping(ObfuscationType type, IMapping<?> remapped) {
      AnnotatedMixinElementHandlerShadow.this.addFieldMapping(type, setObfuscatedName(remapped), getDesc(), remapped.getDesc());
    }
  }
  
  class AnnotatedElementShadowMethod extends AnnotatedElementShadow<ExecutableElement, MappingMethod> {
    public AnnotatedElementShadowMethod(ExecutableElement element, AnnotationHandle annotation, boolean shouldRemap) {
      super(element, annotation, shouldRemap, IMapping.Type.METHOD);
    }
    
    public MappingMethod getMapping(TypeHandle owner, String name, String desc) {
      return owner.getMappingMethod(name, desc);
    }
    
    public void addMapping(ObfuscationType type, IMapping<?> remapped) {
      AnnotatedMixinElementHandlerShadow.this.addMethodMapping(type, setObfuscatedName(remapped), getDesc(), remapped.getDesc());
    }
  }
  
  AnnotatedMixinElementHandlerShadow(IMixinAnnotationProcessor ap, AnnotatedMixin mixin) {
    super(ap, mixin);
  }
  
  public void registerShadow(AnnotatedElementShadow<?, ?> elem) {
    validateTarget((Element)elem.getElement(), elem.getAnnotation(), elem.getName(), "@Shadow");
    if (!elem.shouldRemap())
      return; 
    for (TypeHandle target : this.mixin.getTargets())
      registerShadowForTarget(elem, target); 
  }
  
  private void registerShadowForTarget(AnnotatedElementShadow<?, ?> elem, TypeHandle target) {
    ObfuscationData<? extends IMapping<?>> obfData = (ObfuscationData)elem.getObfuscationData(this.obf.getDataProvider(), target);
    if (obfData.isEmpty()) {
      String info = this.mixin.isMultiTarget() ? (" in target " + target) : "";
      if (target.isSimulated()) {
        elem.printMessage((Messager)this.ap, Diagnostic.Kind.WARNING, "Unable to locate obfuscation mapping" + info + " for @Shadow " + elem);
      } else {
        elem.printMessage((Messager)this.ap, Diagnostic.Kind.WARNING, "Unable to locate obfuscation mapping" + info + " for @Shadow " + elem);
      } 
      return;
    } 
    for (ObfuscationType type : obfData) {
      try {
        elem.addMapping(type, obfData.get(type));
      } catch (MappingConflictException ex) {
        elem.printMessage((Messager)this.ap, Diagnostic.Kind.ERROR, "Mapping conflict for @Shadow " + elem + ": " + ex.getNew().getSimpleName() + " for target " + target + " conflicts with existing mapping " + ex
            .getOld().getSimpleName());
      } 
    } 
  }
}
