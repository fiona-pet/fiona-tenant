package com.fionapet.tenant.multitenant;

import com.fionapet.tenant.config.TenantConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TenantContextHolder {

	@Autowired
	TenantConfig tenantConfig;

	private static final Logger log = LoggerFactory.getLogger(TenantContextHolder.class); 
	private static ThreadLocal<String> currentSchema = new ThreadLocal<>();

	public String getCurrentSchema() {
		String schemaName = currentSchema.get();
		if (schemaName == null) {
			return (tenantConfig.getDefaultSchema());
		}
		return schemaName;
	}

	public void setCurrentSchema(String schema) {
		currentSchema.set(schema);
		log.debug("Current schema = {}", schema);
	}

	public void clear() {
		setCurrentSchema(tenantConfig.getDefaultSchema());
	}
	
}
