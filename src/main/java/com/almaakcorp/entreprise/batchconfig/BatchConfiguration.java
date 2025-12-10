package com.almaakcorp.entreprise.batchconfig;

import com.almaakcorp.entreprise.models.Products;
import com.almaakcorp.entreprise.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {


    private final ProductRepository productRepository;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    public FlatFileItemReader<Products> createItemReader(String filePath) {
        validateFilePath(filePath);

        FlatFileItemReader<Products> reader = new FlatFileItemReaderBuilder<Products>()
                .name("productsItemReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .delimiter(",")
                .names("productName", "productDescription", "productPartNumber", "productManufacturer", "productNormalPrice", "productSellingPrice")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Products>() {{
                    setTargetType(Products.class);
                }})
                .build();

        return reader;
    }

    private void validateFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("File is not readable: " + filePath);
        }

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path is not a regular file: " + filePath);
        }

        String fileName = path.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".csv")) {
            throw new IllegalArgumentException("File is not a CSV file: " + filePath);
        }
    }

    @Bean
    public ProductsProcessor processor() {
        return new ProductsProcessor();
    }

    @Bean
    public RepositoryItemWriter<Products> itemWriter() {
        RepositoryItemWriter<Products> writer = new RepositoryItemWriter<>();
        writer.setRepository(productRepository);
        writer.setMethodName("save");
        return writer;
    }

    public Step createImportStep(String filePath) {
        return new StepBuilder("productsImportStep", jobRepository)
                .<Products, Products>chunk(10, transactionManager)
                .reader(createItemReader(filePath))
                .processor(processor())
                .writer(itemWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build(); // No taskExecutor
    }

    public Job createImportJob(String filePath) {
        return new JobBuilder("importProductsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(createImportStep(filePath))
                .build();
    }
}