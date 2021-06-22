package org.spongepowered.asm.mixin.refmap;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

public final class ReferenceMapper implements IReferenceMapper, Serializable {
  private static final long serialVersionUID = 2L;
  
  public static final String DEFAULT_RESOURCE = "mixin.refmap.json";
  
  public static final ReferenceMapper DEFAULT_MAPPER = new ReferenceMapper(true, "invalid");
  
  private final Map<String, Map<String, String>> mappings = Maps.newHashMap();
  
  private final Map<String, Map<String, Map<String, String>>> data = Maps.newHashMap();
  
  private final transient boolean readOnly;
  
  private transient String context = null;
  
  private transient String resource;
  
  public ReferenceMapper() {
    this(false, "mixin.refmap.json");
  }
  
  private ReferenceMapper(boolean readOnly, String resource) {
    this.readOnly = readOnly;
    this.resource = resource;
  }
  
  public boolean isDefault() {
    return this.readOnly;
  }
  
  private void setResourceName(String resource) {
    if (!this.readOnly)
      this.resource = (resource != null) ? resource : "<unknown resource>"; 
  }
  
  public String getResourceName() {
    return this.resource;
  }
  
  public String getStatus() {
    return isDefault() ? "No refMap loaded." : ("Using refmap " + getResourceName());
  }
  
  public String getContext() {
    return this.context;
  }
  
  public void setContext(String context) {
    this.context = context;
  }
  
  public String remap(String className, String reference) {
    return remapWithContext(this.context, className, reference);
  }
  
  public String remapWithContext(String context, String className, String reference) {
    Map<String, Map<String, String>> mappings = this.mappings;
    if (context != null) {
      mappings = this.data.get(context);
      if (mappings == null)
        mappings = this.mappings; 
    } 
    return remap(mappings, className, reference);
  }
  
  private String remap(Map<String, Map<String, String>> mappings, String className, String reference) {
    if (className == null)
      for (Map<String, String> mapping : mappings.values()) {
        if (mapping.containsKey(reference))
          return mapping.get(reference); 
      }  
    Map<String, String> classMappings = mappings.get(className);
    if (classMappings == null)
      return reference; 
    String remappedReference = classMappings.get(reference);
    return (remappedReference != null) ? remappedReference : reference;
  }
  
  public String addMapping(String context, String className, String reference, String newReference) {
    if (this.readOnly || reference == null || newReference == null || reference.equals(newReference))
      return null; 
    Map<String, Map<String, String>> mappings = this.mappings;
    if (context != null) {
      mappings = this.data.get(context);
      if (mappings == null) {
        mappings = Maps.newHashMap();
        this.data.put(context, mappings);
      } 
    } 
    Map<String, String> classMappings = mappings.get(className);
    if (classMappings == null) {
      classMappings = new HashMap<String, String>();
      mappings.put(className, classMappings);
    } 
    return classMappings.put(reference, newReference);
  }
  
  public void write(Appendable writer) {
    (new GsonBuilder()).setPrettyPrinting().create().toJson(this, writer);
  }
  
  public static ReferenceMapper read(String resourcePath) {
    Logger logger = LogManager.getLogger("mixin");
    Reader reader = null;
    try {
      IMixinService service = MixinService.getService();
      InputStream resource = service.getResourceAsStream(resourcePath);
      if (resource != null) {
        reader = new InputStreamReader(resource);
        ReferenceMapper mapper = readJson(reader);
        mapper.setResourceName(resourcePath);
        return mapper;
      } 
    } catch (JsonParseException ex) {
      logger.error("Invalid REFMAP JSON in " + resourcePath + ": " + ex.getClass().getName() + " " + ex.getMessage());
    } catch (Exception ex) {
      logger.error("Failed reading REFMAP JSON from " + resourcePath + ": " + ex.getClass().getName() + " " + ex.getMessage());
    } finally {
      IOUtils.closeQuietly(reader);
    } 
    return DEFAULT_MAPPER;
  }
  
  public static ReferenceMapper read(Reader reader, String name) {
    try {
      ReferenceMapper mapper = readJson(reader);
      mapper.setResourceName(name);
      return mapper;
    } catch (Exception ex) {
      return DEFAULT_MAPPER;
    } 
  }
  
  private static ReferenceMapper readJson(Reader reader) {
    return (ReferenceMapper)(new Gson()).fromJson(reader, ReferenceMapper.class);
  }
}
