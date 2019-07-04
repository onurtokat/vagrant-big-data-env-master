package com.onurtokat;

import com.onurtokat.producer.OrdersDataProducer;
import com.onurtokat.producer.ProductCategoryDataProducer;
import com.onurtokat.producer.ProductViewsDataProducer;
import com.onurtokat.stream.TopTenBoughtKafkaStream;
import com.onurtokat.stream.TopTenViewedKafkaStream;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    public static void main(String[] args) {
        //create config
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //prevent duplication
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new OrdersDataProducer(config));
        executorService.execute(new ProductCategoryDataProducer(config));
        executorService.execute(new ProductViewsDataProducer(config));

        //calculate for
        //Top ten products of the categories viewed by the most distinct users
        //TopTenViewedKafkaStream.process();

        //calculate for
        //Top ten products of the categories bought by the most distinct users
        //TopTenBoughtKafkaStream.process();
    }
}