package org.infinispan.configuration.cache;

import java.util.List;

public class LoadersConfiguration {

   private final boolean passivation;
   private final boolean preload;
   private final boolean shared;
   private final List<LoaderConfiguration> cacheLoaders;
   
   LoadersConfiguration(boolean passivation, boolean preload, boolean shared, List<LoaderConfiguration> cacheLoaders) {
      this.passivation = passivation;
      this.preload = preload;
      this.shared = shared;
      this.cacheLoaders = cacheLoaders;
   }

   public boolean isPassivation() {
      return passivation;
   }

   public boolean isPreload() {
      return preload;
   }

   public boolean isShared() {
      return shared;
   }

   public List<LoaderConfiguration> getCacheLoaders() {
      return cacheLoaders;
   }
   
}
