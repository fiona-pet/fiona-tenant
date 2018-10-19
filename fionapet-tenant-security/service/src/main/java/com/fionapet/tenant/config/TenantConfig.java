package com.fionapet.tenant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@Getter
@Setter
public class TenantConfig {

    @Value("${app.tenant.context.default.schema}")
    private String defaultSchema = "core";
}
