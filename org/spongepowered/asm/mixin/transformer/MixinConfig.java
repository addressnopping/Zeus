package org.spongepowered.asm.mixin.transformer;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinInitialisationError;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.spongepowered.asm.mixin.refmap.ReferenceMapper;
import org.spongepowered.asm.mixin.refmap.RemappingReferenceMapper;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.VersionNumber;

final class MixinConfig implements Comparable<MixinConfig>, IMixinConfig {
  static class InjectorOptions {
    @SerializedName("defaultRequire")
    int defaultRequireValue = 0;
    
    @SerializedName("defaultGroup")
    String defaultGroup = "default";
    
    @SerializedName("injectionPoints")
    List<String> injectionPoints;
    
    @SerializedName("maxShiftBy")
    int maxShiftBy = 0;
  }
  
  static class OverwriteOptions {
    @SerializedName("conformVisibility")
    boolean conformAccessModifiers;
    
    @SerializedName("requireAnnotations")
    boolean requireOverwriteAnnotations;
  }
  
  private static int configOrder = 0;
  
  private static final Set<String> globalMixinList = new HashSet<String>();
  
  private final Logger logger = LogManager.getLogger("mixin");
  
  private final transient Map<String, List<MixinInfo>> mixinMapping = new HashMap<String, List<MixinInfo>>();
  
  private final transient Set<String> unhandledTargets = new HashSet<String>();
  
  private final transient List<MixinInfo> mixins = new ArrayList<MixinInfo>();
  
  private transient Config handle;
  
  @SerializedName("target")
  private String selector;
  
  @SerializedName("minVersion")
  private String version;
  
  @SerializedName("compatibilityLevel")
  private String compatibility;
  
  @SerializedName("required")
  private boolean required;
  
  @SerializedName("priority")
  private int priority = 1000;
  
  @SerializedName("mixinPriority")
  private int mixinPriority = 1000;
  
  @SerializedName("package")
  private String mixinPackage;
  
  @SerializedName("mixins")
  private List<String> mixinClasses;
  
  @SerializedName("client")
  private List<String> mixinClassesClient;
  
  @SerializedName("server")
  private List<String> mixinClassesServer;
  
  @SerializedName("setSourceFile")
  private boolean setSourceFile = false;
  
  @SerializedName("refmap")
  private String refMapperConfig;
  
  @SerializedName("verbose")
  private boolean verboseLogging;
  
  private final transient int order = configOrder++;
  
  private final transient List<IListener> listeners = new ArrayList<IListener>();
  
  private transient IMixinService service;
  
  private transient MixinEnvironment env;
  
  private transient String name;
  
  @SerializedName("plugin")
  private String pluginClassName;
  
  @SerializedName("injectors")
  private InjectorOptions injectorOptions = new InjectorOptions();
  
  @SerializedName("overwrites")
  private OverwriteOptions overwriteOptions = new OverwriteOptions();
  
  private transient IMixinConfigPlugin plugin;
  
  private transient IReferenceMapper refMapper;
  
  private transient boolean prepared = false;
  
  private transient boolean visited = false;
  
  private boolean onLoad(IMixinService service, String name, MixinEnvironment fallbackEnvironment) {
    this.service = service;
    this.name = name;
    this.env = parseSelector(this.selector, fallbackEnvironment);
    this.required &= !this.env.getOption(MixinEnvironment.Option.IGNORE_REQUIRED) ? 1 : 0;
    initCompatibilityLevel();
    initInjectionPoints();
    return checkVersion();
  }
  
  private void initCompatibilityLevel() {
    if (this.compatibility == null)
      return; 
    MixinEnvironment.CompatibilityLevel level = MixinEnvironment.CompatibilityLevel.valueOf(this.compatibility.trim().toUpperCase());
    MixinEnvironment.CompatibilityLevel current = MixinEnvironment.getCompatibilityLevel();
    if (level == current)
      return; 
    if (current.isAtLeast(level) && 
      !current.canSupport(level))
      throw new MixinInitialisationError("Mixin config " + this.name + " requires compatibility level " + level + " which is too old"); 
    if (!current.canElevateTo(level))
      throw new MixinInitialisationError("Mixin config " + this.name + " requires compatibility level " + level + " which is prohibited by " + current); 
    MixinEnvironment.setCompatibilityLevel(level);
  }
  
