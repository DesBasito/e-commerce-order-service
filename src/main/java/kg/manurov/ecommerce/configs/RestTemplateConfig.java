package kg.manurov.ecommerce.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplateConfig restTemplate(){
        return new RestTemplateConfig();
    }
}
