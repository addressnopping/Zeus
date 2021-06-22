package org.spongepowered.asm.mixin.extensibility;

import org.apache.logging.log4j.Level;

public interface IMixinErrorHandler {
  ErrorAction onPrepareError(IMixinConfig paramIMixinConfig, Throwable paramThrowable, IMixinInfo paramIMixinInfo, ErrorAction paramErrorAction);
  
  ErrorAction onApplyError(String paramString, Throwable paramThrowable, IMixinInfo paramIMixinInfo, ErrorAction paramErrorAction);
  
  public enum ErrorAction {
    NONE((String)Level.INFO),
    WARN((String)Level.WARN),
    ERROR((String)Level.FATAL);
    
    public final Level logLevel;
    
    ErrorAction(Level logLevel) {
      this.logLevel = logLevel;
    }
  }
}
