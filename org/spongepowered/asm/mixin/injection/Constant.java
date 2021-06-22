package org.spongepowered.asm.mixin.injection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Constant {
  boolean nullValue() default false;
  
  int intValue() default 0;
  
  float floatValue() default 0.0F;
  
  long longValue() default 0L;
  
  double doubleValue() default 0.0D;
  
  String stringValue() default "";
  
  Class<?> classValue() default Object.class;
  
  int ordinal() default -1;
  
  String slice() default "";
  
  Condition[] expandZeroConditions() default {};
  
  boolean log() default false;
  
  public enum Condition {
    LESS_THAN_ZERO((String)new int[] { 155, 156 }),
    LESS_THAN_OR_EQUAL_TO_ZERO((String)new int[] { 158, 157 }),
    GREATER_THAN_OR_EQUAL_TO_ZERO((String)LESS_THAN_ZERO),
    GREATER_THAN_ZERO((String)LESS_THAN_OR_EQUAL_TO_ZERO);
    
    private final int[] opcodes;
    
    private final Condition equivalence;
    
    Condition(Condition equivalence, int... opcodes) {
      this.equivalence = (equivalence != null) ? equivalence : this;
      this.opcodes = opcodes;
    }
    
    public Condition getEquivalentCondition() {
      return this.equivalence;
    }
    
    public int[] getOpcodes() {
      return this.opcodes;
    }
  }
}
