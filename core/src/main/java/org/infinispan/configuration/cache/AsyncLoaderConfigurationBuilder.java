package org.infinispan.configuration.cache;

public class AsyncLoaderConfigurationBuilder extends AbstractLoaderConfigurationChildBuilder<AsyncLoaderConfiguration> {

   private boolean enabled;
   private long flushLockTimeout;
   private int modificationQueueSize;
   private long shutdownTimeout;
   private long threadPoolSize;
   
   AsyncLoaderConfigurationBuilder(LoaderConfigurationBuilder builder) {
      super(builder);
   }

   public AsyncLoaderConfigurationBuilder enable() {
      this.enabled = true;
      return this;
   }
   
   public AsyncLoaderConfigurationBuilder disable() {
      this.enabled = false;
      return this;
   }

   public AsyncLoaderConfigurationBuilder flushLockTimeout(long l) {
      this.flushLockTimeout = l;
      return this;
   }

   public AsyncLoaderConfigurationBuilder modificationQueueSize(int i) {
      this.modificationQueueSize = i;
      return this;
   }

   public AsyncLoaderConfigurationBuilder shutdownTimeout(long l) {
      this.shutdownTimeout = l;
      return this;
   }

   public AsyncLoaderConfigurationBuilder threadPoolSize(long l) {
      this.threadPoolSize = l;
      return this;
   }

   @Override
   void validate() {
      // TODO Auto-generated method stub
      
   }

   @Override
   AsyncLoaderConfiguration create() {
      return new AsyncLoaderConfiguration(enabled, flushLockTimeout, modificationQueueSize, shutdownTimeout, threadPoolSize);
   }

}