  private MixinEnvironment parseSelector(String target, MixinEnvironment fallbackEnvironment) {
    if (target != null) {
      String[] selectors = target.split("[&\\| ]");
      for (String sel : selectors) {
        sel = sel.trim();
        Pattern environmentSelector = Pattern.compile("^@env(?:ironment)?\\(([A-Z]+)\\)$");
        Matcher environmentSelectorMatcher = environmentSelector.matcher(sel);
        if (environmentSelectorMatcher.matches())
          return MixinEnvironment.getEnvironment(MixinEnvironment.Phase.forName(environmentSelectorMatcher.group(1))); 
      } 
      MixinEnvironment.Phase phase = MixinEnvironment.Phase.forName(target);
      if (phase != null)
        return MixinEnvironment.getEnvironment(phase); 
    } 
    return fallbackEnvironment;
  }
  
  private void initInjectionPoints() {
    if (this.injectorOptions.injectionPoints == null)
      return; 
    for (String injectionPoint : this.injectorOptions.injectionPoints) {
      try {
        Class<?> injectionPointClass = this.service.getClassProvider().findClass(injectionPoint, true);
        if (InjectionPoint.class.isAssignableFrom(injectionPointClass)) {
          InjectionPoint.register(injectionPointClass);
          continue;
        } 
        this.logger.error("Unable to register injection point {} for {}, class must extend InjectionPoint", new Object[] { injectionPointClass, this });
      } catch (Throwable th) {
        this.logger.catching(th);
      } 
    } 
  }
  
  private boolean checkVersion() throws MixinInitialisationError {
    if (this.version == null)
      this.logger.error("Mixin config {} does not specify \"minVersion\" property", new Object[] { this.name }); 
    VersionNumber minVersion = VersionNumber.parse(this.version);
    VersionNumber curVersion = VersionNumber.parse(this.env.getVersion());
    if (minVersion.compareTo(curVersion) > 0) {
      this.logger.warn("Mixin config {} requires mixin subsystem version {} but {} was found. The mixin config will not be applied.", new Object[] { this.name, minVersion, curVersion });
      if (this.required)
        throw new MixinInitialisationError("Required mixin config " + this.name + " requires mixin subsystem version " + minVersion); 
      return false;
    } 
    return true;
  }
  
  void addListener(IListener listener) {
    this.listeners.add(listener);
  }
  
  void onSelect() {
    if (this.pluginClassName != null)
      try {
        Class<?> pluginClass = this.service.getClassProvider().findClass(this.pluginClassName, true);
        this.plugin = (IMixinConfigPlugin)pluginClass.newInstance();
        if (this.plugin != null)
          this.plugin.onLoad(this.mixinPackage); 
      } catch (Throwable th) {
        th.printStackTrace();
        this.plugin = null;
      }  
    if (!this.mixinPackage.endsWith("."))
      this.mixinPackage += "."; 
    boolean suppressRefMapWarning = false;
    if (this.refMapperConfig == null) {
      if (this.plugin != null)
        this.refMapperConfig = this.plugin.getRefMapperConfig(); 
      if (this.refMapperConfig == null) {
        suppressRefMapWarning = true;
        this.refMapperConfig = "mixin.refmap.json";
      } 
    } 
    this.refMapper = (IReferenceMapper)ReferenceMapper.read(this.refMapperConfig);
    this.verboseLogging |= this.env.getOption(MixinEnvironment.Option.DEBUG_VERBOSE);
    if (!suppressRefMapWarning && this.refMapper.isDefault() && !this.env.getOption(MixinEnvironment.Option.DISABLE_REFMAP))
      this.logger.warn("Reference map '{}' for {} could not be read. If this is a development environment you can ignore this message", new Object[] { this.refMapperConfig, this }); 
    if (this.env.getOption(MixinEnvironment.Option.REFMAP_REMAP))
      this.refMapper = RemappingReferenceMapper.of(this.env, this.refMapper); 
  }
  
