package org.spongepowered.asm.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.mixin.extensibility.IEnvironmentTokenProvider;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.mixin.transformer.MixinTransformer;
import org.spongepowered.asm.obfuscation.RemapperChain;
import org.spongepowered.asm.service.ILegacyClassTransformer;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.ITokenProvider;
import org.spongepowered.asm.util.JavaVersion;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.perf.Profiler;

public final class MixinEnvironment implements ITokenProvider {
  public static final class Phase {
    static final Phase NOT_INITIALISED = new Phase(-1, "NOT_INITIALISED");
    
    public static final Phase PREINIT = new Phase(0, "PREINIT");
    
    public static final Phase INIT = new Phase(1, "INIT");
    
    public static final Phase DEFAULT = new Phase(2, "DEFAULT");
    
    static final List<Phase> phases = (List<Phase>)ImmutableList.of(PREINIT, INIT, DEFAULT);
    
    final int ordinal;
    
    final String name;
    
    private MixinEnvironment environment;
    
    private Phase(int ordinal, String name) {
      this.ordinal = ordinal;
      this.name = name;
    }
    
    public String toString() {
      return this.name;
    }
    
    public static Phase forName(String name) {
      for (Phase phase : phases) {
        if (phase.name.equals(name))
          return phase; 
      } 
      return null;
    }
    
    MixinEnvironment getEnvironment() {
      if (this.ordinal < 0)
        throw new IllegalArgumentException("Cannot access the NOT_INITIALISED environment"); 
      if (this.environment == null)
        this.environment = new MixinEnvironment(this); 
      return this.environment;
    }
  }
  
  public enum Side {
    UNKNOWN {
      protected boolean detect() {
        return false;
      }
    },
    CLIENT {
      protected boolean detect() {
        String sideName = MixinService.getService().getSideName();
        return "CLIENT".equals(sideName);
      }
    },
    SERVER {
      protected boolean detect() {
        String sideName = MixinService.getService().getSideName();
        return ("SERVER".equals(sideName) || "DEDICATEDSERVER".equals(sideName));
      }
    };
    
    protected abstract boolean detect();
  }
  
  public enum Option {
    DEBUG_ALL("debug"),
    DEBUG_EXPORT((String)DEBUG_ALL, "export"),
    DEBUG_EXPORT_FILTER((String)DEBUG_EXPORT, "filter", false),
    DEBUG_EXPORT_DECOMPILE((String)DEBUG_EXPORT, Inherit.ALLOW_OVERRIDE, (Option)"decompile"),
    DEBUG_EXPORT_DECOMPILE_THREADED((String)DEBUG_EXPORT_DECOMPILE, Inherit.ALLOW_OVERRIDE, (Option)"async"),
    DEBUG_EXPORT_DECOMPILE_MERGESIGNATURES((String)DEBUG_EXPORT_DECOMPILE, Inherit.ALLOW_OVERRIDE, (Option)"mergeGenericSignatures"),
    DEBUG_VERIFY((String)DEBUG_ALL, "verify"),
    DEBUG_VERBOSE((String)DEBUG_ALL, "verbose"),
    DEBUG_INJECTORS((String)DEBUG_ALL, "countInjections"),
    DEBUG_STRICT((String)DEBUG_ALL, Inherit.INDEPENDENT, (Option)"strict"),
    DEBUG_UNIQUE((String)DEBUG_STRICT, "unique"),
    DEBUG_TARGETS((String)DEBUG_STRICT, "targets"),
    DEBUG_PROFILER((String)DEBUG_ALL, Inherit.ALLOW_OVERRIDE, (Option)"profiler"),
    DUMP_TARGET_ON_FAILURE("dumpTargetOnFailure"),
    CHECK_ALL("checks"),
    CHECK_IMPLEMENTS((String)CHECK_ALL, "interfaces"),
    CHECK_IMPLEMENTS_STRICT((String)CHECK_IMPLEMENTS, Inherit.ALLOW_OVERRIDE, (Option)"strict"),
    IGNORE_CONSTRAINTS("ignoreConstraints"),
    HOT_SWAP("hotSwap"),
    ENVIRONMENT((String)Inherit.ALWAYS_FALSE, "env"),
    OBFUSCATION_TYPE((String)ENVIRONMENT, Inherit.ALWAYS_FALSE, (Option)"obf"),
    DISABLE_REFMAP((String)ENVIRONMENT, Inherit.INDEPENDENT, (Option)"disableRefMap"),
    REFMAP_REMAP((String)ENVIRONMENT, Inherit.INDEPENDENT, (Option)"remapRefMap"),
    REFMAP_REMAP_RESOURCE((String)ENVIRONMENT, Inherit.INDEPENDENT, (Option)"refMapRemappingFile", (Inherit)""),
    REFMAP_REMAP_SOURCE_ENV((String)ENVIRONMENT, Inherit.INDEPENDENT, (Option)"refMapRemappingEnv", (Inherit)"searge"),
    REFMAP_REMAP_ALLOW_PERMISSIVE((String)ENVIRONMENT, Inherit.INDEPENDENT, (Option)"allowPermissiveMatch", true, "true"),
    IGNORE_REQUIRED((String)ENVIRONMENT, Inherit.INDEPENDENT, (Option)"ignoreRequired"),
    DEFAULT_COMPATIBILITY_LEVEL((String)ENVIRONMENT, Inherit.INDEPENDENT, (Option)"compatLevel"),
    SHIFT_BY_VIOLATION_BEHAVIOUR((String)ENVIRONMENT, Inherit.INDEPENDENT, (Option)"shiftByViolation", (Inherit)"warn"),
    INITIALISER_INJECTION_MODE("initialiserInjectionMode", "default");
    
