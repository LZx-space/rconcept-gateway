package com.rconcept.gateway.infrastructure.config.filter.params;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

/**
 * 针对验证请求体数据做抽象的实现
 *
 * @author LZx
 * @since 2021/1/6
 */
public abstract class AbstractParamsValidator implements ParamsValidator {

    @Override
    public final void body(MediaType contentType, DataBuffer body) throws ValidateException {
        if (MediaType.APPLICATION_JSON.equals(contentType)) {
            String json = body.toString(StandardCharsets.UTF_8);
            json(json);
        }
    }

    /**
     * 验证JSON类型的请求体
     *
     * @param json 请求体
     * @throws ValidateException 验证异常
     */
    protected abstract void json(String json) throws ValidateException;

}
