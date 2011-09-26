package org.infinispan.configuration.cache;

public class JMXStatisticsConfigurationBuilder extends AbstractConfigurationChildBuilder<JMXStatisticsConfiguration> {

   private boolean enabled;
   
   JMXStatisticsConfigurationBuilder(ConfigurationBuilder builder) {
      super(builder);
   }
   
   public JMXStatisticsConfigurationBuilder enable() {
      this.enabled = true;
      return this;
   }
   
   public JMXStatisticsConfigurationBuilder disable() {
      this.enabled = false;
      return this;
   }

   @Override
   void validate() {
      // TODO Auto-generated method stub
      
   }

   @Override
   JMXStatisticsConfiguration create() {
      return new JMXStatisticsConfiguration(enabled);
   }
   
}
