package org.infinispan.configuration.cache;

import java.util.LinkedList;
import java.util.List;

public class CustomInterceptorsConfigurationBuilder extends AbstractConfigurationChildBuilder<CustomInterceptorsConfiguration> {

   private List<InterceptorConfigurationBuilder> interceptors = new LinkedList<InterceptorConfigurationBuilder>();
   
   CustomInterceptorsConfigurationBuilder(ConfigurationBuilder builder) {
      super(builder);
   }
   
   public InterceptorConfigurationBuilder addInterceptor() {
      InterceptorConfigurationBuilder builder = new InterceptorConfigurationBuilder(this);
      this.interceptors.add(builder);
      return builder;
   }

   @Override
   void validate() {
      // TODO Auto-generated method stub
      
   }

   @Override
   CustomInterceptorsConfiguration create() {
      List<InterceptorConfiguration> interceptorConfigurations = new LinkedList<InterceptorConfiguration>();
      for (InterceptorConfigurationBuilder builder : interceptors)
         interceptorConfigurations.add(builder.create());
      return new CustomInterceptorsConfiguration(interceptorConfigurations);
   }

}
