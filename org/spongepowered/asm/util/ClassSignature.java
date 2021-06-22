package org.spongepowered.asm.util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.spongepowered.asm.lib.signature.SignatureReader;
import org.spongepowered.asm.lib.signature.SignatureVisitor;
import org.spongepowered.asm.lib.signature.SignatureWriter;
import org.spongepowered.asm.lib.tree.ClassNode;

public class ClassSignature {
  protected static final String OBJECT = "java/lang/Object";
  
  static class Lazy extends ClassSignature {
    private final String sig;
    
    private ClassSignature generated;
    
    Lazy(String sig) {
      this.sig = sig;
    }
    
    public ClassSignature wake() {
      if (this.generated == null)
        this.generated = ClassSignature.of(this.sig); 
      return this.generated;
    }
  }
  
  static class TypeVar implements Comparable<TypeVar> {
    private final String originalName;
    
    private String currentName;
    
    TypeVar(String name) {
      this.currentName = this.originalName = name;
    }
    
    public int compareTo(TypeVar other) {
      return this.currentName.compareTo(other.currentName);
    }
    
    public String toString() {
      return this.currentName;
    }
    
    String getOriginalName() {
      return this.originalName;
    }
    
    void rename(String name) {
      this.currentName = name;
    }
    
    public boolean matches(String originalName) {
      return this.originalName.equals(originalName);
    }
    
    public boolean equals(Object obj) {
      return this.currentName.equals(obj);
    }
    
    public int hashCode() {
      return this.currentName.hashCode();
    }
  }
  
  static interface IToken {
    public static final String WILDCARDS = "+-";
    
    String asType();
    
    String asBound();
    
    ClassSignature.Token asToken();
    
    IToken setArray(boolean param1Boolean);
    
    IToken setWildcard(char param1Char);
  }
  
  static class Token implements IToken {
    static final String SYMBOLS = "+-*";
    
    private final boolean inner;
    
    private boolean array;
    
    private char symbol = Character.MIN_VALUE;
    
    private String type;
    
    private List<Token> classBound;
    
    private List<Token> ifaceBound;
    
    private List<ClassSignature.IToken> signature;
    
    private List<ClassSignature.IToken> suffix;
    
    private Token tail;
    
    Token() {
      this(false);
    }
    
    Token(String type) {
      this(type, false);
    }
    
    Token(char symbol) {
      this();
      this.symbol = symbol;
    }
    
    Token(boolean inner) {
      this(null, inner);
    }
    
    Token(String type, boolean inner) {
      this.inner = inner;
      this.type = type;
    }
    
    Token setSymbol(char symbol) {
      if (this.symbol == '\000' && "+-*".indexOf(symbol) > -1)
        this.symbol = symbol; 
      return this;
    }
    
    Token setType(String type) {
      if (this.type == null)
        this.type = type; 
      return this;
    }
    
    boolean hasClassBound() {
      return (this.classBound != null);
    }
    
    boolean hasInterfaceBound() {
      return (this.ifaceBound != null);
    }
    
    public ClassSignature.IToken setArray(boolean array) {
      this.array |= array;
      return this;
    }
    
    public ClassSignature.IToken setWildcard(char wildcard) {
      if ("+-".indexOf(wildcard) == -1)
        return this; 
      return setSymbol(wildcard);
    }
    
    private List<Token> getClassBound() {
      if (this.classBound == null)
        this.classBound = new ArrayList<Token>(); 
      return this.classBound;
    }
    
    private List<Token> getIfaceBound() {
      if (this.ifaceBound == null)
        this.ifaceBound = new ArrayList<Token>(); 
      return this.ifaceBound;
    }
    
    private List<ClassSignature.IToken> getSignature() {
      if (this.signature == null)
        this.signature = new ArrayList<ClassSignature.IToken>(); 
      return this.signature;
    }
    
    private List<ClassSignature.IToken> getSuffix() {
      if (this.suffix == null)
        this.suffix = new ArrayList<ClassSignature.IToken>(); 
      return this.suffix;
    }
    
