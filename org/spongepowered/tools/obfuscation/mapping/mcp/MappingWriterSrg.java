package org.spongepowered.tools.obfuscation.mapping.mcp;

import java.io.IOException;
import java.io.PrintWriter;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.ObfuscationType;
import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer;
import org.spongepowered.tools.obfuscation.mapping.common.MappingWriter;

public class MappingWriterSrg extends MappingWriter {
  public MappingWriterSrg(Messager messager, Filer filer) {
    super(messager, filer);
  }
  
  public void write(String output, ObfuscationType type, IMappingConsumer.MappingSet<MappingField> fields, IMappingConsumer.MappingSet<MappingMethod> methods) {
    if (output == null)
      return; 
    PrintWriter writer = null;
    try {
      writer = openFileWriter(output, type + " output SRGs");
      writeFieldMappings(writer, fields);
      writeMethodMappings(writer, methods);
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (writer != null)
        try {
          writer.close();
        } catch (Exception exception) {} 
    } 
  }
  
  protected void writeFieldMappings(PrintWriter writer, IMappingConsumer.MappingSet<MappingField> fields) {
    for (IMappingConsumer.MappingSet.Pair<MappingField> field : fields)
      writer.println(formatFieldMapping(field)); 
  }
  
  protected void writeMethodMappings(PrintWriter writer, IMappingConsumer.MappingSet<MappingMethod> methods) {
    for (IMappingConsumer.MappingSet.Pair<MappingMethod> method : methods)
      writer.println(formatMethodMapping(method)); 
  }
  
  protected String formatFieldMapping(IMappingConsumer.MappingSet.Pair<MappingField> mapping) {
    return String.format("FD: %s/%s %s/%s", new Object[] { ((MappingField)mapping.from).getOwner(), ((MappingField)mapping.from).getName(), ((MappingField)mapping.to).getOwner(), ((MappingField)mapping.to).getName() });
  }
  
  protected String formatMethodMapping(IMappingConsumer.MappingSet.Pair<MappingMethod> mapping) {
    return String.format("MD: %s %s %s %s", new Object[] { ((MappingMethod)mapping.from).getName(), ((MappingMethod)mapping.from).getDesc(), ((MappingMethod)mapping.to).getName(), ((MappingMethod)mapping.to).getDesc() });
  }
}