  void prepare() {
    if (this.prepared)
      return; 
    this.prepared = true;
    prepareMixins(this.mixinClasses, false);
    switch (this.env.getSide()) {
      case CLIENT:
        prepareMixins(this.mixinClassesClient, false);
        return;
      case SERVER:
        prepareMixins(this.mixinClassesServer, false);
        return;
    } 
    this.logger.warn("Mixin environment was unable to detect the current side, sided mixins will not be applied");
  }
  
  static interface IListener {
    void onPrepare(MixinInfo param1MixinInfo);
    
    void onInit(MixinInfo param1MixinInfo);
  }
  
  void postInitialise() {
    if (this.plugin != null) {
      List<String> pluginMixins = this.plugin.getMixins();
      prepareMixins(pluginMixins, true);
    } 
    for (Iterator<MixinInfo> iter = this.mixins.iterator(); iter.hasNext(); ) {
      MixinInfo mixin = iter.next();
      try {
        mixin.validate();
        for (IListener listener : this.listeners)
          listener.onInit(mixin); 
      } catch (InvalidMixinException ex) {
        this.logger.error(ex.getMixin() + ": " + ex.getMessage(), (Throwable)ex);
        removeMixin(mixin);
        iter.remove();
      } catch (Exception ex) {
        this.logger.error(ex.getMessage(), ex);
        removeMixin(mixin);
        iter.remove();
      } 
    } 
  }
  
  private void removeMixin(MixinInfo remove) {
    for (List<MixinInfo> mixinsFor : this.mixinMapping.values()) {
      for (Iterator<MixinInfo> iter = mixinsFor.iterator(); iter.hasNext();) {
        if (remove == iter.next())
          iter.remove(); 
      } 
    } 
  }
  
  private void prepareMixins(List<String> mixinClasses, boolean suppressPlugin) {
    if (mixinClasses == null)
      return; 
    for (String mixinClass : mixinClasses) {
      String fqMixinClass = this.mixinPackage + mixinClass;
      if (mixinClass == null || globalMixinList.contains(fqMixinClass))
        continue; 
      MixinInfo mixin = null;
      try {
        mixin = new MixinInfo(this.service, this, mixinClass, true, this.plugin, suppressPlugin);
        if (mixin.getTargetClasses().size() > 0) {
          globalMixinList.add(fqMixinClass);
          for (String targetClass : mixin.getTargetClasses()) {
            String targetClassName = targetClass.replace('/', '.');
            mixinsFor(targetClassName).add(mixin);
            this.unhandledTargets.add(targetClassName);
          } 
          for (IListener listener : this.listeners)
            listener.onPrepare(mixin); 
          this.mixins.add(mixin);
        } 
      } catch (InvalidMixinException ex) {
        if (this.required)
          throw ex; 
        this.logger.error(ex.getMessage(), (Throwable)ex);
      } catch (Exception ex) {
        if (this.required)
          throw new InvalidMixinException(mixin, "Error initialising mixin " + mixin + " - " + ex.getClass() + ": " + ex.getMessage(), ex); 
        this.logger.error(ex.getMessage(), ex);
      } 
    } 
  }
  
  void postApply(String transformedName, ClassNode targetClass) {
    this.unhandledTargets.remove(transformedName);
  }
  
  public Config getHandle() {
    if (this.handle == null)
      this.handle = new Config(this); 
    return this.handle;
  }
  
  public boolean isRequired() {
    return this.required;
  }
  