    ClassSignature.IToken addTypeArgument(char symbol) {
      if (this.tail != null)
        return this.tail.addTypeArgument(symbol); 
      Token token = new Token(symbol);
      getSignature().add(token);
      return token;
    }
    
    ClassSignature.IToken addTypeArgument(String name) {
      if (this.tail != null)
        return this.tail.addTypeArgument(name); 
      Token token = new Token(name);
      getSignature().add(token);
      return token;
    }
    
    ClassSignature.IToken addTypeArgument(Token token) {
      if (this.tail != null)
        return this.tail.addTypeArgument(token); 
      getSignature().add(token);
      return token;
    }
    
    ClassSignature.IToken addTypeArgument(ClassSignature.TokenHandle token) {
      if (this.tail != null)
        return this.tail.addTypeArgument(token); 
      ClassSignature.TokenHandle handle = token.clone();
      getSignature().add(handle);
      return handle;
    }
    
    Token addBound(String bound, boolean classBound) {
      if (classBound)
        return addClassBound(bound); 
      return addInterfaceBound(bound);
    }
    
    Token addClassBound(String bound) {
      Token token = new Token(bound);
      getClassBound().add(token);
      return token;
    }
    
    Token addInterfaceBound(String bound) {
      Token token = new Token(bound);
      getIfaceBound().add(token);
      return token;
    }
    
    Token addInnerClass(String name) {
      this.tail = new Token(name, true);
      getSuffix().add(this.tail);
      return this.tail;
    }
    
    public String toString() {
      return asType();
    }
    
    public String asBound() {
      StringBuilder sb = new StringBuilder();
      if (this.type != null)
        sb.append(this.type); 
      if (this.classBound != null)
        for (Token token : this.classBound)
          sb.append(token.asType());  
      if (this.ifaceBound != null)
        for (Token token : this.ifaceBound)
          sb.append(':').append(token.asType());  
      return sb.toString();
    }
    
    public String asType() {
      return asType(false);
    }
    
    public String asType(boolean raw) {
      StringBuilder sb = new StringBuilder();
      if (this.array)
        sb.append('['); 
      if (this.symbol != '\000')
        sb.append(this.symbol); 
      if (this.type == null)
        return sb.toString(); 
      if (!this.inner)
        sb.append('L'); 
      sb.append(this.type);
      if (!raw) {
        if (this.signature != null) {
          sb.append('<');
          for (ClassSignature.IToken token : this.signature)
            sb.append(token.asType()); 
          sb.append('>');
        } 
        if (this.suffix != null)
          for (ClassSignature.IToken token : this.suffix)
            sb.append('.').append(token.asType());  
      } 
      if (!this.inner)
        sb.append(';'); 
      return sb.toString();
    }
    
    boolean isRaw() {
      return (this.signature == null);
    }
    
    String getClassType() {
      return (this.type != null) ? this.type : "java/lang/Object";
    }
    
    public Token asToken() {
      return this;
    }
  }
  
  class TokenHandle implements IToken {
    final ClassSignature.Token token;
    
    boolean array;
    
    char wildcard;
    
    TokenHandle() {
      this(new ClassSignature.Token());
    }
    
    TokenHandle(ClassSignature.Token token) {
      this.token = token;
    }
    
    public ClassSignature.IToken setArray(boolean array) {
      this.array |= array;
      return this;
    }
    
    public ClassSignature.IToken setWildcard(char wildcard) {
      if ("+-".indexOf(wildcard) > -1)
        this.wildcard = wildcard; 
      return this;
    }
    
    public String asBound() {
      return this.token.asBound();
    }
    
    public String asType() {
      StringBuilder sb = new StringBuilder();
      if (this.wildcard > '\000')
        sb.append(this.wildcard); 
      if (this.array)
        sb.append('['); 
      return sb.append(ClassSignature.this.getTypeVar(this)).toString();
    }
    
    public ClassSignature.Token asToken() {
      return this.token;
    }
    
    public String toString() {
      return this.token.toString();
    }
    
    public TokenHandle clone() {
      return new TokenHandle(this.token);
    }
  }
  
  class SignatureParser extends SignatureVisitor {
    private FormalParamElement param;
    
