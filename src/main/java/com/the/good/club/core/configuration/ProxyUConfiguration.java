package com.the.good.club.core.configuration;


import com.the.good.club.dataU.sdk.ProxyUClientConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ProxyUClientConfiguration.class)
public class ProxyUConfiguration {

//    @Bean
//    ProxyUClient proxyUClient(
//            ManagedChannel channel,
//            ProxyUClientStorage proxyUClientStorage,
//            ProxyUClientCallbacks proxyUClientCallbacks) {
//        return new ProxyUClient(channel, proxyUClientStorage, proxyUClientCallbacks);
//    }
}
