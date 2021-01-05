package com.rconcept.gateway.infrastructure.config.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * 重写默认的错位信息处理类的{@link #getErrorAttributes(ServerRequest, ErrorAttributeOptions)}用于调整返回的
 * 数据，比如JSON数据对应的MAP
 *
 * @author LZx
 * @since 2020/12/30
 */
@Slf4j
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        modify(errorAttributes);
        return errorAttributes;
    }

    /**
     * 修改错误属性
     *
     * @param currentErrorAttributes 当前错误信息
     */
    private void modify(Map<String, Object> currentErrorAttributes) {
        currentErrorAttributes.remove("message");
    }

}