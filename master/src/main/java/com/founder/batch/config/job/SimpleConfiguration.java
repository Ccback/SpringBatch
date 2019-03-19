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
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration class is for the master side of the remote chunking sample.
 * The master step reads numbers from 1 to 6 and sends 2 chunks {1, 2, 3} and
 * {4, 5, 6} to workers for processing and writing.
 *
 * @author Mahmoud Ben Hassine
 */
@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
public class SimpleConfiguration {
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private TestListener testListener;
	@Bean
	public Job footballJob() {
		return this.jobBuilderFactory.get("footballJob")
				.listener(testListener)
				.start(playerLoad())
				.next(gameLoad())
				.next(playerSummarization())
				.build();
	}

	private Step playerSummarization() {
		System.out.println("playerSummarization");
		return stepBuilderFactory.get("step3")
				.<Person, Person> chunk(10)
				.reader(() -> {System.out.println("playerSummarization---------reader!------");return null;})
				.writer(a -> System.out.println("playerSummarization---------writer!------"))
				.build();
	}

	private Step gameLoad() {
		System.out.println("gameLoad");
		return stepBuilderFactory.get("step2")
				.<Person, Person> chunk(10)
				.reader(() -> {System.out.println("gameLoad------------reader!------");return null;})
				.writer(a -> System.out.println("gameLoad------------writer!------"))
				.build();
	}

	private Step playerLoad() {
		System.out.println("playerLoad");
		return stepBuilderFactory.get("step1")
				.<Person, Person> chunk(10)
				.reader(() -> {System.out.println("playerLoad---------------reader!------");return null;})
				.writer(a -> System.out.println("playerLoad---------------writer!------"))
				.build();
	}

}
