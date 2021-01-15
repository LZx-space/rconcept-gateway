package com.rconcept.gateway.infrastructure.config.filter.params;

import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

/**
 * @author LZx
 * @since 2020/12/31
 */
public interface ParamsValidator extends Ordered {

    /**
     * 验证查询参数
     *
     * @param queryParams 请求参数
     * @throws ValidateException 验证不过时抛出的异常
     */
    void params(MultiValueMap<String, String> queryParams) throws ValidateException;

    /**
     * 验证请求体，典型为JSON格式数据的非KV对参数的请求
     *
     * @param contentType 请求内容类型
     * @param body        请求数据
     * @throws ValidateException 验证不过时抛出的异常
     */
    void body(MediaType contentType, DataBuffer body) throws ValidateException;

}
