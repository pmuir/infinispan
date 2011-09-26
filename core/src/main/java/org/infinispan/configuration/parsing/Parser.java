package org.infinispan.configuration.parsing;

import java.util.Collections;
import java.util.Properties;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.InterceptorConfiguration.Position;
import org.infinispan.configuration.cache.InterceptorConfigurationBuilder;
import org.infinispan.configuration.cache.LoaderConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.ShutdownHookBehavior;
import org.infinispan.container.DataContainer;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.distribution.ch.HashSeed;
import org.infinispan.distribution.group.Grouper;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionThreadPolicy;
import org.infinispan.executors.ExecutorFactory;
import org.infinispan.executors.ScheduledExecutorFactory;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.jmx.MBeanServerLookup;
import org.infinispan.loaders.CacheLoader;
import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.marshall.Marshaller;
import org.infinispan.remoting.ReplicationQueue;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.infinispan.util.Util;
import org.infinispan.util.concurrent.IsolationLevel;
import org.jboss.staxmapper.XMLExtendedStreamReader;

public class Parser {

   private final ClassLoader cl;

   public Parser(ClassLoader cl) {
      this.cl = cl;
   }

   public ConfigurationBuilderHolder parse(XMLExtendedStreamReader reader) throws XMLStreamException {

      ConfigurationBuilderHolder holder = new ConfigurationBuilderHolder();

      ParseUtils.requireNoAttributes(reader);

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case DEFAULT: {
               parseDefaultCache(reader, holder.getDefaultConfigurationBuilder());
               break;
            }
            case GLOBAL: {
               parseGlobal(reader, holder.getGlobalConfigurationBuilder());
               break;
            }
            case NAMED_CACHE: {
               parseNamedCache(reader, holder.newConfigurationBuilder());
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
      return holder;
   }

   private void parseNamedCache(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      ParseUtils.requireSingleAttribute(reader, "name");
      String name = ParseUtils.readStringAttributeElement(reader, "name");
      builder.name(name);
      parseCache(reader, builder);
   }

   private void parseDefaultCache(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      ParseUtils.requireNoAttributes(reader);
      parseCache(reader, builder);
   }

   private void parseCache(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case CLUSTERING:
               parseClustering(reader, builder);
               break;
            case CUSTOM_INTERCEPTORS:
               parseCustomInterceptors(reader, builder);
               break;
            case DATA_CONTAINER:
               parseDataContainer(reader, builder);
               break;
            case DEADLOCK_DETECTION:
               parseDeadlockDetection(reader, builder);
               break;
            case EVICTION:
               parseEviction(reader, builder);
               break;
            case EXPIRATION:
               parseExpiration(reader, builder);
               break;
            case INDEXING:
               parseIndexing(reader, builder);
               break;
            case INVOCATION_BATCHING:
               parseInvocationBatching(reader, builder);
               break;
            case JMX_STATISTICS:
               parseJmxStatistics(reader, builder);
               break;
            case LAZY_DESERIALIZATION:
               parseLazyDeserialization(reader, builder);
               break;
            case LOADERS:
               parseLoaders(reader, builder);
               break;
            case LOCKING:
               parseLocking(reader, builder);
               break;
            case STORE_AS_BINARY:
               parseStoreAsBinary(reader, builder);
               break;
            case TRANSACTION:
               parseTransaction(reader, builder);
               break;
            case UNSAFE:
               parseUnsafe(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   private void parseTransaction(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case AUTO_COMMIT:
               builder.transaction().autoCommit(Boolean.valueOf(value).booleanValue());
               break;
            case CACHE_STOP_TIMEOUT:
               builder.transaction().cacheStopTimeout(Integer.valueOf(value).intValue());
               break;
            case EAGER_LOCK_SINGLE_NODE:
               builder.transaction().eagerLockingSingleNode(Boolean.valueOf(value).booleanValue());
               break;
            case LOCKING_MODE:
               builder.transaction().lockingMode(LockingMode.valueOf(value));
               break;
            case SYNC_COMMIT_PHASE:
               builder.transaction().syncCommitPhase(Boolean.valueOf(value).booleanValue());
               break;
            case SYNC_ROLLBACK_PHASE:
               builder.transaction().syncRollbackPhase(Boolean.valueOf(value).booleanValue());
               break;
            case TRANSACTION_MANAGER_LOOKUP_CLASS:
               builder.transaction().transactionManagerLookup(Util.<TransactionManagerLookup>getInstance(value, cl));
               break;
            case TRANSACTION_MODE:
               builder.transaction().transactionMode(TransactionMode.valueOf(value));
               break;
            case USE_EAGER_LOCKING:
               builder.transaction().useEagerLocking(Boolean.valueOf(value).booleanValue());
               break;
            case USE_SYNCHRONIZAION:
               builder.transaction().useSynchronization(Boolean.valueOf(value).booleanValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case RECOVERY:
               parseRecovery(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
      
   }

   private void parseRecovery(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.transaction().recovery().enable();
               else
                  builder.transaction().recovery().disable();
               break;
            case RECOVERY_INFO_CACHE_NAME:
               builder.transaction().recovery().recoveryInfoCacheName(value);
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
   }

   private void parseUnsafe(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case UNRELIABLE_RETURN_VALUES:
               builder.unsafe().unreliableReturnValues(Boolean.valueOf(value).booleanValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
      
   }

   private void parseStoreAsBinary(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.storeAsBinary().enable();
               else
                  builder.storeAsBinary().disable();
               break;
            case STORE_KEYS_AS_BINARY:
               builder.storeAsBinary().storeKeysAsBinary(Boolean.valueOf(value).booleanValue());
               break;
            case STORE_VALUES_AS_BINARY:
               builder.storeAsBinary().storeValuesAsBinary(Boolean.valueOf(value).booleanValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
      
   }

   private void parseLocking(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CONCURRENCY_LEVEL:
               builder.locking().concurrencyLevel(Integer.valueOf(value).intValue());
               break;
            case ISOLATION_LEVEL:
               builder.locking().isolationLevel(IsolationLevel.valueOf(value));
               break;
            case LOCK_ACQUISITION_TIMEOUT:
               builder.locking().lockAcquisitionTimeout(Long.valueOf(value).longValue());
               break;
            case USE_LOCK_STRIPING:
               builder.locking().useLockStriping(Boolean.valueOf(value).booleanValue());
               break;
            case WRITE_SKEW_CHECK:
               builder.locking().writeSkewCheck(Boolean.valueOf(value).booleanValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
      
   }

   private void parseLoaders(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case PASSIVATION:
               builder.loaders().passivation(Boolean.valueOf(value).booleanValue());
               break;
            case PRELOAD:
               builder.loaders().preload(Boolean.valueOf(value).booleanValue());
               break;
            case SHARED:
               builder.loaders().shared(Boolean.valueOf(value).booleanValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case LOADER:
               parseLoader(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   private void parseLoader(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      
      LoaderConfigurationBuilder loaderBuilder = builder.loaders().addCacheLoader();
      
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CLASS:
               loaderBuilder.cacheLoader(Util.<CacheLoader>getInstance(value, cl));
               break;
            case FETCH_PERSISTENT_STATE:
               loaderBuilder.fetchPersistentState(Boolean.valueOf(value).booleanValue());
               break;
            case IGNORE_MODIFICATIONS:
               loaderBuilder.ignoreModifications(Boolean.valueOf(value).booleanValue());
               break;
            case PURGE_ON_STARTUP:
               loaderBuilder.purgeOnStartup(Boolean.valueOf(value).booleanValue());
               break;
            case PURGER_THREADS:
               loaderBuilder.purgerThreads(Integer.valueOf(value).intValue());
               break;
            case PURGE_SYNCHRONOUSLY:
               loaderBuilder.purgeSynchronously(Boolean.valueOf(value).booleanValue());
               break; 
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ASYNC:
               parseAsyncLoader(reader, loaderBuilder);
               break;
            case PROPERTIES:
               loaderBuilder.withProperties(parseProperties(reader));
               break;
            case SINGLETON_STORE:
               parseSingletonStore(reader, loaderBuilder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
      
   }

   private void parseSingletonStore(XMLExtendedStreamReader reader, LoaderConfigurationBuilder loaderBuilder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  loaderBuilder.singletonStore().enable();
               else
                  loaderBuilder.singletonStore().disable();
               break;
            case PUSH_STATE_TIMEOUT:
               loaderBuilder.singletonStore().pushStateTimeout(Long.valueOf(value).longValue());
               break;
            case PUSH_STATE_WHEN_COORDINATOR:
               loaderBuilder.singletonStore().pushStateWhenCoordinator(Boolean.valueOf(value).booleanValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
   }

   private void parseAsyncLoader(XMLExtendedStreamReader reader, LoaderConfigurationBuilder loaderBuilder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  loaderBuilder.async().enable();
               else
                  loaderBuilder.async().disable();
               break;
            case FLUSH_LOCK_TIMEOUT:
               loaderBuilder.async().flushLockTimeout(Long.valueOf(value).longValue());
               break;
            case MODIFICTION_QUEUE_SIZE:
               loaderBuilder.async().modificationQueueSize(Integer.valueOf(value).intValue());
               break;
            case SHUTDOWN_TIMEOUT:
               loaderBuilder.async().shutdownTimeout(Long.valueOf(value).longValue());
               break;
            case THREAD_POOL_SIZE:
               loaderBuilder.async().threadPoolSize(Long.valueOf(value).longValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
      
   }

   private void parseLazyDeserialization(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.lazyDeserialization().enable();
               else
                  builder.lazyDeserialization().disable();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
   }

   private void parseJmxStatistics(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.jmxStatistics().enable();
               else
                  builder.jmxStatistics().disable();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
   }

   private void parseInvocationBatching(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.invocationBatching().enable();
               else
                  builder.invocationBatching().disable();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
      
   }

   private void parseIndexing(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.indexing().enable();
               else
                  builder.indexing().disable();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      builder.indexing().withProperties(parseProperties(reader));
      
   }

   private void parseExpiration(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case LIFESPAN:
               builder.expiration().lifespan(Long.valueOf(value).longValue());
               break;
            case MAX_IDLE:
               builder.expiration().maxIdle(Long.valueOf(value).longValue());
               break;
            case REAPER_ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.expiration().enableReaper();
               else
                  builder.expiration().disableReaper();
               break;
            case WAKE_UP_INTERVAL:
               builder.expiration().wakeUpInterval(Long.valueOf(value).longValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
      
   }

   private void parseEviction(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case MAX_ENTRIES:
               builder.eviction().maxEntries(Integer.valueOf(value).intValue());
               break;
            case STRATEGY:
               builder.eviction().strategy(EvictionStrategy.valueOf(value));
               break;
            case THREAD_POLICY:
               builder.eviction().threadPolicy(EvictionThreadPolicy.valueOf(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
      
   }

   private void parseDeadlockDetection(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.deadlockDetection().enable();
               else
                  builder.deadlockDetection().disable();
               break;
            case SPIN_DURATION:
               builder.deadlockDetection().spinDuration(Long.valueOf(value).intValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
      
   }

   private void parseDataContainer(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CLASS:
               builder.dataContainer().dataContainer(Util.<DataContainer>getInstance(value, cl));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      builder.dataContainer().withProperties(parseProperties(reader));
   }

   private void parseCustomInterceptors(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      ParseUtils.requireNoAttributes(reader);
      
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case INTERCEPTOR:
               parseInterceptor(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
      
   }

   private void parseInterceptor(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      
      InterceptorConfigurationBuilder interceptorBuilder = builder.customInterceptors().addInterceptor();
      
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case AFTER:
               interceptorBuilder.after(Util.<CommandInterceptor>loadClass(value, cl));
               break;
            case BEFORE:
               interceptorBuilder.before(Util.<CommandInterceptor>loadClass(value, cl));
               break;
            case CLASS:
               interceptorBuilder.interceptor(Util.<CommandInterceptor>getInstance(value, cl));
               break;
            case INDEX:
               interceptorBuilder.index(Integer.valueOf(value).intValue());
               break;
            case POSITION:
               interceptorBuilder.position(Position.valueOf(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
      ParseUtils.requireNoContent(reader);
   }

   private void parseClustering(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case MODE:
               builder.clustering().cacheMode(CacheMode.valueOf(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ASYNC:
               parseAsync(reader, builder);
               break;
            case HASH:
               parseHash(reader, builder);
               break;
            case L1:
               parseL1reader(reader, builder);
               break;
            case STATE_RETRIEVAL:
               parseStateRetrieval(reader, builder);
               break;
            case SYNC:
               parseSync(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   private void parseSync(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      ParseUtils.requireNoContent(reader);

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case REPL_TIMEOUT:
               builder.clustering().sync().replTimeout(Long.valueOf(value).longValue());
               break;
           
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      
   }

   private void parseStateRetrieval(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException{
      ParseUtils.requireNoContent(reader);

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ALWAYS_PROVIDE_IN_MEMORY_STATE:
               builder.clustering().stateRetrieval().alwaysProvideInMemoryState(Boolean.valueOf(value).booleanValue());
               break;
            case FETCH_IN_MEMORY_STATE:
               builder.clustering().stateRetrieval().fetchInMemoryState(Boolean.valueOf(value).booleanValue());
               break;
            case INITIAL_RETRY_WAIT_TIME:
               builder.clustering().stateRetrieval().initialRetryWaitTime(Long.valueOf(value).longValue());
               break;
            case LOG_FLUSH_TIMEOUT:
               builder.clustering().stateRetrieval().logFlushTimeout(Long.valueOf(value).longValue());
               break;
            case MAX_NON_PROGRESSING_LOG_WRITES:
               builder.clustering().stateRetrieval().maxNonPorgressingLogWrites(Integer.valueOf(value).intValue());
               break;
            case NUM_RETRIES:
               builder.clustering().stateRetrieval().numRetries(Integer.valueOf(value).intValue());
               break;
            case RETRY_WAIT_TIME_INCREASE_FACTOR:
               builder.clustering().stateRetrieval().retryWaitTimeIncreaseFactor(Integer.valueOf(value).intValue());
               break;
            case TIMEOUT:
               builder.clustering().stateRetrieval().timeout(Long.valueOf(value).longValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

   }

   private void parseL1reader(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      ParseUtils.requireNoContent(reader);

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.clustering().l1().enable();
               else
                  builder.clustering().l1().disable();
               break;
            case INVALIDATION_THRESHOLD:
               builder.clustering().l1().invalidationThreshold(Integer.valueOf(value).intValue());
               break;
            case LIFESPAN:
               builder.clustering().l1().lifespan(Long.valueOf(value).longValue());
               break;
            case ON_REHASH:
               if (Boolean.valueOf(value).booleanValue())
                  builder.clustering().l1().enableOnRehash();
               else
                  builder.clustering().l1().disableOnRehash();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

   }

   private void parseHash(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CLASS:
            case HASH_FUNCTION_CLASS:
               builder.clustering().hash().consistentHash(Util.<ConsistentHash> getInstance(value, cl));
               break;
            case HASH_SEED_CLASS:
               builder.clustering().hash().hashSeed(Util.<HashSeed> getInstance(value, cl));
               break;
            case NUM_OWNERS:
               builder.clustering().hash().numOwners(Integer.valueOf(value).intValue());
               break;
            case NUM_VIRTUAL_NODES:
               builder.clustering().hash().numVirtualNodes(Integer.valueOf(value).intValue());
               break;
            case REHASH_ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.clustering().hash().rehashEnabled();
               else
                  builder.clustering().hash().rehashDisabled();
               break;
            case REHASH_RPC_TIMEOUT:
               builder.clustering().hash().rehashRpcTimeout(Long.valueOf(value).longValue());
               break;
            case REHASH_WAIT:
               builder.clustering().hash().rehashWait(Long.valueOf(value).longValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case GROUPS:
               parseGroups(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }

   }

   private void parseGroups(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      ParseUtils.requireSingleAttribute(reader, "enabled");

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.valueOf(value).booleanValue())
                  builder.clustering().hash().groups().enabled();
               else
                  builder.clustering().hash().groups().disabled();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case GROUPER:
               String value = ParseUtils.readStringAttributeElement(reader, "class");
               builder.clustering().hash().groups().addGrouper(Util.<Grouper<?>>getInstance(value, cl));
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }

   }

   private void parseAsync(XMLExtendedStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ASYNC_MARSHALLING:
               if (Boolean.valueOf(value).booleanValue())
                  builder.clustering().async().asyncMarshalling();
               else
                  builder.clustering().async().syncMarshalling();
               break;
            case REPL_QUEUE_CLASS:
               builder.clustering().async().replQueue(Util.<ReplicationQueue> getInstance(value, cl));
               break;
            case REPL_QUEUE_INTERVAL:
               builder.clustering().async().replQueueInterval(Long.valueOf(value).longValue());
               break;
            case REPL_QUEUE_MAX_ELEMENTS:
               builder.clustering().async().replQueueMaxElements(Integer.valueOf(value).intValue());
               break;
            case USE_REPL_QUEUE:
               builder.clustering().async().useReplQueue(Boolean.valueOf(value).booleanValue());
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseGlobal(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder) throws XMLStreamException {

      ParseUtils.requireNoAttributes(reader);

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ASYNC_LISTENER_EXECUTOR: {
               parseAsyncListenerExectuor(reader, builder);
               break;
            }
            case ASYNC_TRANSPORT_EXECUTOR: {
               parseAsyncTransportExecutor(reader, builder);
               break;
            }
            case EVICTION_SCHEDULED_EXECUTOR: {
               parseEvictionScheduledExecutor(reader, builder);
               break;
            }
            case GLOBAL_JMX_STATISTICS: {
               parseGlobalJMXStatistics(reader, builder);
               break;
            }
            case REPLICATION_QUEUE_SCHEDULED_EXECUTOR: {
               parseReplicationQueueScheduledExecutor(reader, builder);
               break;
            }
            case SERIALIZATION: {
               parseSerialization(reader, builder);
               break;
            }
            case SHUTDOWN: {
               parseShutdown(reader, builder);
               break;
            }
            case TRANSPORT: {
               parseTransport(reader, builder);
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseTransport(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder) throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CLUSTER_NAME: {
               builder.transport().clusterName(value);
               break;
            }
            case DISTRIBUTED_SYNC_TIMEOUT: {
               builder.transport().distributedSyncTimeout(Long.valueOf(value));
               break;
            }
            case MACHINE_ID: {
               builder.transport().machineId(value);
               break;
            }
            case NODE_NAME: {
               builder.transport().nodeName(value);
               break;
            }
            case RACK_ID: {
               builder.transport().rackId(value);
               break;
            }
            case SITE_ID: {
               builder.transport().siteId(value);
               break;
            }
            case STRICT_PEER_TO_PEER: {
               builder.transport().strictPeerToPeer(Boolean.valueOf(value));
               break;
            }
            case TRANSPORT_CLASS: {
               builder.transport().transport(Util.<Transport> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.transport().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseShutdown(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder) throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case HOOK_BEHAVIOR: {
               builder.shutdown().hookBehavior(ShutdownHookBehavior.valueOf(value));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }

      ParseUtils.requireNoContent(reader);
   }

   private void parseSerialization(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case MARSHALLER_CLASS: {
               builder.serialization().marshallerClass(Util.<Marshaller> loadClass(value, cl));
               break;
            }
            case VERSION: {
               builder.serialization().version(Short.valueOf(value));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ADVANCED_EXTERNALIZERS: {
               parseAdvancedExternalizers(reader, builder);
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }

   }

   private void parseAdvancedExternalizers(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {

      ParseUtils.requireNoAttributes(reader);

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ADVANCED_EXTERNALIZER: {
               int attributes = reader.getAttributeCount();
               AdvancedExternalizer<?> advancedExternalizer = null;
               Integer id = null;
               ParseUtils.requireAttributes(reader, Attribute.EXTERNALIZER_CLASS.getLocalName());
               ParseUtils.requireNoContent(reader);
               for (int i = 0; i < attributes; i++) {
                  String value = reader.getAttributeValue(i);
                  Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                  switch (attribute) {
                     case EXTERNALIZER_CLASS: {
                        advancedExternalizer = Util.getInstance(value, cl);
                        break;
                     }
                     case ID: {
                        id = Integer.valueOf(value);
                        break;
                     }
                     default: {
                        throw ParseUtils.unexpectedAttribute(reader, i);
                     }
                  }
               }
               if (id != null)
                  builder.serialization().addAdvancedExternalizer(id, advancedExternalizer);
               else
                  builder.serialization().addAdvancedExternalizer(advancedExternalizer);
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseReplicationQueueScheduledExecutor(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FACTORY: {
               builder.replicationQueueScheduledExecutor().factory(Util.<ScheduledExecutorFactory> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.replicationQueueScheduledExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseGlobalJMXStatistics(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         // allowDuplicateDomains="true" cacheManagerName="" enabled="true" jmxDomain=""
         // mBeanServerLookup
         switch (attribute) {
            case ALLOW_DUPLICATE_DOMAINS: {
               builder.globalJmxStatistics().allowDuplicateDomains(Boolean.valueOf(value));
               break;
            }
            case CACHE_MANAGER_NAME: {
               builder.globalJmxStatistics().cacheManagerName(value);
               break;
            }
            case ENABLED: {
               if (!Boolean.valueOf(value))
                  builder.globalJmxStatistics().disable();
               break;
            }
            case JMX_DOMAIN: {
               builder.globalJmxStatistics().jmxDomain(value);
               break;
            }
            case MBEAN_SERVER_LOOKUP: {
               builder.globalJmxStatistics().mBeanServerLookup(Util.<MBeanServerLookup> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.evictionScheduledExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseEvictionScheduledExecutor(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FACTORY: {
               builder.evictionScheduledExecutor().factory(Util.<ScheduledExecutorFactory> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.evictionScheduledExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseAsyncTransportExecutor(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FACTORY: {
               builder.asyncTransportExecutor().factory(Util.<ExecutorFactory> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.asyncTransportExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseAsyncListenerExectuor(XMLExtendedStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FACTORY: {
               builder.asyncListenerExecutor().factory(Util.<ExecutorFactory> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.asyncListenerExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }

   }

   public static Properties parseProperties(XMLExtendedStreamReader reader) throws XMLStreamException {

      ParseUtils.requireNoAttributes(reader);

      Properties p = new Properties();
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTY: {
               ParseUtils.requireNoContent(reader);
               int attributes = reader.getAttributeCount();
               String key = null;
               for (int i = 0; i < attributes; i++) {
                  String value = reader.getAttributeValue(i);
                  Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                  switch (attribute) {
                     case NAME: {
                        key = value;
                        break;
                     }
                     default: {
                        throw ParseUtils.unexpectedAttribute(reader, i);
                     }
                  }
               }
               if (key == null) {
                  throw ParseUtils.missingRequired(reader, Collections.singleton(Attribute.NAME));
               }
               String value = reader.getElementText();
               p.put(key, value);
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
      return p;
   }

}