  public MixinEnvironment getEnvironment() {
    return this.env;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getMixinPackage() {
    return this.mixinPackage;
  }
  
  public int getPriority() {
    return this.priority;
  }
  
  public int getDefaultMixinPriority() {
    return this.mixinPriority;
  }
  
  public int getDefaultRequiredInjections() {
    return this.injectorOptions.defaultRequireValue;
  }
  
  public String getDefaultInjectorGroup() {
    String defaultGroup = this.injectorOptions.defaultGroup;
    return (defaultGroup != null && !defaultGroup.isEmpty()) ? defaultGroup : "default";
  }
  
  public boolean conformOverwriteVisibility() {
    return this.overwriteOptions.conformAccessModifiers;
  }
  
  public boolean requireOverwriteAnnotations() {
    return this.overwriteOptions.requireOverwriteAnnotations;
  }
  
  public int getMaxShiftByValue() {
    return Math.min(Math.max(this.injectorOptions.maxShiftBy, 0), 5);
  }
  
  public boolean select(MixinEnvironment environment) {
    this.visited = true;
    return (this.env == environment);
  }
  
  boolean isVisited() {
    return this.visited;
  }
  
  int getDeclaredMixinCount() {
    return getCollectionSize((Collection<?>[])new Collection[] { this.mixinClasses, this.mixinClassesClient, this.mixinClassesServer });
  }
  
  int getMixinCount() {
    return this.mixins.size();
  }
  
  public List<String> getClasses() {
    return Collections.unmodifiableList(this.mixinClasses);
  }
  
  public boolean shouldSetSourceFile() {
    return this.setSourceFile;
  }
  
  public IReferenceMapper getReferenceMapper() {
    if (this.env.getOption(MixinEnvironment.Option.DISABLE_REFMAP))
      return (IReferenceMapper)ReferenceMapper.DEFAULT_MAPPER; 
    this.refMapper.setContext(this.env.getRefmapObfuscationContext());
    return this.refMapper;
  }
  
  String remapClassName(String className, String reference) {
    return getReferenceMapper().remap(className, reference);
  }
  
  public IMixinConfigPlugin getPlugin() {
    return this.plugin;
  }
  
  public Set<String> getTargets() {
    return Collections.unmodifiableSet(this.mixinMapping.keySet());
  }
  
  public Set<String> getUnhandledTargets() {
    return Collections.unmodifiableSet(this.unhandledTargets);
  }
  
  public Level getLoggingLevel() {
    return this.verboseLogging ? Level.INFO : Level.DEBUG;
  }
  
  public boolean packageMatch(String className) {
    return className.startsWith(this.mixinPackage);
  }
  
  public boolean hasMixinsFor(String targetClass) {
    return this.mixinMapping.containsKey(targetClass);
  }
  
  public List<MixinInfo> getMixinsFor(String targetClass) {
    return mixinsFor(targetClass);
  }
  
  private List<MixinInfo> mixinsFor(String targetClass) {
    List<MixinInfo> mixins = this.mixinMapping.get(targetClass);
    if (mixins == null) {
      mixins = new ArrayList<MixinInfo>();
      this.mixinMapping.put(targetClass, mixins);
    } 
    return mixins;
  }
  
  public List<String> reloadMixin(String mixinClass, byte[] bytes) {
    for (Iterator<MixinInfo> iter = this.mixins.iterator(); iter.hasNext(); ) {
      MixinInfo mixin = iter.next();
      if (mixin.getClassName().equals(mixinClass)) {
        mixin.reloadMixin(bytes);
        return mixin.getTargetClasses();
      } 
    } 
    return Collections.emptyList();
  }
  
  public String toString() {
    return this.name;
  }
  
  public int compareTo(MixinConfig other) {
    if (other == null)
      return 0; 
    if (other.priority == this.priority)
      return this.order - other.order; 
    return this.priority - other.priority;
  }
  
  static Config create(String configFile, MixinEnvironment outer) {
    try {
      IMixinService service = MixinService.getService();
      MixinConfig config = (MixinConfig)(new Gson()).fromJson(new InputStreamReader(service.getResourceAsStream(configFile)), MixinConfig.class);
      if (config.onLoad(service, configFile, outer))
        return config.getHandle(); 
      return null;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new IllegalArgumentException(String.format("The specified resource '%s' was invalid or could not be read", new Object[] { configFile }), ex);
    } 
  }
  
  private static int getCollectionSize(Collection<?>... collections) {
    int total = 0;
    for (Collection<?> collection : collections) {
      if (collection != null)
        total += collection.size(); 
    } 
    return total;
  }
}
