package hajiboot.config;

import java.util.Map;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import hajiboot.datasource.ReadOnlyTransactionRoutingDataSource.DataSourceType;
import hajiboot.datasource.ReadOnlyTransactionRoutingDataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
public class DataSourceConfig {
	@Bean
	@ConfigurationProperties("spring.datasource")
	public DataSourceProperties readWriteDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties("spring.datasource.hikari")
	public HikariDataSource readWriteDataSource(@Qualifier("readWriteDataSourceProperties") DataSourceProperties properties) {
		HikariDataSource dataSource = properties.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
		dataSource.setPoolName("read-write-pool");
		return dataSource;
	}

	@Bean
	@ConfigurationProperties("read-only.datasource")
	public DataSourceProperties readOnlyDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties("read-only.datasource.hikari")
	public HikariDataSource readOnlyDataSource(@Qualifier("readOnlyDataSourceProperties") DataSourceProperties properties) {
		HikariDataSource dataSource = properties.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
		dataSource.setReadOnly(true);
		dataSource.setPoolName("read-only-pool");
		return dataSource;
	}

	@Bean
	@Primary
	public DataSource actualDataSource(
			@Qualifier("readWriteDataSource") DataSource readWriteDataSource,
			@Qualifier("readOnlyDataSource") DataSource readOnlyDataSource) {
		ReadOnlyTransactionRoutingDataSource routingDataSource = new ReadOnlyTransactionRoutingDataSource(true);
		routingDataSource.setTargetDataSources(Map.of(
				DataSourceType.READ_ONLY, readOnlyDataSource,
				DataSourceType.READ_WRITE, readWriteDataSource));
		routingDataSource.afterPropertiesSet();
		return new LazyConnectionDataSourceProxy(routingDataSource);
	}
}