    abstract class SignatureElement extends SignatureVisitor {
      public SignatureElement() {
        super(327680);
      }
    }
    
    abstract class TokenElement extends SignatureElement {
      protected ClassSignature.Token token;
      
      private boolean array;
      
      public ClassSignature.Token getToken() {
        if (this.token == null)
          this.token = new ClassSignature.Token(); 
        return this.token;
      }
      
      protected void setArray() {
        this.array = true;
      }
      
      private boolean getArray() {
        boolean array = this.array;
        this.array = false;
        return array;
      }
      
      public void visitClassType(String name) {
        getToken().setType(name);
      }
      
      public SignatureVisitor visitClassBound() {
        getToken();
        return new ClassSignature.SignatureParser.BoundElement(this, true);
      }
      
      public SignatureVisitor visitInterfaceBound() {
        getToken();
        return new ClassSignature.SignatureParser.BoundElement(this, false);
      }
      
      public void visitInnerClassType(String name) {
        this.token.addInnerClass(name);
      }
      
      public SignatureVisitor visitArrayType() {
        setArray();
        return this;
      }
      
      public SignatureVisitor visitTypeArgument(char wildcard) {
        return new ClassSignature.SignatureParser.TypeArgElement(this, wildcard);
      }
      
      ClassSignature.Token addTypeArgument() {
        return this.token.addTypeArgument('*').asToken();
      }
      
      ClassSignature.IToken addTypeArgument(char symbol) {
        return this.token.addTypeArgument(symbol).setArray(getArray());
      }
      
      ClassSignature.IToken addTypeArgument(String name) {
        return this.token.addTypeArgument(name).setArray(getArray());
      }
      
      ClassSignature.IToken addTypeArgument(ClassSignature.Token token) {
        return this.token.addTypeArgument(token).setArray(getArray());
      }
      
      ClassSignature.IToken addTypeArgument(ClassSignature.TokenHandle token) {
        return this.token.addTypeArgument(token).setArray(getArray());
      }
    }
    
    class FormalParamElement extends TokenElement {
      private final ClassSignature.TokenHandle handle;
      
      FormalParamElement(String param) {
        this.handle = ClassSignature.this.getType(param);
        this.token = this.handle.asToken();
      }
    }
    
    class TypeArgElement extends TokenElement {
      private final ClassSignature.SignatureParser.TokenElement type;
      
      private final char wildcard;
      
      TypeArgElement(ClassSignature.SignatureParser.TokenElement type, char wildcard) {
        this.type = type;
        this.wildcard = wildcard;
      }
      
      public SignatureVisitor visitArrayType() {
        this.type.setArray();
        return this;
      }
      
      public void visitBaseType(char descriptor) {
        this.token = this.type.addTypeArgument(descriptor).asToken();
      }
      
      public void visitTypeVariable(String name) {
        ClassSignature.TokenHandle token = ClassSignature.this.getType(name);
        this.token = this.type.addTypeArgument(token).setWildcard(this.wildcard).asToken();
      }
      
      public void visitClassType(String name) {
        this.token = this.type.addTypeArgument(name).setWildcard(this.wildcard).asToken();
      }
      
      public void visitTypeArgument() {
        this.token.addTypeArgument('*');
      }
      
      public SignatureVisitor visitTypeArgument(char wildcard) {
        return new TypeArgElement(this, wildcard);
      }
      
      public void visitEnd() {}
    }
    
    class BoundElement extends TokenElement {
      private final ClassSignature.SignatureParser.TokenElement type;
      
      private final boolean classBound;
      
      BoundElement(ClassSignature.SignatureParser.TokenElement type, boolean classBound) {
        this.type = type;
        this.classBound = classBound;
      }
      
      public void visitClassType(String name) {
        this.token = this.type.token.addBound(name, this.classBound);
      }
      
      public void visitTypeArgument() {
        this.token.addTypeArgument('*');
      }
      
      public SignatureVisitor visitTypeArgument(char wildcard) {
        return new ClassSignature.SignatureParser.TypeArgElement(this, wildcard);
      }
    }
    
