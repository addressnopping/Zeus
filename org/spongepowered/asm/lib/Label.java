package org.spongepowered.asm.lib;

public class Label {
  static final int DEBUG = 1;
  
  static final int RESOLVED = 2;
  
  static final int RESIZED = 4;
  
  static final int PUSHED = 8;
  
  static final int TARGET = 16;
  
  static final int STORE = 32;
  
  static final int REACHABLE = 64;
  
  static final int JSR = 128;
  
  static final int RET = 256;
  
  static final int SUBROUTINE = 512;
  
  static final int VISITED = 1024;
  
  static final int VISITED2 = 2048;
  
  public Object info;
  
  int status;
  
  int line;
  
  int position;
  
  private int referenceCount;
  
  private int[] srcAndRefPositions;
  
  int inputStackTop;
  
  int outputStackMax;
  
  Frame frame;
  
  Label successor;
  
  Edge successors;
  
  Label next;
  
  public int getOffset() {
    if ((this.status & 0x2) == 0)
      throw new IllegalStateException("Label offset position has not been resolved yet"); 
    return this.position;
  }
  
  void put(MethodWriter owner, ByteVector out, int source, boolean wideOffset) {
    if ((this.status & 0x2) == 0) {
      if (wideOffset) {
        addReference(-1 - source, out.length);
        out.putInt(-1);
      } else {
        addReference(source, out.length);
        out.putShort(-1);
      } 
    } else if (wideOffset) {
      out.putInt(this.position - source);
    } else {
      out.putShort(this.position - source);
    } 
  }
  
  private void addReference(int sourcePosition, int referencePosition) {
    if (this.srcAndRefPositions == null)
      this.srcAndRefPositions = new int[6]; 
    if (this.referenceCount >= this.srcAndRefPositions.length) {
      int[] a = new int[this.srcAndRefPositions.length + 6];
      System.arraycopy(this.srcAndRefPositions, 0, a, 0, this.srcAndRefPositions.length);
      this.srcAndRefPositions = a;
    } 
    this.srcAndRefPositions[this.referenceCount++] = sourcePosition;
    this.srcAndRefPositions[this.referenceCount++] = referencePosition;
  }
  
  boolean resolve(MethodWriter owner, int position, byte[] data) {
    boolean needUpdate = false;
    this.status |= 0x2;
    this.position = position;
    int i = 0;
    while (i < this.referenceCount) {
      int source = this.srcAndRefPositions[i++];
      int reference = this.srcAndRefPositions[i++];
      if (source >= 0) {
        int j = position - source;
        if (j < -32768 || j > 32767) {
          int opcode = data[reference - 1] & 0xFF;
          if (opcode <= 168) {
            data[reference - 1] = (byte)(opcode + 49);
          } else {
            data[reference - 1] = (byte)(opcode + 20);
          } 
          needUpdate = true;
        } 
        data[reference++] = (byte)(j >>> 8);
        data[reference] = (byte)j;
        continue;
      } 
      int offset = position + source + 1;
      data[reference++] = (byte)(offset >>> 24);
      data[reference++] = (byte)(offset >>> 16);
      data[reference++] = (byte)(offset >>> 8);
      data[reference] = (byte)offset;
    } 
    return needUpdate;
  }
  
  Label getFirst() {
    return (this.frame == null) ? this : this.frame.owner;
  }
  
  boolean inSubroutine(long id) {
    if ((this.status & 0x400) != 0)
      return ((this.srcAndRefPositions[(int)(id >>> 32L)] & (int)id) != 0); 
    return false;
  }
  
  boolean inSameSubroutine(Label block) {
    if ((this.status & 0x400) == 0 || (block.status & 0x400) == 0)
      return false; 
    for (int i = 0; i < this.srcAndRefPositions.length; i++) {
      if ((this.srcAndRefPositions[i] & block.srcAndRefPositions[i]) != 0)
        return true; 
    } 
    return false;
  }
  
  void addToSubroutine(long id, int nbSubroutines) {
    if ((this.status & 0x400) == 0) {
      this.status |= 0x400;
      this.srcAndRefPositions = new int[nbSubroutines / 32 + 1];
    } 
    this.srcAndRefPositions[(int)(id >>> 32L)] = this.srcAndRefPositions[(int)(id >>> 32L)] | (int)id;
  }
  
  void visitSubroutine(Label JSR, long id, int nbSubroutines) {
    Label stack = this;
    while (stack != null) {
      Label l = stack;
      stack = l.next;
      l.next = null;
      if (JSR != null) {
        if ((l.status & 0x800) != 0)
          continue; 
        l.status |= 0x800;
        if ((l.status & 0x100) != 0 && 
          !l.inSameSubroutine(JSR)) {
          Edge edge = new Edge();
          edge.info = l.inputStackTop;
          edge.successor = JSR.successors.successor;
          edge.next = l.successors;
          l.successors = edge;
        } 
      } else {
        if (l.inSubroutine(id))
          continue; 
        l.addToSubroutine(id, nbSubroutines);
      } 
      Edge e = l.successors;
      while (e != null) {
        if ((l.status & 0x80) == 0 || e != l.successors.next)
          if (e.successor.next == null) {
            e.successor.next = stack;
            stack = e.successor;
          }  
        e = e.next;
      } 
    } 
  }
  
  public String toString() {
    return "L" + System.identityHashCode(this);
  }
}
