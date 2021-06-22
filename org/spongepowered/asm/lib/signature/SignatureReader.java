package org.spongepowered.asm.lib.signature;

public class SignatureReader {
  private final String signature;
  
  public SignatureReader(String signature) {
    this.signature = signature;
  }
  
  public void accept(SignatureVisitor v) {
    int pos;
    String signature = this.signature;
    int len = signature.length();
    if (signature.charAt(0) == '<') {
      char c;
      pos = 2;
      do {
        int end = signature.indexOf(':', pos);
        v.visitFormalTypeParameter(signature.substring(pos - 1, end));
        pos = end + 1;
        c = signature.charAt(pos);
        if (c == 'L' || c == '[' || c == 'T')
          pos = parseType(signature, pos, v.visitClassBound()); 
        while ((c = signature.charAt(pos++)) == ':')
          pos = parseType(signature, pos, v.visitInterfaceBound()); 
      } while (c != '>');
    } else {
      pos = 0;
    } 
    if (signature.charAt(pos) == '(') {
      pos++;
      while (signature.charAt(pos) != ')')
        pos = parseType(signature, pos, v.visitParameterType()); 
      pos = parseType(signature, pos + 1, v.visitReturnType());
      while (pos < len)
        pos = parseType(signature, pos + 1, v.visitExceptionType()); 
    } else {
      pos = parseType(signature, pos, v.visitSuperclass());
      while (pos < len)
        pos = parseType(signature, pos, v.visitInterface()); 
    } 
  }
  
  public void acceptType(SignatureVisitor v) {
    parseType(this.signature, 0, v);
  }
  
  private static int parseType(String signature, int pos, SignatureVisitor v) {
    int end;
    char c;
    switch (c = signature.charAt(pos++)) {
      case 'B':
      case 'C':
      case 'D':
      case 'F':
      case 'I':
      case 'J':
      case 'S':
      case 'V':
      case 'Z':
        v.visitBaseType(c);
        return pos;
      case '[':
        return parseType(signature, pos, v.visitArrayType());
      case 'T':
        end = signature.indexOf(';', pos);
        v.visitTypeVariable(signature.substring(pos, end));
        return end + 1;
    } 
    int start = pos;
    boolean visited = false;
    boolean inner = false;
    label36: while (true) {
      String name;
      switch (c = signature.charAt(pos++)) {
        case '.':
        case ';':
          if (!visited) {
            String str = signature.substring(start, pos - 1);
            if (inner) {
              v.visitInnerClassType(str);
            } else {
              v.visitClassType(str);
            } 
          } 
          if (c == ';') {
            v.visitEnd();
            return pos;
          } 
          start = pos;
          visited = false;
          inner = true;
        case '<':
          name = signature.substring(start, pos - 1);
          if (inner) {
            v.visitInnerClassType(name);
          } else {
            v.visitClassType(name);
          } 
          visited = true;
          while (true) {
            switch (c = signature.charAt(pos)) {
              case '>':
                continue label36;
              case '*':
                pos++;
                v.visitTypeArgument();
                continue;
              case '+':
              case '-':
                pos = parseType(signature, pos + 1, v
                    .visitTypeArgument(c));
                continue;
            } 
            pos = parseType(signature, pos, v
                .visitTypeArgument('='));
          } 
          break;
      } 
    } 
  }
}
