package com.rconcept.gateway.infrastructure.config.filter.validator;

import com.rconcept.gateway.infrastructure.config.filter.RequestBodyValidator;
import com.rconcept.gateway.infrastructure.config.filter.ValidateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author LZx
 * @since 2020/12/31
 */
@Slf4j
@Component
public class XssValidator implements RequestBodyValidator {

    @Override
    public void commence(DataBuffer body) throws ValidateException {
        log.info("-{}->", this.getClass().getSimpleName());
        String s = body.toString(StandardCharsets.UTF_8);
        System.out.println("-params->\t" + s);
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
