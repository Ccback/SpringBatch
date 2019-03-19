/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.founder.batch.config.job;

import com.founder.batch.bean.Person;
import com.founder.batch.bean.PersonFCRParameter;
import com.founder.batch.fcr.PersonFCRServiceImpl;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;

import javax.sql.DataSource;

/**
 * This configuration class is for the worker side of the remote chunking sample.
 * It uses the {@link RemoteChunkingWorkerBuilder} to configure an
 * {@link IntegrationFlow} in order to:
 * <ul>
 *     <li>receive requests from the master</li>
 *     <li>process chunks with the configured item processor and writer</li>
 *     <li>send replies to the master</li>
 * </ul>
 *
 * @author Mahmoud Ben Hassine
 */
@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
@PropertySource("classpath:application.properties")
public class RemoteChunkingWorkerConfiguration {

	@Value("${broker.url}")
	private String brokerUrl;

	@Autowired
	private RemoteChunkingWorkerBuilder<Person, Person> remoteChunkingWorkerBuilder;

	@Autowired
	private PersonFCRServiceImpl personFCRServiceImpl;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private DirectChannel replies;

	@Autowired
	private DirectChannel requests;

	/*
	 * Configure worker components
	 */
	@Bean
	public ItemProcessor<Person, Person> itemProcessor() {
		return item -> {
			System.out.println("processing item " + item);
			personFCRServiceImpl.callPerson(new PersonFCRParameter());
			return item;
		};
	}

	@Bean
	public ItemWriter<Person> itemWriter() {
		return items -> {
			for (Person item : items) {
				System.out.println("writing item " + item);
			}
		};
	}
    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }
	@Bean
	public IntegrationFlow workerIntegrationFlow() {
		return this.remoteChunkingWorkerBuilder
				.itemProcessor(itemProcessor())
				.itemWriter(writer(dataSource))
				.inputChannel(requests)
				.outputChannel(replies)
				.build();
	}

}
