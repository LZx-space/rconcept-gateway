package com.rconcept.gateway.infrastructure.config.filter.validator;

import com.rconcept.gateway.infrastructure.config.filter.AbstractRequestValidator;
import com.rconcept.gateway.infrastructure.config.filter.ValidateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

/**
 * SQL injection验证器
 *
 * @author LZx
 * @since 2021/1/6
 */
@Slf4j
@Component
public class SqlInjectValidator extends AbstractRequestValidator {

    @Override
    protected void json(String json) throws ValidateException {

    }

    @Override
    public void params(MultiValueMap<String, String> queryParams) throws ValidateException {

    }

    @Override
    public int getOrder() {
        return 1;
    }

}
