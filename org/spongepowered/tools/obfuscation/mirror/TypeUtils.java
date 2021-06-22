package org.spongepowered.tools.obfuscation.mirror;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.spongepowered.asm.util.SignaturePrinter;

public abstract class TypeUtils {
  private static final int MAX_GENERIC_RECURSION_DEPTH = 5;
  
  private static final String OBJECT_SIG = "java.lang.Object";
  
  private static final String OBJECT_REF = "java/lang/Object";
  
  public static PackageElement getPackage(TypeMirror type) {
    if (!(type instanceof DeclaredType))
      return null; 
    return getPackage((TypeElement)((DeclaredType)type).asElement());
  }
  
  public static PackageElement getPackage(TypeElement type) {
    Element parent = type.getEnclosingElement();
    while (parent != null && !(parent instanceof PackageElement))
      parent = parent.getEnclosingElement(); 
    return (PackageElement)parent;
  }
  
  public static String getElementType(Element element) {
    if (element instanceof TypeElement)
      return "TypeElement"; 
    if (element instanceof ExecutableElement)
      return "ExecutableElement"; 
    if (element instanceof VariableElement)
      return "VariableElement"; 
    if (element instanceof PackageElement)
      return "PackageElement"; 
    if (element instanceof javax.lang.model.element.TypeParameterElement)
      return "TypeParameterElement"; 
    return element.getClass().getSimpleName();
  }
  
  public static String stripGenerics(String type) {
    StringBuilder sb = new StringBuilder();
    for (int pos = 0, depth = 0; pos < type.length(); pos++) {
      char c = type.charAt(pos);
      if (c == '<')
        depth++; 
      if (depth == 0) {
        sb.append(c);
      } else if (c == '>') {
        depth--;
      } 
    } 
    return sb.toString();
  }
  
  public static String getName(VariableElement field) {
    return (field != null) ? field.getSimpleName().toString() : null;
  }
  
  public static String getName(ExecutableElement method) {
    return (method != null) ? method.getSimpleName().toString() : null;
  }
  
  public static String getJavaSignature(Element element) {
    if (element instanceof ExecutableElement) {
      ExecutableElement method = (ExecutableElement)element;
      StringBuilder desc = (new StringBuilder()).append("(");
      boolean extra = false;
      for (VariableElement arg : method.getParameters()) {
        if (extra)
          desc.append(','); 
        desc.append(getTypeName(arg.asType()));
        extra = true;
      } 
      desc.append(')').append(getTypeName(method.getReturnType()));
      return desc.toString();
    } 
    return getTypeName(element.asType());
  }
  
  public static String getJavaSignature(String descriptor) {
    return (new SignaturePrinter("", descriptor)).setFullyQualified(true).toDescriptor();
  }
  
  public static String getTypeName(TypeMirror type) {
    switch (type.getKind()) {
      case PUBLIC:
        return getTypeName(((ArrayType)type).getComponentType()) + "[]";
      case PROTECTED:
        return getTypeName((DeclaredType)type);
      case PRIVATE:
        return getTypeName(getUpperBound(type));
      case null:
        return "java.lang.Object";
    } 
    return type.toString();
  }
  
  public static String getTypeName(DeclaredType type) {
    if (type == null)
      return "java.lang.Object"; 
    return getInternalName((TypeElement)type.asElement()).replace('/', '.');
  }
  
  public static String getDescriptor(Element element) {
    if (element instanceof ExecutableElement)
      return getDescriptor((ExecutableElement)element); 
    if (element instanceof VariableElement)
      return getInternalName((VariableElement)element); 
    return getInternalName(element.asType());
  }
  
  public static String getDescriptor(ExecutableElement method) {
    if (method == null)
      return null; 
    StringBuilder signature = new StringBuilder();
    for (VariableElement var : method.getParameters())
      signature.append(getInternalName(var)); 
    String returnType = getInternalName(method.getReturnType());
    return String.format("(%s)%s", new Object[] { signature, returnType });
  }
  
