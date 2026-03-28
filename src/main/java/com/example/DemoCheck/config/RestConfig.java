package com.example.DemoCheck.config;

import com.example.DemoCheck.handler.ProductEventHandler;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import com.example.DemoCheck.validator.ProductValidator;

@Configuration
public class RestConfig implements RepositoryRestConfigurer {

        @Bean
        public ProductEventHandler productEventHandler() {
                return new ProductEventHandler();
        }

        @Autowired
        private ProductValidator productValidator;

        @Override
        public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
                v.addValidator("beforeCreate", productValidator);
                v.addValidator("beforeSave", productValidator);
        }

        @Override
        public void configureRepositoryRestConfiguration(
                        RepositoryRestConfiguration config,
                        org.springframework.web.servlet.config.annotation.@NonNull CorsRegistry cors) {

                config.getProjectionConfiguration()
                                .addProjection(com.example.DemoCheck.projection.CustomerProjection.class);

                // MY CHANGES
                config.getProjectionConfiguration()
                                .addProjection(com.example.DemoCheck.projection.OfficeProjection.class);

        //EXPOSE ID to be be included in the response
        config.exposeIdsFor(com.example.DemoCheck.entity.Office.class);

        config.getProjectionConfiguration()
                .addProjection(com.example.DemoCheck.projection.EmployeeListProjection.class);

        config.exposeIdsFor(com.example.DemoCheck.entity.Employee.class);
    }
}
