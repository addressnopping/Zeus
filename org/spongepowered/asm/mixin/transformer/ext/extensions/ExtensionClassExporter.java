package org.spongepowered.asm.mixin.transformer.ext.extensions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IDecompiler;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.util.Constants;
import org.spongepowered.asm.util.perf.Profiler;

public class ExtensionClassExporter implements IExtension {
  private static final String DECOMPILER_CLASS = "org.spongepowered.asm.mixin.transformer.debug.RuntimeDecompiler";
  
  private static final String EXPORT_CLASS_DIR = "class";
  
  private static final String EXPORT_JAVA_DIR = "java";
  
  private static final Logger logger = LogManager.getLogger("mixin");
  
  private final File classExportDir = new File(Constants.DEBUG_OUTPUT_DIR, "class");
  
  private final IDecompiler decompiler;
  
  public ExtensionClassExporter(MixinEnvironment env) {
    this.decompiler = initDecompiler(env, new File(Constants.DEBUG_OUTPUT_DIR, "java"));
    try {
      FileUtils.deleteDirectory(this.classExportDir);
    } catch (IOException ex) {
      logger.warn("Error cleaning class output directory: {}", new Object[] { ex.getMessage() });
    } 
  }
  
  public boolean isDecompilerActive() {
    return (this.decompiler != null);
  }
  
  private IDecompiler initDecompiler(MixinEnvironment env, File outputPath) {
    if (!env.getOption(MixinEnvironment.Option.DEBUG_EXPORT_DECOMPILE))
      return null; 
    try {
      boolean as = env.getOption(MixinEnvironment.Option.DEBUG_EXPORT_DECOMPILE_THREADED);
      logger.info("Attempting to load Fernflower decompiler{}", new Object[] { as ? " (Threaded mode)" : "" });
      String className = "org.spongepowered.asm.mixin.transformer.debug.RuntimeDecompiler" + (as ? "Async" : "");
      Class<? extends IDecompiler> clazz = (Class)Class.forName(className);
      Constructor<? extends IDecompiler> ctor = clazz.getDeclaredConstructor(new Class[] { File.class });
      IDecompiler decompiler = ctor.newInstance(new Object[] { outputPath });
      logger.info("Fernflower decompiler was successfully initialised, exported classes will be decompiled{}", new Object[] { as ? " in a separate thread" : "" });
      return decompiler;
    } catch (Throwable th) {
      logger.info("Fernflower could not be loaded, exported classes will not be decompiled. {}: {}", new Object[] { th
            .getClass().getSimpleName(), th.getMessage() });
      return null;
    } 
  }
  
  private String prepareFilter(String filter) {
    filter = "^\\Q" + filter.replace("**", "").replace("*", "").replace("?", "") + "\\E$";
    return filter.replace("", "\\E.*\\Q").replace("", "\\E[^\\.]+\\Q").replace("", "\\E.\\Q").replace("\\Q\\E", "");
  }
  
  private boolean applyFilter(String filter, String subject) {
    return Pattern.compile(prepareFilter(filter), 2).matcher(subject).matches();
  }
  
  public boolean checkActive(MixinEnvironment environment) {
    return true;
  }
  
  public void preApply(ITargetClassContext context) {}
  
  public void postApply(ITargetClassContext context) {}
  
  public void export(MixinEnvironment env, String name, boolean force, byte[] bytes) {
    if (force || env.getOption(MixinEnvironment.Option.DEBUG_EXPORT)) {
      String filter = env.getOptionValue(MixinEnvironment.Option.DEBUG_EXPORT_FILTER);
      if (force || filter == null || applyFilter(filter, name)) {
        Profiler.Section exportTimer = MixinEnvironment.getProfiler().begin("debug.export");
        File outputFile = dumpClass(name.replace('.', '/'), bytes);
        if (this.decompiler != null)
          this.decompiler.decompile(outputFile); 
        exportTimer.end();
      } 
    } 
  }
  
  public File dumpClass(String fileName, byte[] bytes) {
    File outputFile = new File(this.classExportDir, fileName + ".class");
    try {
      FileUtils.writeByteArrayToFile(outputFile, bytes);
    } catch (IOException iOException) {}
    return outputFile;
  }
}
