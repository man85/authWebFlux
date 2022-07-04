package com.reactive.authWebFlux.config;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;

@Configuration
public class WebServerConfig {

    @Bean
    public NioEventLoopGroup nioEventLoopGroup() {
        return new NioEventLoopGroup(1);
    }

    @Bean
    public ReactorResourceFactory reactorResourceFactory(NioEventLoopGroup eventLoopGroup) {
        ReactorResourceFactory f = new ReactorResourceFactory();
        f.setLoopResources(b -> eventLoopGroup);
        f.setUseGlobalResources(false);
        return f;
    }
}
