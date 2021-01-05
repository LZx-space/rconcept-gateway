package com.rconcept.gateway.infrastructure.config.exceptionhandler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.stream.Collectors;

/**
 * 网关异常处理自动配置
 *
 * @author LZx
 * @since 2020/12/30
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFluxConfigurer.class)
@AutoConfigureBefore(ErrorWebFluxAutoConfiguration.class)
@EnableConfigurationProperties({ServerProperties.class})
public class WebExceptionAutoConfiguration {

    private final ServerProperties serverProperties;

    public WebExceptionAutoConfiguration(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    /**
     * order(-1)查看{@link ErrorWebFluxAutoConfiguration}
     *
     * @param errorAttributes       the error attributes
     * @param resources             the resources configuration properties
     * @param viewResolvers         视图解析器： 视图名 -> 页面字符
     * @param serverCodecConfigurer codecs config for HTTP message reader and writer
     * @param applicationContext    the current application context
     * @return Web请求的异常处理器
     */
    @Bean
    @Order(-1)
    public ErrorWebExceptionHandler errorWebExceptionHandler(
            ErrorAttributes errorAttributes, WebProperties.Resources resources,
            ObjectProvider<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer,
            ApplicationContext applicationContext) {
        JsonWebExceptionHandler exceptionHandler = new JsonWebExceptionHandler(errorAttributes,
                resources, this.serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    /**
     * @return 自定义的异常属性提供对象
     */
    @Bean
    public ErrorAttributes errorAttributes() {
        return new CustomErrorAttributes();
    }

}
