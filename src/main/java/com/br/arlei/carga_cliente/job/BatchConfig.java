package com.br.arlei.carga_cliente.job;

import com.br.arlei.carga_cliente.data_source.DataSourceQualifiers;
import com.br.arlei.carga_cliente.model.Client;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;


import javax.sql.DataSource;


@Configuration
public class BatchConfig {


    private final JobRepository jobRepository;


    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public JdbcCursorItemReader<Client> reader(@Qualifier(DataSourceQualifiers.POSTGRES_DATA_SOURCE)
                                               DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Client>()
                .dataSource(dataSource)
                .name("clientReader")
                .sql("SELECT id, nome, email, cpf FROM clientes")
                .rowMapper(new BeanPropertyRowMapper<>(Client.class))
                .build();
    }

    @Bean
    public ItemProcessor<Client, Client> processor() {
        return client -> {
            RestTemplate restTemplate = new RestTemplate();
            String cpfApiUrl = "https://api.invertexto.com/v1/faker?token=18690|l3KlyXA6An7ahjjEG4XFh1gwzXfYXGsK&fields=" + client.getNome() + ",cpf&locale=pt_BR";
            String cpf = restTemplate.getForObject(cpfApiUrl, String.class);
            System.out.println(cpf.split(":")[1].replace(".", "")
                    .replace("}","")
                            .replace("\"", "")
                            .replace("-","")
                    .trim());

            client.setCpf(cpf.split(":")[1].replace(".", "")
                    .replace("}","")
                    .replace("\"", "")
                    .replace("-","")
                    .trim());
            return client;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Client> writer(
            @Qualifier(DataSourceQualifiers.MYSQL_DATA_SOURCE) DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Client>()
                .dataSource(dataSource)
                .sql("INSERT INTO clientes_com_cpf (id, nome, email, cpf) VALUES (:id, :nome, :email, :cpf)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step step(ItemReader<Client> reader,
                     ItemProcessor<Client, Client> processor,
                     ItemWriter<Client> writer) {
        return new StepBuilder("step",jobRepository)
                .<Client, Client>chunk(100,transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job job(Step step) {
        return new JobBuilder("job",jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }

}