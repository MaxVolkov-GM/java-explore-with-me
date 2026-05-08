package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {
	@Bean
	public StatsClient statsClient(@Value("${stats-server.url}") String statsServerUrl) {
		return new StatsClient(statsServerUrl);
	}
}