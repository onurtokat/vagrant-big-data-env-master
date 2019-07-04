package com.onurtokat.producer;

import com.google.gson.Gson;
import com.onurtokat.Constants;
import com.onurtokat.model.ProductView;
import com.onurtokat.serde.JsonDeserializer;
import com.onurtokat.serde.JsonSerializer;
import com.onurtokat.serde.WrapperSerde;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
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

public class ProductViewsDataProducer implements Runnable {

    private static Logger logger = LoggerFactory.getLogger("ProductViewsDataProducer");
    private Properties config;

    public ProductViewsDataProducer(Properties config) {
        this.config = config;
    }

    @Override
    public void run() {
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        KafkaProducer<String, ProductView> producer = new KafkaProducer<>(config);
        ProducerRecord<String, ProductView> record;
        try {
            while (true) {
                while (Files.exists(Paths.get(Constants.PRODUCT_VIEW_FILEPATH))) {
                    try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(Constants.PRODUCT_VIEW_FILEPATH))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            record = new ProducerRecord<>(Constants.PRODUCT_VIEW_TOPIC, null,
                                    new Gson().fromJson(line, ProductView.class));
                            RecordMetadata metadata = producer.send(record).get();
                            System.out.println(line + " sent to partition " + metadata.partition() + " with offset " +
                                    metadata.offset());
                        }
                        Files.move(Paths.get(Constants.PRODUCT_VIEW_FILEPATH),
                                Paths.get(Constants.DONE_FILEPATH +
                                        Constants.PV_OUTPUT_FILE_NAME + LocalDateTime.now().format(Constants.formatter) +
                                        Constants.PV_OUTPUT_FILE_EXTENTION), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("Error occured when " + Constants.PRODUCT_VIEW_FILEPATH + " reading", e);
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

    static public final class ProductViewSerde extends WrapperSerde<ProductView> {
        public ProductViewSerde() {
            super(new JsonSerializer<ProductView>(), new JsonDeserializer<ProductView>(ProductView.class));
        }
    }
}