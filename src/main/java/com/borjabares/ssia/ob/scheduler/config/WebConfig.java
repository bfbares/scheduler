package com.borjabares.ssia.ob.scheduler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@ComponentScan({"com.borjabares.ssia.ob.scheduler.controller"})
@EnableWebMvc
@EnableScheduling
@Import(WebsocketConfig.class)
public class WebConfig extends WebMvcConfigurerAdapter {
    private final Environment environment;

    @Autowired
    public WebConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (environment.acceptsProfiles("dev")) {
            registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
        } else {
            registry.addResourceHandler("/resources/**").addResourceLocations("/resources/").setCachePeriod(30 * 24 * 60 * 60).resourceChain(true).addResolver(new GzipResourceResolver()).addResolver(new PathResourceResolver());
        }
    }
}
