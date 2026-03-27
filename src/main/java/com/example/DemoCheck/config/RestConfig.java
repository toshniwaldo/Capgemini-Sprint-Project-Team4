package com.example.DemoCheck.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

@Configuration
public class RestConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config,
            org.springframework.web.servlet.config.annotation.@NonNull CorsRegistry cors) {

        config.getProjectionConfiguration()
                .addProjection(com.example.DemoCheck.projection.CustomerProjection.class);

        //MY CHANGES
        config.getProjectionConfiguration()
                .addProjection(com.example.DemoCheck.projection.OfficeProjection.class);

        //EXPOSE ID to be be included in the response
        config.exposeIdsFor(com.example.DemoCheck.entity.Office.class);
    }
}
