package org.spongepowered.asm.service;

import java.net.URL;

public interface IClassProvider {
  URL[] getClassPath();
  
  Class<?> findClass(String paramString) throws ClassNotFoundException;
  
  Class<?> findClass(String paramString, boolean paramBoolean) throws ClassNotFoundException;
  
  Class<?> findAgentClass(String paramString, boolean paramBoolean) throws ClassNotFoundException;
}
