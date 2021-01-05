package com.rconcept.gateway.infrastructure.config.exceptionhandler;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.server.RequestPredicate;

/**
 * 重写{@link #acceptsTextHtml()}让{@link #getRoutingFunction(ErrorAttributes)}使用默认可选的HTML
 * 或者JSON格式数据中的JSON格式的返回数据
 *
 * @author LZx
 * @since 2020/12/30
 */
public class JsonWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     *
     * @param errorAttributes    the error attributes
     * @param resources the resources configuration properties
     * @param errorProperties    the error configuration properties
     * @param applicationContext the current application context
     */
    public JsonWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
                                   ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
    }

    @Override
    protected RequestPredicate acceptsTextHtml() {
        return request -> false;
    }

}