    private static final String PREFIX = "mixin";
    
    final Option parent;
    
    final Inherit inheritance;
    
    final String property;
    
    final String defaultValue;
    
    final boolean isFlag;
    
    final int depth;
    
    private enum Inherit {
      INHERIT, ALLOW_OVERRIDE, INDEPENDENT, ALWAYS_FALSE;
    }
    
    Option(Option parent, Inherit inheritance, String property, boolean isFlag, String defaultStringValue) {
      this.parent = parent;
      this.inheritance = inheritance;
      this.property = ((parent != null) ? parent.property : "mixin") + "." + property;
      this.defaultValue = defaultStringValue;
      this.isFlag = isFlag;
      int depth = 0;
      for (; parent != null; depth++)
        parent = parent.parent; 
      this.depth = depth;
    }
    
    Option getParent() {
      return this.parent;
    }
    
    String getProperty() {
      return this.property;
    }
    
    public String toString() {
      return this.isFlag ? String.valueOf(getBooleanValue()) : getStringValue();
    }
    
    private boolean getLocalBooleanValue(boolean defaultValue) {
      return Boolean.parseBoolean(System.getProperty(this.property, Boolean.toString(defaultValue)));
    }
    
    private boolean getInheritedBooleanValue() {
      return (this.parent != null && this.parent.getBooleanValue());
    }
    
    final boolean getBooleanValue() {
      if (this.inheritance == Inherit.ALWAYS_FALSE)
        return false; 
      boolean local = getLocalBooleanValue(false);
      if (this.inheritance == Inherit.INDEPENDENT)
        return local; 
      boolean inherited = (local || getInheritedBooleanValue());
      return (this.inheritance == Inherit.INHERIT) ? inherited : getLocalBooleanValue(inherited);
    }
    
    final String getStringValue() {
      return (this.inheritance == Inherit.INDEPENDENT || this.parent == null || this.parent.getBooleanValue()) ? 
        System.getProperty(this.property, this.defaultValue) : this.defaultValue;
    }
    
    <E extends Enum<E>> E getEnumValue(E defaultValue) {
      String value = System.getProperty(this.property, defaultValue.name());
      try {
        return Enum.valueOf((Class)defaultValue.getClass(), value.toUpperCase());
      } catch (IllegalArgumentException ex) {
        return defaultValue;
      } 
    }
  }
  
