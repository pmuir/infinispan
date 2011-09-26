package org.infinispan.configuration.cache;

public class AsyncLoaderConfiguration {
   
   private final boolean enabled;
   private final long flushLockTimeout;
   private final int modificationQueueSize;
   private final long shutdownTimeout;
   private final long threadPoolSize;
   
   AsyncLoaderConfiguration(boolean enabled, long flushLockTimeout, int modificationQueueSize, long shutdownTimeout,
         long threadPoolSize) {
      this.enabled = enabled;
      this.flushLockTimeout = flushLockTimeout;
      this.modificationQueueSize = modificationQueueSize;
      this.shutdownTimeout = shutdownTimeout;
      this.threadPoolSize = threadPoolSize;
   }

   public boolean isEnabled() {
      return enabled;
   }

   public long getFlushLockTimeout() {
      return flushLockTimeout;
   }

   public int getModificationQueueSize() {
      return modificationQueueSize;
   }

   public long getShutdownTimeout() {
      return shutdownTimeout;
   }

   public long getThreadPoolSize() {
      return threadPoolSize;
   }

}
