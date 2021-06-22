package org.spongepowered.tools.obfuscation.mapping;

import com.google.common.base.Objects;
import java.util.LinkedHashSet;
import org.spongepowered.asm.obfuscation.mapping.IMapping;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.ObfuscationType;

public interface IMappingConsumer {
  void clear();
  
  void addFieldMapping(ObfuscationType paramObfuscationType, MappingField paramMappingField1, MappingField paramMappingField2);
  
  void addMethodMapping(ObfuscationType paramObfuscationType, MappingMethod paramMappingMethod1, MappingMethod paramMappingMethod2);
  
  MappingSet<MappingField> getFieldMappings(ObfuscationType paramObfuscationType);
  
  MappingSet<MappingMethod> getMethodMappings(ObfuscationType paramObfuscationType);
  
  public static class MappingSet<TMapping extends IMapping<TMapping>> extends LinkedHashSet<MappingSet.Pair<TMapping>> {
    private static final long serialVersionUID = 1L;
    
    public static class Pair<TMapping extends IMapping<TMapping>> {
      public final TMapping from;
      
      public final TMapping to;
      
      public Pair(TMapping from, TMapping to) {
        this.from = from;
        this.to = to;
      }
      
      public boolean equals(Object obj) {
        if (!(obj instanceof Pair))
          return false; 
        Pair<TMapping> other = (Pair<TMapping>)obj;
        return (Objects.equal(this.from, other.from) && Objects.equal(this.to, other.to));
      }
      
      public int hashCode() {
        return Objects.hashCode(new Object[] { this.from, this.to });
      }
      
      public String toString() {
        return String.format("%s -> %s", new Object[] { this.from, this.to });
      }
    }
  }
}
