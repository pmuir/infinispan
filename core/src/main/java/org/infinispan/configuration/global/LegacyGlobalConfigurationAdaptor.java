package org.infinispan.configuration.global;

import org.infinispan.config.AdvancedExternalizerConfig;
import org.infinispan.executors.ExecutorFactory;
import org.infinispan.executors.ScheduledExecutorFactory;
import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.marshall.Marshaller;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.util.Util;

public class LegacyGlobalConfigurationAdaptor {
   
   public GlobalConfigurationBuilder adapt(org.infinispan.config.GlobalConfiguration legacy) {
      GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder();
      
      ClassLoader cl = legacy.getClassLoader();
      
      builder.classLoader(cl);
      
      builder.transport()
         .clusterName(legacy.getClusterName())
         .machineId(legacy.getMachineId())
         .rackId(legacy.getRackId())
         .siteId(legacy.getSiteId())
         .strictPeerToPeer(legacy.isStrictPeerToPeer())
         .distributedSyncTimeout(legacy.getDistributedSyncTimeout())
         .transport(Util.<Transport>getInstance(legacy.getTransportClass(), cl))
         .nodeName(legacy.getTransportNodeName())
         .withProperties(legacy.getTransportProperties());
      
      if (legacy.isExposeGlobalJmxStatistics())
         builder.globalJmxStatistics().enable();
      else
         builder.globalJmxStatistics().disable();
      builder.globalJmxStatistics()
         .jmxDomain(legacy.getJmxDomain())
         .mBeanServerLookup(legacy.getMBeanServerLookupInstance())
         .allowDuplicateDomains(legacy.isAllowDuplicateDomains())
         .cacheManagerName(legacy.getCacheManagerName())
         .withProperties(legacy.getMBeanServerProperties());
      
      builder.serialization()
         .marshallerClass(Util.<Marshaller>loadClass(legacy.getMarshallerClass(), cl))
         .version(legacy.getMarshallVersion());
      for (AdvancedExternalizerConfig aec : legacy.getExternalizers()) {
         if (aec.getId() != null) {
            if (aec.getAdvancedExternalizer() != null)
               builder.serialization().addAdvancedExternalizer(aec.getId(), aec.getAdvancedExternalizer());
            else if (aec.getExternalizerClass() != null)
               builder.serialization().addAdvancedExternalizer(aec.getId(), Util.<AdvancedExternalizer<?>>getInstance(aec.getExternalizerClass(), cl));
         }
         else if (aec.getId() == null) {
            if (aec.getAdvancedExternalizer() != null)
               builder.serialization().addAdvancedExternalizer(aec.getAdvancedExternalizer());
            else if (aec.getExternalizerClass() != null)
               builder.serialization().addAdvancedExternalizer(Util.<AdvancedExternalizer<?>>getInstance(aec.getExternalizerClass(), cl));
         }
      }
      
      builder.asyncTransportExecutor()
         .factory(Util.<ExecutorFactory>getInstance(legacy.getAsyncTransportExecutorFactoryClass(), cl))
         .withProperties(legacy.getAsyncTransportExecutorProperties());
      
      builder.asyncListenerExecutor()
         .factory(Util.<ExecutorFactory>getInstance(legacy.getAsyncListenerExecutorFactoryClass(), cl))
         .withProperties(legacy.getAsyncListenerExecutorProperties());
      
      builder.evictionScheduledExecutor()
         .factory(Util.<ScheduledExecutorFactory>getInstance(legacy.getEvictionScheduledExecutorFactoryClass(), cl))
         .withProperties(legacy.getEvictionScheduledExecutorProperties());
      
      builder.replicationQueueScheduledExecutor()
      .factory(Util.<ScheduledExecutorFactory>getInstance(legacy.getReplicationQueueScheduledExecutorFactoryClass(), cl))
      .withProperties(legacy.getReplicationQueueScheduledExecutorProperties());
      
      builder.shutdown().hookBehavior(ShutdownHookBehavior.valueOf(legacy.getShutdownHookBehavior().name()));
      
      return builder;
   }

}
