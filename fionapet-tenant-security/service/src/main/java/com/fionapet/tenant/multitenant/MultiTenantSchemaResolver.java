package com.fionapet.tenant.multitenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MultiTenantSchemaResolver implements CurrentTenantIdentifierResolver {

	@Autowired
	TenantContextHolder tenantContextHolder;

	@Override
	public String resolveCurrentTenantIdentifier() {
		return tenantContextHolder.getCurrentSchema();
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}