  public enum CompatibilityLevel {
    JAVA_6(6, 50, false),
    JAVA_7(7, 51, false) {
      boolean isSupported() {
        return (JavaVersion.current() >= 1.7D);
      }
    },
    JAVA_8(8, 52, true) {
      boolean isSupported() {
        return (JavaVersion.current() >= 1.8D);
      }
    },
    JAVA_9(9, 53, true) {
      boolean isSupported() {
        return false;
      }
    };
    
    private static final int CLASS_V1_9 = 53;
    
    private final int ver;
    
    private final int classVersion;
    
    private final boolean supportsMethodsInInterfaces;
    
    private CompatibilityLevel maxCompatibleLevel;
    
    CompatibilityLevel(int ver, int classVersion, boolean resolveMethodsInInterfaces) {
      this.ver = ver;
      this.classVersion = classVersion;
      this.supportsMethodsInInterfaces = resolveMethodsInInterfaces;
    }
    
    private void setMaxCompatibleLevel(CompatibilityLevel maxCompatibleLevel) {
      this.maxCompatibleLevel = maxCompatibleLevel;
    }
    
    boolean isSupported() {
      return true;
    }
    
    public int classVersion() {
      return this.classVersion;
    }
    
    public boolean supportsMethodsInInterfaces() {
      return this.supportsMethodsInInterfaces;
    }
    
    public boolean isAtLeast(CompatibilityLevel level) {
      return (level == null || this.ver >= level.ver);
    }
    
    public boolean canElevateTo(CompatibilityLevel level) {
      if (level == null || this.maxCompatibleLevel == null)
        return true; 
      return (level.ver <= this.maxCompatibleLevel.ver);
    }
    
    public boolean canSupport(CompatibilityLevel level) {
      if (level == null)
        return true; 
      return level.canElevateTo(this);
    }
  }
  
  static class TokenProviderWrapper implements Comparable<TokenProviderWrapper> {
    private static int nextOrder = 0;
    
    private final int priority;
    
    private final int order;
    
    private final IEnvironmentTokenProvider provider;
    
    private final MixinEnvironment environment;
    
    public TokenProviderWrapper(IEnvironmentTokenProvider provider, MixinEnvironment environment) {
      this.provider = provider;
      this.environment = environment;
      this.order = nextOrder++;
      this.priority = provider.getPriority();
    }
    
    public int compareTo(TokenProviderWrapper other) {
      if (other == null)
        return 0; 
      if (other.priority == this.priority)
        return other.order - this.order; 
      return other.priority - this.priority;
    }
    
    public IEnvironmentTokenProvider getProvider() {
      return this.provider;
    }
    
    Integer getToken(String token) {
      return this.provider.getToken(token, this.environment);
    }
  }
  
  static class MixinLogWatcher {
    static MixinAppender appender = new MixinAppender();
    
    static Logger log;
    
    static Level oldLevel = null;
    
    static void begin() {
      Logger fmlLog = LogManager.getLogger("FML");
      if (!(fmlLog instanceof Logger))
        return; 
      log = (Logger)fmlLog;
      oldLevel = log.getLevel();
      appender.start();
      log.addAppender((Appender)appender);
      log.setLevel(Level.ALL);
    }
    
    static void end() {
      if (log != null)
        log.removeAppender((Appender)appender); 
    }
    
    static class MixinAppender extends AbstractAppender {
      MixinAppender() {
        super("MixinLogWatcherAppender", null, null);
      }
      
      public void append(LogEvent event) {
        if (event.getLevel() != Level.DEBUG || !"Validating minecraft".equals(event.getMessage().getFormattedMessage()))
          return; 
        MixinEnvironment.gotoPhase(MixinEnvironment.Phase.INIT);
        if (MixinEnvironment.MixinLogWatcher.log.getLevel() == Level.ALL)
          MixinEnvironment.MixinLogWatcher.log.setLevel(MixinEnvironment.MixinLogWatcher.oldLevel); 
      }
    }
  }
  
