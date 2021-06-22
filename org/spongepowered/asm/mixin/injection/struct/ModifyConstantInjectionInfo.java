package org.spongepowered.asm.mixin.injection.struct;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.invoke.ModifyConstantInjector;
import org.spongepowered.asm.mixin.injection.points.BeforeConstant;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

public class ModifyConstantInjectionInfo extends InjectionInfo {
  private static final String CONSTANT_ANNOTATION_CLASS = Constant.class.getName().replace('.', '/');
  
  public ModifyConstantInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
    super(mixin, method, annotation, "constant");
  }
  
  protected List<AnnotationNode> readInjectionPoints(String type) {
    ImmutableList immutableList;
    List<AnnotationNode> ats = super.readInjectionPoints(type);
    if (ats.isEmpty()) {
      AnnotationNode c = new AnnotationNode(CONSTANT_ANNOTATION_CLASS);
      c.visit("log", Boolean.TRUE);
      immutableList = ImmutableList.of(c);
    } 
    return (List<AnnotationNode>)immutableList;
  }
  
  protected void parseInjectionPoints(List<AnnotationNode> ats) {
    Type returnType = Type.getReturnType(this.method.desc);
    for (AnnotationNode at : ats)
      this.injectionPoints.add(new BeforeConstant(getContext(), at, returnType.getDescriptor())); 
  }
  
  protected Injector parseInjector(AnnotationNode injectAnnotation) {
    return (Injector)new ModifyConstantInjector(this);
  }
  
  protected String getDescription() {
    return "Constant modifier method";
  }
  
  public String getSliceId(String id) {
    return Strings.nullToEmpty(id);
  }
}
