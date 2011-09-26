package org.infinispan.configuration.cache;

public class RecoveryConfiguration {

   private final boolean enabled;
   private final String recoveryInfoCacheName;
   
   RecoveryConfiguration(boolean enabled, String recoveryInfoCacheName) {
      this.enabled = enabled;
      this.recoveryInfoCacheName = recoveryInfoCacheName;
   }

   public boolean isEnabled() {
      return enabled;
   }

   public String getRecoveryInfoCacheName() {
      return recoveryInfoCacheName;
   }
   
}
