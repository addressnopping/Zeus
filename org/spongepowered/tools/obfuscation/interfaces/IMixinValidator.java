package org.spongepowered.tools.obfuscation.interfaces;

import java.util.Collection;
import javax.lang.model.element.TypeElement;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;

public interface IMixinValidator {
  boolean validate(ValidationPass paramValidationPass, TypeElement paramTypeElement, AnnotationHandle paramAnnotationHandle, Collection<TypeHandle> paramCollection);
  
  public enum ValidationPass {
    EARLY, LATE, FINAL;
  }
}
