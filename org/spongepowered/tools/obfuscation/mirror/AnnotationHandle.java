package org.spongepowered.tools.obfuscation.mirror;

import com.google.common.collect.ImmutableList;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public final class AnnotationHandle {
  public static final AnnotationHandle MISSING = new AnnotationHandle(null);
  
  private final AnnotationMirror annotation;
  
  private AnnotationHandle(AnnotationMirror annotation) {
    this.annotation = annotation;
  }
  
  public AnnotationMirror asMirror() {
    return this.annotation;
  }
  
  public boolean exists() {
    return (this.annotation != null);
  }
  
  public String toString() {
    if (this.annotation == null)
      return "@{UnknownAnnotation}"; 
    return "@" + this.annotation.getAnnotationType().asElement().getSimpleName();
  }
  
  public <T> T getValue(String key, T defaultValue) {
    if (this.annotation == null)
      return defaultValue; 
    AnnotationValue value = getAnnotationValue(key);
    if (defaultValue instanceof Enum && value != null) {
      VariableElement varValue = (VariableElement)value.getValue();
      if (varValue == null)
        return defaultValue; 
      return (T)Enum.valueOf(defaultValue.getClass(), varValue.getSimpleName().toString());
    } 
    return (value != null) ? (T)value.getValue() : defaultValue;
  }
  
  public <T> T getValue() {
    return getValue("value", null);
  }
  
  public <T> T getValue(String key) {
    return getValue(key, null);
  }
  
  public boolean getBoolean(String key, boolean defaultValue) {
    return ((Boolean)getValue(key, Boolean.valueOf(defaultValue))).booleanValue();
  }
  
  public AnnotationHandle getAnnotation(String key) {
    Object value = getValue(key);
    if (value instanceof AnnotationMirror)
      return of((AnnotationMirror)value); 
    if (value instanceof AnnotationValue) {
      Object mirror = ((AnnotationValue)value).getValue();
      if (mirror instanceof AnnotationMirror)
        return of((AnnotationMirror)mirror); 
    } 
    return null;
  }
  
  public <T> List<T> getList() {
    return getList("value");
  }
  
  public <T> List<T> getList(String key) {
    List<AnnotationValue> list = getValue(key, Collections.emptyList());
    return unwrapAnnotationValueList(list);
  }
  
  public List<AnnotationHandle> getAnnotationList(String key) {
    Object val = getValue(key, null);
    if (val == null)
      return Collections.emptyList(); 
    if (val instanceof AnnotationMirror)
      return (List<AnnotationHandle>)ImmutableList.of(of((AnnotationMirror)val)); 
    List<AnnotationValue> list = (List<AnnotationValue>)val;
    List<AnnotationHandle> annotations = new ArrayList<AnnotationHandle>(list.size());
    for (AnnotationValue value : list)
      annotations.add(new AnnotationHandle((AnnotationMirror)value.getValue())); 
    return Collections.unmodifiableList(annotations);
  }
  
  protected AnnotationValue getAnnotationValue(String key) {
    for (ExecutableElement elem : this.annotation.getElementValues().keySet()) {
      if (elem.getSimpleName().contentEquals(key))
        return this.annotation.getElementValues().get(elem); 
    } 
    return null;
  }
  
  protected static <T> List<T> unwrapAnnotationValueList(List<AnnotationValue> list) {
    if (list == null)
      return Collections.emptyList(); 
    List<T> unfolded = new ArrayList<T>(list.size());
    for (AnnotationValue value : list)
      unfolded.add((T)value.getValue()); 
    return unfolded;
  }
  
  protected static AnnotationMirror getAnnotation(Element elem, Class<? extends Annotation> annotationClass) {
    if (elem == null)
      return null; 
    List<? extends AnnotationMirror> annotations = elem.getAnnotationMirrors();
    if (annotations == null)
      return null; 
    for (AnnotationMirror annotation : annotations) {
      Element element = annotation.getAnnotationType().asElement();
      if (!(element instanceof TypeElement))
        continue; 
      TypeElement annotationElement = (TypeElement)element;
      if (annotationElement.getQualifiedName().contentEquals(annotationClass.getName()))
        return annotation; 
    } 
    return null;
  }
  
  public static AnnotationHandle of(AnnotationMirror annotation) {
    return new AnnotationHandle(annotation);
  }
  
  public static AnnotationHandle of(Element elem, Class<? extends Annotation> annotationClass) {
    return new AnnotationHandle(getAnnotation(elem, annotationClass));
  }
}
