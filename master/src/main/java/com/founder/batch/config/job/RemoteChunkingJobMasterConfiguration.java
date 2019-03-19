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
import com.founder.batch.listener.TestListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingMasterStepBuilderFactory;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
@EnableIntegration
public class RemoteChunkingJobMasterConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private RemoteChunkingMasterStepBuilderFactory masterStepBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private TestListener testListener;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private QueueChannel replies;

	@Autowired
	private DirectChannel requests;

    @Bean
    public FlatFileItemReader<Person> fileReader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }

	@Bean
	public ItemProcessor itemProcessor() {
		return item -> {
			System.out.println("processing item " + item);
			return item;
		};
	}

	@Bean
	public TaskletStep masterStep() {
		return this.masterStepBuilderFactory.get("masterStep")
				.<Integer, Integer>chunk(3)
				.reader(fileReader())
				.outputChannel(requests)
				.inputChannel(replies)
				.processor(itemProcessor())
				.build();
	}

	@Bean
	public JdbcPagingItemReader<Person> jdbcReader(DataSource dataSource) {
		Map<String, Order> sort = new HashMap<>();
		sort.put("first_Name",Order.ASCENDING);
		return new JdbcPagingItemReaderBuilder<Person>().rowMapper(((rs, rowNum) -> {
			String fn = rs.getString("first_Name");
			String ln = rs.getString("last_Name");
			Person p = new Person();
			p.setFirstName(fn);
			p.setLastName(ln);
			return p;
		})).selectClause("first_Name,last_Name").fromClause("people").sortKeys(sort).name("shenmegui").dataSource(dataSource).build();
	}

	@Bean
	public FlatFileItemWriter<Person> fileWriter() {
		return new FlatFileItemWriterBuilder<Person>()
				.name("personItemWriter")
				.resource(new FileSystemResource("D:/sample-data-copy"))
				.delimited()
				.names(new String[]{"firstName", "lastName"})
				.build();
	}

    private Step writerDataToFile() {
        System.out.println("playerLoad");
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(3)
                .reader(jdbcReader(dataSource))
                .writer(fileWriter())
                .build();
    }

	@Bean
	public Job remoteChunkingJob() {
		return this.jobBuilderFactory.get("remoteChunkingJob")
                .incrementer(new RunIdIncrementer())
				.listener(testListener)
                .start(masterStep())
                .next(writerDataToFile())
                .build();
	}

}
