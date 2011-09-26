package org.infinispan.configuration.cache;

public class Configuration {
   
   private final String name;
   private final ClusteringConfiguration clusteringConfiguration;
   private final CustomInterceptorsConfiguration customInterceptorsConfiguration;
   private final DataContainerConfiguration dataContainerConfiguration;
   private final DeadlockDetectionConfiguration deadlockDetectionConfiguration;
   private final EvictionConfiguration evictionConfiguration;
   private final ExpirationConfiguration expirationConfiguration;
   private final IndexingConfiguration indexingConfiguration;
   private final InvocationBatchingConfiguration invocationBatchingConfiguration;
   private final JMXStatisticsConfiguration jmxStatisticsConfiguration;
   private final LazyDeserializationConfiguration lazyDeserializationConfiguration;
   private final LoadersConfiguration loadersConfiguration;
   private final LockingConfiguration lockingConfiguration;
   private final StoreAsBinaryConfiguration storeAsBinaryConfiguration;
   private final TransactionConfiguration transactionConfiguration;
   private final UnsafeConfiguration unsafeConfiguration;

   Configuration(String name, ClusteringConfiguration clusteringConfiguration,
         CustomInterceptorsConfiguration customInterceptorsConfiguration,
         DataContainerConfiguration dataContainerConfiguration, DeadlockDetectionConfiguration deadlockDetectionConfiguration,
         EvictionConfiguration evictionConfiguration, ExpirationConfiguration expirationConfiguration,
         IndexingConfiguration indexingConfiguration, InvocationBatchingConfiguration invocationBatchingConfiguration,
         JMXStatisticsConfiguration jmxStatisticsConfiguration,
         LazyDeserializationConfiguration lazyDeserializationConfiguration, LoadersConfiguration loadersConfiguration,
         LockingConfiguration lockingConfiguration, StoreAsBinaryConfiguration storeAsBinaryConfiguration,
         TransactionConfiguration transactionConfiguration, UnsafeConfiguration unsafeConfiguration) {
      this.name = name;
      this.clusteringConfiguration = clusteringConfiguration;
      this.customInterceptorsConfiguration = customInterceptorsConfiguration;
      this.dataContainerConfiguration = dataContainerConfiguration;
      this.deadlockDetectionConfiguration = deadlockDetectionConfiguration;
      this.evictionConfiguration = evictionConfiguration;
      this.expirationConfiguration = expirationConfiguration;
      this.indexingConfiguration = indexingConfiguration;
      this.invocationBatchingConfiguration = invocationBatchingConfiguration;
      this.jmxStatisticsConfiguration = jmxStatisticsConfiguration;
      this.lazyDeserializationConfiguration = lazyDeserializationConfiguration;
      this.loadersConfiguration = loadersConfiguration;
      this.lockingConfiguration = lockingConfiguration;
      this.storeAsBinaryConfiguration = storeAsBinaryConfiguration;
      this.transactionConfiguration = transactionConfiguration;
      this.unsafeConfiguration = unsafeConfiguration;
   }

   public String getName() {
      return name;
   }
   
   public ClusteringConfiguration getClustering() {
      return clusteringConfiguration;
   }
   
   public CustomInterceptorsConfiguration getCustomInterceptors() {
      return customInterceptorsConfiguration;
   }
   
   public DataContainerConfiguration getDataContainer() {
      return dataContainerConfiguration;
   }
   
   public DeadlockDetectionConfiguration getDeadlockDetectionn() {
      return deadlockDetectionConfiguration;
   }
   
   public EvictionConfiguration getEviction() {
      return evictionConfiguration;
   }
   
   public ExpirationConfiguration getExpiration() {
      return expirationConfiguration;
   }
   
   public IndexingConfiguration getIndexing() {
      return indexingConfiguration;
   }
   
   public InvocationBatchingConfiguration getInvocationBatching() {
      return invocationBatchingConfiguration;
   }
   
   public JMXStatisticsConfiguration getJmxStatistics() {
      return jmxStatisticsConfiguration;
   }
   
   public LazyDeserializationConfiguration getLazyDeserialization() {
      return lazyDeserializationConfiguration;
   }
   
   public LoadersConfiguration getLoaders() {
      return loadersConfiguration;
   }
   
   public LockingConfiguration getLocking() {
      return lockingConfiguration;
   }
   
   public StoreAsBinaryConfiguration getStoreAsBinary() {
      return storeAsBinaryConfiguration;
   }
   
   public TransactionConfiguration getTransaction() {
      return transactionConfiguration;
   }
   
   public UnsafeConfiguration getUnsafe() {
      return unsafeConfiguration;
   }

}