    class SuperClassElement extends TokenElement {
      public void visitEnd() {
        ClassSignature.this.setSuperClass(this.token);
      }
    }
    
    class InterfaceElement extends TokenElement {
      public void visitEnd() {
        ClassSignature.this.addInterface(this.token);
      }
    }
    
    SignatureParser() {
      super(327680);
    }
    
    public void visitFormalTypeParameter(String name) {
      this.param = new FormalParamElement(name);
    }
    
    public SignatureVisitor visitClassBound() {
      return this.param.visitClassBound();
    }
    
    public SignatureVisitor visitInterfaceBound() {
      return this.param.visitInterfaceBound();
    }
    
    public SignatureVisitor visitSuperclass() {
      return new SuperClassElement();
    }
    
    public SignatureVisitor visitInterface() {
      return new InterfaceElement();
    }
  }
  
  class SignatureRemapper extends SignatureWriter {
    private final Set<String> localTypeVars = new HashSet<String>();
    
    public void visitFormalTypeParameter(String name) {
      this.localTypeVars.add(name);
      super.visitFormalTypeParameter(name);
    }
    
    public void visitTypeVariable(String name) {
      if (!this.localTypeVars.contains(name)) {
        ClassSignature.TypeVar typeVar = ClassSignature.this.getTypeVar(name);
        if (typeVar != null) {
          super.visitTypeVariable(typeVar.toString());
          return;
        } 
      } 
      super.visitTypeVariable(name);
    }
  }
  
  private final Map<TypeVar, TokenHandle> types = new LinkedHashMap<TypeVar, TokenHandle>();
  
  private Token superClass = new Token("java/lang/Object");
  
  private final List<Token> interfaces = new ArrayList<Token>();
  
  private final Deque<String> rawInterfaces = new LinkedList<String>();
  
  private ClassSignature read(String signature) {
    if (signature != null)
      try {
        (new SignatureReader(signature)).accept(new SignatureParser());
      } catch (Exception ex) {
        ex.printStackTrace();
      }  
    return this;
  }
  
  protected TypeVar getTypeVar(String varName) {
    for (TypeVar typeVar : this.types.keySet()) {
      if (typeVar.matches(varName))
        return typeVar; 
    } 
    return null;
  }
  
  protected TokenHandle getType(String varName) {
    for (TypeVar typeVar : this.types.keySet()) {
      if (typeVar.matches(varName))
        return this.types.get(typeVar); 
    } 
    TokenHandle handle = new TokenHandle();
    this.types.put(new TypeVar(varName), handle);
    return handle;
  }
  
  protected String getTypeVar(TokenHandle handle) {
    for (Map.Entry<TypeVar, TokenHandle> type : this.types.entrySet()) {
      TypeVar typeVar = type.getKey();
      TokenHandle typeHandle = type.getValue();
      if (handle == typeHandle || handle.asToken() == typeHandle.asToken())
        return "T" + typeVar + ";"; 
    } 
    return handle.token.asType();
  }
  
  protected void addTypeVar(TypeVar typeVar, TokenHandle handle) throws IllegalArgumentException {
    if (this.types.containsKey(typeVar))
      throw new IllegalArgumentException("TypeVar " + typeVar + " is already present on " + this); 
    this.types.put(typeVar, handle);
  }
  
  protected void setSuperClass(Token superClass) {
    this.superClass = superClass;
  }
  
  public String getSuperClass() {
    return this.superClass.asType(true);
  }
  
  protected void addInterface(Token iface) {
    if (!iface.isRaw()) {
      String raw = iface.asType(true);
      for (ListIterator<Token> iter = this.interfaces.listIterator(); iter.hasNext(); ) {
        Token intrface = iter.next();
        if (intrface.isRaw() && intrface.asType(true).equals(raw)) {
          iter.set(iface);
          return;
        } 
      } 
    } 
    this.interfaces.add(iface);
  }
  
  public void addInterface(String iface) {
    this.rawInterfaces.add(iface);
  }
  
  protected void addRawInterface(String iface) {
    Token token = new Token(iface);
    String raw = token.asType(true);
    for (Token intrface : this.interfaces) {
      if (intrface.asType(true).equals(raw))
        return; 
    } 
    this.interfaces.add(token);
  }
  
