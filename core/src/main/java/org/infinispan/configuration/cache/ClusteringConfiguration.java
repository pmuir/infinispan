package org.infinispan.configuration.cache;

public class ClusteringConfiguration {
   
   private final CacheMode cacheMode;
   private final AsyncConfiguration asyncConfiguration;
   private final HashConfiguration hashConfiguration;
   private final L1Configuration l1Configuration;
   private final StateRetrievalConfiguration stateRetrievalConfiguration;
   
   ClusteringConfiguration(CacheMode cacheMode, AsyncConfiguration asyncConfiguration, HashConfiguration hashConfiguration,
         L1Configuration l1Configuration, StateRetrievalConfiguration stateRetrievalConfiguration) {
      this.cacheMode = cacheMode;
      this.asyncConfiguration = asyncConfiguration;
      this.hashConfiguration = hashConfiguration;
      this.l1Configuration = l1Configuration;
      this.stateRetrievalConfiguration = stateRetrievalConfiguration;
   }

   public CacheMode getCacheMode() {
      return cacheMode;
   }
   
   public AsyncConfiguration getAsyncConfiguration() {
      return asyncConfiguration;
   }
   
   public HashConfiguration getHashConfiguration() {
      return hashConfiguration;
   }
   
   public L1Configuration getL1Configuration() {
      return l1Configuration;
   }

}
