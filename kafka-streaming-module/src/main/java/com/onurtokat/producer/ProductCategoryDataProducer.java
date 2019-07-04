package com.onurtokat.producer;

import com.onurtokat.Constants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProductCategoryDataProducer implements Runnable {

    private static Logger logger = LoggerFactory.getLogger("ProductCategoryDataProducer");
    private Properties config;

    public ProductCategoryDataProducer(Properties config) {
        this.config = config;
    }

    @Override
    public void run() {
        KafkaProducer<String, String> producer = new KafkaProducer<>(config);
        ProducerRecord<String, String> record;
        int counter = 0;
        try {
            while (true) {
                while (Files.exists(Paths.get(Constants.PRODUCTED_CATEGORY_MAP_FILEPATH))) {
                    try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(Constants.PRODUCTED_CATEGORY_MAP_FILEPATH))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            if (counter > 0) { //header prevent
                                String[] fields = line.split(",");
                                record = new ProducerRecord<>(Constants.PRODUCT_CATEGORY_MAP_TOPIC, fields[0],
                                        fields[1]);
                                RecordMetadata metadata = producer.send(record).get();
                                System.out.println(line + " sent to partition " + metadata.partition() + " with offset " +
                                        metadata.offset());
                            }
                            counter++;
                        }
                        Files.move(Paths.get(Constants.PRODUCTED_CATEGORY_MAP_FILEPATH),
                                Paths.get(Constants.DONE_FILEPATH +
                                        Constants.PC_OUTPUT_FILE_NAME + LocalDateTime.now().format(Constants.formatter) +
                                        Constants.PC_OUTPUT_FILE_EXTENTION), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("Error occured when " + Constants.PRODUCTED_CATEGORY_MAP_FILEPATH + " reading", e);
                    } catch (ExecutionException e) {
                        logger.error("Error occured when metadata tracing", e);
                    }
                }
                Thread.sleep(60000);
            }
        } catch (InterruptedException e) {
            logger.error("Error occured when " + Runtime.getRuntime().getClass().getName() + " sleeping", e);
        }
    }
}