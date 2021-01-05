package com.rconcept.gateway.infrastructure.config.filter;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;

/**
 * @author LZx
 * @since 2020/12/31
 */
public interface RequestBodyValidator extends Ordered {

    /**
     * 验证请求体
     *
     * @param body 请求数据
     * @throws ValidateException 验证不过时抛出的异常
     */
    void commence(DataBuffer body) throws ValidateException;

}
