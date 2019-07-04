package com.onurtokat.producer;

import com.google.gson.Gson;
import com.onurtokat.Constants;
import com.onurtokat.model.Order;
import com.onurtokat.serde.JsonSerializer;
import com.onurtokat.stream.TopTenBoughtKafkaStream;
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

public class OrdersDataProducer implements Runnable {

    private static Logger logger = LoggerFactory.getLogger("OrdersDataProducer");
    private Gson gson;
    private Properties config;

    public OrdersDataProducer(Properties config) {
        this.config = config;
    }

    @Override
    public void run() {
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        gson = new Gson();
        KafkaProducer<String, Order> producer = new KafkaProducer<>(config);
        ProducerRecord<String, Order> record;
        Order tempOrder;
        try {
            while (true) {
                while (Files.exists(Paths.get(Constants.ORDER_FILEPATH))) {
                    try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(Constants.ORDER_FILEPATH))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            tempOrder = gson.fromJson(line, Order.class);
                            record = new ProducerRecord<>(Constants.ORDER_TOPIC, null,
                                    tempOrder);
                            RecordMetadata metadata = producer.send(record).get();
                            System.out.println(line + " sent to partition " + metadata.partition() + " with offset " +
                                    metadata.offset());
                        }
                        Files.move(Paths.get(Constants.ORDER_FILEPATH), Paths.get(Constants.DONE_FILEPATH +
                                Constants.ORDER_OUTPUT_FILE_NAME + LocalDateTime.now().format(Constants.formatter) +
                                Constants.ORDER_OUTPUT_FILE_EXTENTION), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                Thread.sleep(60000);
            }
        } catch (IOException e) {
            logger.error("Error occured when " + Constants.ORDER_FILEPATH + " reading", e);
        } catch (ExecutionException e) {
            logger.error("Error occured when metadata tracing", e);
        } catch (InterruptedException e) {
            logger.error("Error occured when " + Runtime.getRuntime().getClass().getName() + " sleeping", e);
        }
    }
}