package com.fionapet.tenant.multitenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

@Component
public class MultiTenantSchemaProvider implements MultiTenantConnectionProvider {
	
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private DataSource dataSource;
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		return null;
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();		
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		Connection connection = getAnyConnection();

		connection.createStatement().execute("USE " + tenantIdentifier);

//		connection.setSchema(tenantIdentifier);

		return connection;
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		connection.createStatement().execute("USE " + tenantIdentifier);

		releaseAnyConnection(connection); 	 			
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return true;
	}

}
