package org.spongepowered.asm.mixin.struct;

import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError;
import org.spongepowered.asm.util.Bytecode;

public abstract class MemberRef {
  public static final class Method extends MemberRef {
    private static final int OPCODES = 191;
    
    public final MethodInsnNode insn;
    
    public Method(MethodInsnNode insn) {
      this.insn = insn;
    }
    
    public boolean isField() {
      return false;
    }
    
    public int getOpcode() {
      return this.insn.getOpcode();
    }
    
    public void setOpcode(int opcode) {
      if ((opcode & 0xBF) == 0)
        throw new IllegalArgumentException("Invalid opcode for method instruction: 0x" + Integer.toHexString(opcode)); 
      this.insn.setOpcode(opcode);
    }
    
    public String getOwner() {
      return this.insn.owner;
    }
    
    public void setOwner(String owner) {
      this.insn.owner = owner;
    }
    
    public String getName() {
      return this.insn.name;
    }
    
    public void setName(String name) {
      this.insn.name = name;
    }
    
    public String getDesc() {
      return this.insn.desc;
    }
    
    public void setDesc(String desc) {
      this.insn.desc = desc;
    }
  }
  
  public static final class Field extends MemberRef {
    private static final int OPCODES = 183;
    
    public final FieldInsnNode insn;
    
    public Field(FieldInsnNode insn) {
      this.insn = insn;
    }
    
    public boolean isField() {
      return true;
    }
    
    public int getOpcode() {
      return this.insn.getOpcode();
    }
    
    public void setOpcode(int opcode) {
      if ((opcode & 0xB7) == 0)
        throw new IllegalArgumentException("Invalid opcode for field instruction: 0x" + Integer.toHexString(opcode)); 
      this.insn.setOpcode(opcode);
    }
    
    public String getOwner() {
      return this.insn.owner;
    }
    
    public void setOwner(String owner) {
      this.insn.owner = owner;
    }
    
    public String getName() {
      return this.insn.name;
    }
    
    public void setName(String name) {
      this.insn.name = name;
    }
    
    public String getDesc() {
      return this.insn.desc;
    }
    
    public void setDesc(String desc) {
      this.insn.desc = desc;
    }
  }
  
  public static final class Handle extends MemberRef {
    private org.spongepowered.asm.lib.Handle handle;
    
    public Handle(org.spongepowered.asm.lib.Handle handle) {
      this.handle = handle;
    }
    
    public org.spongepowered.asm.lib.Handle getMethodHandle() {
      return this.handle;
    }
    
    public boolean isField() {
      switch (this.handle.getTag()) {
        case 5:
        case 6:
        case 7:
        case 8:
        case 9:
          return false;
        case 1:
        case 2:
        case 3:
        case 4:
          return true;
      } 
      throw new MixinTransformerError("Invalid tag " + this.handle.getTag() + " for method handle " + this.handle + ".");
    }
    
    public int getOpcode() {
      int opcode = MemberRef.opcodeFromTag(this.handle.getTag());
      if (opcode == 0)
        throw new MixinTransformerError("Invalid tag " + this.handle.getTag() + " for method handle " + this.handle + "."); 
      return opcode;
    }
    
    public void setOpcode(int opcode) {
      int tag = MemberRef.tagFromOpcode(opcode);
      if (tag == 0)
        throw new MixinTransformerError("Invalid opcode " + Bytecode.getOpcodeName(opcode) + " for method handle " + this.handle + "."); 
      boolean itf = (tag == 9);
      this.handle = new org.spongepowered.asm.lib.Handle(tag, this.handle.getOwner(), this.handle.getName(), this.handle.getDesc(), itf);
    }
    
    public String getOwner() {
      return this.handle.getOwner();
    }
    
    public void setOwner(String owner) {
      boolean itf = (this.handle.getTag() == 9);
      this.handle = new org.spongepowered.asm.lib.Handle(this.handle.getTag(), owner, this.handle.getName(), this.handle.getDesc(), itf);
    }
    
    public String getName() {
      return this.handle.getName();
    }
    
    public void setName(String name) {
      boolean itf = (this.handle.getTag() == 9);
      this.handle = new org.spongepowered.asm.lib.Handle(this.handle.getTag(), this.handle.getOwner(), name, this.handle.getDesc(), itf);
    }
    
    public String getDesc() {
      return this.handle.getDesc();
    }
    
    public void setDesc(String desc) {
      boolean itf = (this.handle.getTag() == 9);
      this.handle = new org.spongepowered.asm.lib.Handle(this.handle.getTag(), this.handle.getOwner(), this.handle.getName(), desc, itf);
    }
  }
  
  private static final int[] H_OPCODES = new int[] { 0, 180, 178, 181, 179, 182, 184, 183, 183, 185 };
  
  public abstract boolean isField();
  
  public abstract int getOpcode();
  
  public abstract void setOpcode(int paramInt);
  
  public abstract String getOwner();
  
  public abstract void setOwner(String paramString);
  
  public abstract String getName();
  
  public abstract void setName(String paramString);
  
  public abstract String getDesc();
  
  public abstract void setDesc(String paramString);
  
  public String toString() {
    String name = Bytecode.getOpcodeName(getOpcode());
    return String.format("%s for %s.%s%s%s", new Object[] { name, getOwner(), getName(), isField() ? ":" : "", getDesc() });
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof MemberRef))
      return false; 
    MemberRef other = (MemberRef)obj;
    return (getOpcode() == other.getOpcode() && 
      getOwner().equals(other.getOwner()) && 
      getName().equals(other.getName()) && 
      getDesc().equals(other.getDesc()));
  }
  
  public int hashCode() {
    return toString().hashCode();
  }
  
  static int opcodeFromTag(int tag) {
    return (tag >= 0 && tag < H_OPCODES.length) ? H_OPCODES[tag] : 0;
  }
  
  static int tagFromOpcode(int opcode) {
    for (int tag = 1; tag < H_OPCODES.length; tag++) {
      if (H_OPCODES[tag] == opcode)
        return tag; 
    } 
    return 0;
  }
}
