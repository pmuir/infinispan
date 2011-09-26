package org.infinispan.configuration.cache;

public class LazyDeserializationConfigurationBuilder extends AbstractConfigurationChildBuilder<LazyDeserializationConfiguration> {

   private boolean enabled;
   
   LazyDeserializationConfigurationBuilder(ConfigurationBuilder builder) {
      super(builder);
   }
   
   public LazyDeserializationConfigurationBuilder enable() {
      this.enabled = true;
      return this;
   }
   
   public LazyDeserializationConfigurationBuilder disable() {
      this.enabled = false;
      return this;
   }

   @Override
   void validate() {
      // TODO Auto-generated method stub
      
   }

   @Override
   LazyDeserializationConfiguration create() {
      return new LazyDeserializationConfiguration(enabled);
   }
   
}
