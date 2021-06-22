package org.spongepowered.asm.mixin.injection.callback;

import org.spongepowered.asm.lib.Type;

public class CallbackInfoReturnable<R> extends CallbackInfo {
  private R returnValue;
  
  public CallbackInfoReturnable(String name, boolean cancellable) {
    super(name, cancellable);
    this.returnValue = null;
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, R returnValue) {
    super(name, cancellable);
    this.returnValue = returnValue;
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, byte returnValue) {
    super(name, cancellable);
    this.returnValue = (R)Byte.valueOf(returnValue);
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, char returnValue) {
    super(name, cancellable);
    this.returnValue = (R)Character.valueOf(returnValue);
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, double returnValue) {
    super(name, cancellable);
    this.returnValue = (R)Double.valueOf(returnValue);
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, float returnValue) {
    super(name, cancellable);
    this.returnValue = (R)Float.valueOf(returnValue);
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, int returnValue) {
    super(name, cancellable);
    this.returnValue = (R)Integer.valueOf(returnValue);
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, long returnValue) {
    super(name, cancellable);
    this.returnValue = (R)Long.valueOf(returnValue);
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, short returnValue) {
    super(name, cancellable);
    this.returnValue = (R)Short.valueOf(returnValue);
  }
  
  public CallbackInfoReturnable(String name, boolean cancellable, boolean returnValue) {
    super(name, cancellable);
    this.returnValue = (R)Boolean.valueOf(returnValue);
  }
  
  public void setReturnValue(R returnValue) throws CancellationException {
    cancel();
    this.returnValue = returnValue;
  }
  
  public R getReturnValue() {
    return this.returnValue;
  }
  
  public byte getReturnValueB() {
    return (this.returnValue == null) ? 0 : ((Byte)this.returnValue).byteValue();
  }
  
  public char getReturnValueC() {
    return (this.returnValue == null) ? Character.MIN_VALUE : ((Character)this.returnValue).charValue();
  }
  
  public double getReturnValueD() {
    return (this.returnValue == null) ? 0.0D : ((Double)this.returnValue).doubleValue();
  }
  
  public float getReturnValueF() {
    return (this.returnValue == null) ? 0.0F : ((Float)this.returnValue).floatValue();
  }
  
  public int getReturnValueI() {
    return (this.returnValue == null) ? 0 : ((Integer)this.returnValue).intValue();
  }
  
  public long getReturnValueJ() {
    return (this.returnValue == null) ? 0L : ((Long)this.returnValue).longValue();
  }
  
  public short getReturnValueS() {
    return (this.returnValue == null) ? 0 : ((Short)this.returnValue).shortValue();
  }
  
  public boolean getReturnValueZ() {
    return (this.returnValue == null) ? false : ((Boolean)this.returnValue).booleanValue();
  }
  
  static String getReturnAccessor(Type returnType) {
    if (returnType.getSort() == 10 || returnType.getSort() == 9)
      return "getReturnValue"; 
    return String.format("getReturnValue%s", new Object[] { returnType.getDescriptor() });
  }
  
  static String getReturnDescriptor(Type returnType) {
    if (returnType.getSort() == 10 || returnType.getSort() == 9)
      return String.format("()%s", new Object[] { "Ljava/lang/Object;" }); 
    return String.format("()%s", new Object[] { returnType.getDescriptor() });
  }
}