  private static final Set<String> excludeTransformers = Sets.newHashSet((Object[])new String[] { "net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer", "cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer", "net.minecraftforge.fml.common.asm.transformers.TerminalTransformer", "cpw.mods.fml.common.asm.transformers.TerminalTransformer" });
  
  private static MixinEnvironment currentEnvironment;
  
  private static Phase currentPhase = Phase.NOT_INITIALISED;
  
  private static CompatibilityLevel compatibility = Option.DEFAULT_COMPATIBILITY_LEVEL.<CompatibilityLevel>getEnumValue(CompatibilityLevel.JAVA_6);
  
  private static boolean showHeader = true;
  
  private static final Logger logger = LogManager.getLogger("mixin");
  
  private static final Profiler profiler = new Profiler();
  
  private final IMixinService service;
  
  private final Phase phase;
  
  private final String configsKey;
  
  private final boolean[] options;
  
  private final Set<String> tokenProviderClasses = new HashSet<String>();
  
  private final List<TokenProviderWrapper> tokenProviders = new ArrayList<TokenProviderWrapper>();
  
  private final Map<String, Integer> internalTokens = new HashMap<String, Integer>();
  
  private final RemapperChain remappers = new RemapperChain();
  
  private Side side;
  
  private List<ILegacyClassTransformer> transformers;
  
  private String obfuscationContext = null;
  
  MixinEnvironment(Phase phase) {
    this.service = MixinService.getService();
    this.phase = phase;
    this.configsKey = "mixin.configs." + this.phase.name.toLowerCase();
    Object version = getVersion();
    if (version == null || !"0.7.11".equals(version))
      throw new MixinException("Environment conflict, mismatched versions or you didn't call MixinBootstrap.init()"); 
    this.service.checkEnv(this);
    this.options = new boolean[(Option.values()).length];
    for (Option option : Option.values())
      this.options[option.ordinal()] = option.getBooleanValue(); 
    if (showHeader) {
      showHeader = false;
      printHeader(version);
    } 
  }
  