  public void merge(ClassSignature other) {
    try {
      Set<String> typeVars = new HashSet<String>();
      for (TypeVar typeVar : this.types.keySet())
        typeVars.add(typeVar.toString()); 
      other.conform(typeVars);
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
      return;
    } 
    for (Map.Entry<TypeVar, TokenHandle> type : other.types.entrySet())
      addTypeVar(type.getKey(), type.getValue()); 
    for (Token iface : other.interfaces)
      addInterface(iface); 
  }
  
  private void conform(Set<String> typeVars) {
    for (TypeVar typeVar : this.types.keySet()) {
      String name = findUniqueName(typeVar.getOriginalName(), typeVars);
      typeVar.rename(name);
      typeVars.add(name);
    } 
  }
  
  private String findUniqueName(String typeVar, Set<String> typeVars) {
    if (!typeVars.contains(typeVar))
      return typeVar; 
    if (typeVar.length() == 1) {
      String str = findOffsetName(typeVar.charAt(0), typeVars);
      if (str != null)
        return str; 
    } 
    String name = findOffsetName('T', typeVars, "", typeVar);
    if (name != null)
      return name; 
    name = findOffsetName('T', typeVars, typeVar, "");
    if (name != null)
      return name; 
    name = findOffsetName('T', typeVars, "T", typeVar);
    if (name != null)
      return name; 
    name = findOffsetName('T', typeVars, "", typeVar + "Type");
    if (name != null)
      return name; 
    throw new IllegalStateException("Failed to conform type var: " + typeVar);
  }
  
  private String findOffsetName(char c, Set<String> typeVars) {
    return findOffsetName(c, typeVars, "", "");
  }
  
  private String findOffsetName(char c, Set<String> typeVars, String prefix, String suffix) {
    String name = String.format("%s%s%s", new Object[] { prefix, Character.valueOf(c), suffix });
    if (!typeVars.contains(name))
      return name; 
    if (c > '@' && c < '[') {
      int s;
      for (s = c - 64; s + 65 != c; s = ++s % 26) {
        name = String.format("%s%s%s", new Object[] { prefix, Character.valueOf((char)(s + 65)), suffix });
        if (!typeVars.contains(name))
          return name; 
      } 
    } 
    return null;
  }
  
  public SignatureVisitor getRemapper() {
    return (SignatureVisitor)new SignatureRemapper();
  }
  
  public String toString() {
    while (this.rawInterfaces.size() > 0)
      addRawInterface(this.rawInterfaces.remove()); 
    StringBuilder sb = new StringBuilder();
    if (this.types.size() > 0) {
      boolean valid = false;
      StringBuilder types = new StringBuilder();
      for (Map.Entry<TypeVar, TokenHandle> type : this.types.entrySet()) {
        String bound = ((TokenHandle)type.getValue()).asBound();
        if (!bound.isEmpty()) {
          types.append(type.getKey()).append(':').append(bound);
          valid = true;
        } 
      } 
      if (valid)
        sb.append('<').append(types).append('>'); 
    } 
    sb.append(this.superClass.asType());
    for (Token iface : this.interfaces)
      sb.append(iface.asType()); 
    return sb.toString();
  }
  
  public ClassSignature wake() {
    return this;
  }
  
  public static ClassSignature of(String signature) {
    return (new ClassSignature()).read(signature);
  }
  
  public static ClassSignature of(ClassNode classNode) {
    if (classNode.signature != null)
      return of(classNode.signature); 
    return generate(classNode);
  }
  
  public static ClassSignature ofLazy(ClassNode classNode) {
    if (classNode.signature != null)
      return new Lazy(classNode.signature); 
    return generate(classNode);
  }
  
  private static ClassSignature generate(ClassNode classNode) {
    ClassSignature generated = new ClassSignature();
    generated.setSuperClass(new Token((classNode.superName != null) ? classNode.superName : "java/lang/Object"));
    for (String iface : classNode.interfaces)
      generated.addInterface(new Token(iface)); 
    return generated;
  }
}
