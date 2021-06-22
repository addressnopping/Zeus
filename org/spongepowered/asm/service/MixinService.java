package org.spongepowered.asm.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MixinService {
  private static final Logger logger = LogManager.getLogger("mixin");
  
  private static MixinService instance;
  
  private ServiceLoader<IMixinServiceBootstrap> bootstrapServiceLoader;
  
  private final Set<String> bootedServices = new HashSet<String>();
  
  private ServiceLoader<IMixinService> serviceLoader;
  
  private IMixinService service = null;
  
  private MixinService() {
    runBootServices();
  }
  
  private void runBootServices() {
    this.bootstrapServiceLoader = ServiceLoader.load(IMixinServiceBootstrap.class, getClass().getClassLoader());
    for (IMixinServiceBootstrap bootService : this.bootstrapServiceLoader) {
      try {
        bootService.bootstrap();
        this.bootedServices.add(bootService.getServiceClassName());
      } catch (Throwable th) {
        logger.catching(th);
      } 
    } 
  }
  
  private static MixinService getInstance() {
    if (instance == null)
      instance = new MixinService(); 
    return instance;
  }
  
  public static void boot() {
    getInstance();
  }
  
  public static IMixinService getService() {
    return getInstance().getServiceInstance();
  }
  
  private synchronized IMixinService getServiceInstance() {
    if (this.service == null) {
      this.service = initService();
      if (this.service == null)
        throw new ServiceNotAvailableError("No mixin host service is available"); 
    } 
    return this.service;
  }
  
  private IMixinService initService() {
    this.serviceLoader = ServiceLoader.load(IMixinService.class, getClass().getClassLoader());
    Iterator<IMixinService> iter = this.serviceLoader.iterator();
    while (iter.hasNext()) {
      try {
        IMixinService service = iter.next();
        if (this.bootedServices.contains(service.getClass().getName()))
          logger.debug("MixinService [{}] was successfully booted in {}", new Object[] { service.getName(), getClass().getClassLoader() }); 
        if (service.isValid())
          return service; 
      } catch (ServiceConfigurationError serviceError) {
        serviceError.printStackTrace();
      } catch (Throwable th) {
        th.printStackTrace();
      } 
    } 
    return null;
  }
}