  private void printHeader(Object version) {
    String codeSource = getCodeSource();
    String serviceName = this.service.getName();
    Side side = getSide();
    logger.info("SpongePowered MIXIN Subsystem Version={} Source={} Service={} Env={}", new Object[] { version, codeSource, serviceName, side });
    boolean verbose = getOption(Option.DEBUG_VERBOSE);
    if (verbose || getOption(Option.DEBUG_EXPORT) || getOption(Option.DEBUG_PROFILER)) {
      PrettyPrinter printer = new PrettyPrinter(32);
      printer.add("SpongePowered MIXIN%s", new Object[] { verbose ? " (Verbose debugging enabled)" : "" }).centre().hr();
      printer.kv("Code source", codeSource);
      printer.kv("Internal Version", version);
      printer.kv("Java 8 Supported", Boolean.valueOf(CompatibilityLevel.JAVA_8.isSupported())).hr();
      printer.kv("Service Name", serviceName);
      printer.kv("Service Class", this.service.getClass().getName()).hr();
      for (Option option : Option.values()) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < option.depth; i++)
          indent.append("- "); 
        printer.kv(option.property, "%s<%s>", new Object[] { indent, option });
      } 
      printer.hr().kv("Detected Side", side);
      printer.print(System.err);
    } 
  }
  
  private String getCodeSource() {
    try {
      return getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    } catch (Throwable th) {
      return "Unknown";
    } 
  }
  
  public Phase getPhase() {
    return this.phase;
  }
  
  @Deprecated
  public List<String> getMixinConfigs() {
    List<String> mixinConfigs = (List<String>)GlobalProperties.get(this.configsKey);
    if (mixinConfigs == null) {
      mixinConfigs = new ArrayList<String>();
      GlobalProperties.put(this.configsKey, mixinConfigs);
    } 
    return mixinConfigs;
  }
  
  @Deprecated
  public MixinEnvironment addConfiguration(String config) {
    logger.warn("MixinEnvironment::addConfiguration is deprecated and will be removed. Use Mixins::addConfiguration instead!");
    Mixins.addConfiguration(config, this);
    return this;
  }
  
  void registerConfig(String config) {
    List<String> configs = getMixinConfigs();
    if (!configs.contains(config))
      configs.add(config); 
  }
  
  @Deprecated
  public MixinEnvironment registerErrorHandlerClass(String handlerName) {
    Mixins.registerErrorHandlerClass(handlerName);
    return this;
  }
  
  public MixinEnvironment registerTokenProviderClass(String providerName) {
    if (!this.tokenProviderClasses.contains(providerName))
      try {
        Class<? extends IEnvironmentTokenProvider> providerClass = this.service.getClassProvider().findClass(providerName, true);
        IEnvironmentTokenProvider provider = providerClass.newInstance();
        registerTokenProvider(provider);
      } catch (Throwable th) {
        logger.error("Error instantiating " + providerName, th);
      }  
    return this;
  }
  
  public MixinEnvironment registerTokenProvider(IEnvironmentTokenProvider provider) {
    if (provider != null && !this.tokenProviderClasses.contains(provider.getClass().getName())) {
      String providerName = provider.getClass().getName();
      TokenProviderWrapper wrapper = new TokenProviderWrapper(provider, this);
      logger.info("Adding new token provider {} to {}", new Object[] { providerName, this });
      this.tokenProviders.add(wrapper);
      this.tokenProviderClasses.add(providerName);
      Collections.sort(this.tokenProviders);
    } 
    return this;
  }
  
  public Integer getToken(String token) {
    token = token.toUpperCase();
    for (TokenProviderWrapper provider : this.tokenProviders) {
      Integer value = provider.getToken(token);
      if (value != null)
        return value; 
    } 
    return this.internalTokens.get(token);
  }
  
  @Deprecated
  public Set<String> getErrorHandlerClasses() {
    return Mixins.getErrorHandlerClasses();
  }
  
  public Object getActiveTransformer() {
    return GlobalProperties.get("mixin.transformer");
  }
  
  public void setActiveTransformer(ITransformer transformer) {
    if (transformer != null)
      GlobalProperties.put("mixin.transformer", transformer); 
  }
  
  public MixinEnvironment setSide(Side side) {
    if (side != null && getSide() == Side.UNKNOWN && side != Side.UNKNOWN)
      this.side = side; 
    return this;
  }
  
  public Side getSide() {
    if (this.side == null)
      for (Side side : Side.values()) {
        if (side.detect()) {
          this.side = side;
          break;
        } 
      }  
    return (this.side != null) ? this.side : Side.UNKNOWN;
  }
  
  public String getVersion() {
    return (String)GlobalProperties.get("mixin.initialised");
  }
  
  public boolean getOption(Option option) {
    return this.options[option.ordinal()];
  }
  
  public void setOption(Option option, boolean value) {
    this.options[option.ordinal()] = value;
  }
  
  public String getOptionValue(Option option) {
    return option.getStringValue();
  }
  
  public <E extends Enum<E>> E getOption(Option option, E defaultValue) {
    return option.getEnumValue(defaultValue);
  }
  
  public void setObfuscationContext(String context) {
    this.obfuscationContext = context;
  }
  
  public String getObfuscationContext() {
    return this.obfuscationContext;
  }
  
  public String getRefmapObfuscationContext() {
    String overrideObfuscationType = Option.OBFUSCATION_TYPE.getStringValue();
    if (overrideObfuscationType != null)
      return overrideObfuscationType; 
    return this.obfuscationContext;
  }
  
  public RemapperChain getRemappers() {
    return this.remappers;
  }
  
  public void audit() {
    Object activeTransformer = getActiveTransformer();
    if (activeTransformer instanceof MixinTransformer) {
      MixinTransformer transformer = (MixinTransformer)activeTransformer;
      transformer.audit(this);
    } 
  }
  
  public List<ILegacyClassTransformer> getTransformers() {
    if (this.transformers == null)
      buildTransformerDelegationList(); 
    return Collections.unmodifiableList(this.transformers);
  }
  
  public void addTransformerExclusion(String name) {
    excludeTransformers.add(name);
    this.transformers = null;
  }
  
  private void buildTransformerDelegationList() {
    logger.debug("Rebuilding transformer delegation list:");
    this.transformers = new ArrayList<ILegacyClassTransformer>();
    for (ITransformer transformer : this.service.getTransformers()) {
      if (!(transformer instanceof ILegacyClassTransformer))
        continue; 
      ILegacyClassTransformer legacyTransformer = (ILegacyClassTransformer)transformer;
      String transformerName = legacyTransformer.getName();
      boolean include = true;
      for (String excludeClass : excludeTransformers) {
        if (transformerName.contains(excludeClass)) {
          include = false;
          break;
        } 
      } 
      if (include && !legacyTransformer.isDelegationExcluded()) {
        logger.debug("  Adding:    {}", new Object[] { transformerName });
        this.transformers.add(legacyTransformer);
        continue;
      } 
      logger.debug("  Excluding: {}", new Object[] { transformerName });
    } 
    logger.debug("Transformer delegation list created with {} entries", new Object[] { Integer.valueOf(this.transformers.size()) });
  }
  
  public String toString() {
    return String.format("%s[%s]", new Object[] { getClass().getSimpleName(), this.phase });
  }
  
  private static Phase getCurrentPhase() {
    if (currentPhase == Phase.NOT_INITIALISED)
      init(Phase.PREINIT); 
    return currentPhase;
  }
  
  public static void init(Phase phase) {
    if (currentPhase == Phase.NOT_INITIALISED) {
      currentPhase = phase;
      MixinEnvironment env = getEnvironment(phase);
      getProfiler().setActive(env.getOption(Option.DEBUG_PROFILER));
      MixinLogWatcher.begin();
    } 
  }
  
  public static MixinEnvironment getEnvironment(Phase phase) {
    if (phase == null)
      return Phase.DEFAULT.getEnvironment(); 
    return phase.getEnvironment();
  }
  
  public static MixinEnvironment getDefaultEnvironment() {
    return getEnvironment(Phase.DEFAULT);
  }
  
  public static MixinEnvironment getCurrentEnvironment() {
    if (currentEnvironment == null)
      currentEnvironment = getEnvironment(getCurrentPhase()); 
    return currentEnvironment;
  }
  
  public static CompatibilityLevel getCompatibilityLevel() {
    return compatibility;
  }
  
  @Deprecated
  public static void setCompatibilityLevel(CompatibilityLevel level) throws IllegalArgumentException {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (!"org.spongepowered.asm.mixin.transformer.MixinConfig".equals(stackTrace[2].getClassName()))
      logger.warn("MixinEnvironment::setCompatibilityLevel is deprecated and will be removed. Set level via config instead!"); 
    if (level != compatibility && level.isAtLeast(compatibility)) {
      if (!level.isSupported())
        throw new IllegalArgumentException("The requested compatibility level " + level + " could not be set. Level is not supported"); 
      compatibility = level;
      logger.info("Compatibility level set to {}", new Object[] { level });
    } 
  }
  
  public static Profiler getProfiler() {
    return profiler;
  }
  
  static void gotoPhase(Phase phase) {
    if (phase == null || phase.ordinal < 0)
      throw new IllegalArgumentException("Cannot go to the specified phase, phase is null or invalid"); 
    if (phase.ordinal > (getCurrentPhase()).ordinal)
      MixinService.getService().beginPhase(); 
    if (phase == Phase.DEFAULT)
      MixinLogWatcher.end(); 
    currentPhase = phase;
    currentEnvironment = getEnvironment(getCurrentPhase());
  }
}