  public static String getInternalName(VariableElement field) {
    if (field == null)
      return null; 
    return getInternalName(field.asType());
  }
  
  public static String getInternalName(TypeMirror type) {
    switch (type.getKind()) {
      case PUBLIC:
        return "[" + getInternalName(((ArrayType)type).getComponentType());
      case PROTECTED:
        return "L" + getInternalName((DeclaredType)type) + ";";
      case PRIVATE:
        return "L" + getInternalName(getUpperBound(type)) + ";";
      case null:
        return "Z";
      case null:
        return "B";
      case null:
        return "C";
      case null:
        return "D";
      case null:
        return "F";
      case null:
        return "I";
      case null:
        return "J";
      case null:
        return "S";
      case null:
        return "V";
      case null:
        return "Ljava/lang/Object;";
    } 
    throw new IllegalArgumentException("Unable to parse type symbol " + type + " with " + type.getKind() + " to equivalent bytecode type");
  }
  
  public static String getInternalName(DeclaredType type) {
    if (type == null)
      return "java/lang/Object"; 
    return getInternalName((TypeElement)type.asElement());
  }
  
  public static String getInternalName(TypeElement element) {
    if (element == null)
      return null; 
    StringBuilder reference = new StringBuilder();
    reference.append(element.getSimpleName());
    Element parent = element.getEnclosingElement();
    while (parent != null) {
      if (parent instanceof TypeElement) {
        reference.insert(0, "$").insert(0, parent.getSimpleName());
      } else if (parent instanceof PackageElement) {
        reference.insert(0, "/").insert(0, ((PackageElement)parent).getQualifiedName().toString().replace('.', '/'));
      } 
      parent = parent.getEnclosingElement();
    } 
    return reference.toString();
  }
  
  private static DeclaredType getUpperBound(TypeMirror type) {
    try {
      return getUpperBound0(type, 5);
    } catch (IllegalStateException ex) {
      throw new IllegalArgumentException("Type symbol \"" + type + "\" is too complex", ex);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Unable to compute upper bound of type symbol " + type, ex);
    } 
  }
  
  private static DeclaredType getUpperBound0(TypeMirror type, int depth) {
    if (depth == 0)
      throw new IllegalStateException("Generic symbol \"" + type + "\" is too complex, exceeded " + '\005' + " iterations attempting to determine upper bound"); 
    if (type instanceof DeclaredType)
      return (DeclaredType)type; 
    if (type instanceof TypeVariable)
      try {
        TypeMirror upper = ((TypeVariable)type).getUpperBound();
        return getUpperBound0(upper, --depth);
      } catch (IllegalStateException ex) {
        throw ex;
      } catch (IllegalArgumentException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new IllegalArgumentException("Unable to compute upper bound of type symbol " + type);
      }  
    return null;
  }
  
  public static boolean isAssignable(ProcessingEnvironment processingEnv, TypeMirror targetType, TypeMirror superClass) {
    boolean assignable = processingEnv.getTypeUtils().isAssignable(targetType, superClass);
    if (!assignable && targetType instanceof DeclaredType && superClass instanceof DeclaredType) {
      TypeMirror rawTargetType = toRawType(processingEnv, (DeclaredType)targetType);
      TypeMirror rawSuperType = toRawType(processingEnv, (DeclaredType)superClass);
      return processingEnv.getTypeUtils().isAssignable(rawTargetType, rawSuperType);
    } 
    return assignable;
  }
  
  private static TypeMirror toRawType(ProcessingEnvironment processingEnv, DeclaredType targetType) {
    return processingEnv.getElementUtils().getTypeElement(((TypeElement)targetType.asElement()).getQualifiedName()).asType();
  }
  
  public static Visibility getVisibility(Element element) {
    if (element == null)
      return null; 
    for (Modifier modifier : element.getModifiers()) {
      switch (modifier) {
        case PUBLIC:
          return Visibility.PUBLIC;
        case PROTECTED:
          return Visibility.PROTECTED;
        case PRIVATE:
          return Visibility.PRIVATE;
      } 
    } 
    return Visibility.PACKAGE;
  }
}
