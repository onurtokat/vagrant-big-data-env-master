package com.onurtokat.stream;

import com.onurtokat.Constants;
import com.onurtokat.model.ProductView;
import com.onurtokat.serde.JsonDeserializer;
import com.onurtokat.serde.JsonSerializer;
import com.onurtokat.serde.WrapperSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class TopTenViewedKafkaStream {

    private static final Logger logger = LoggerFactory.getLogger("TopTenViewedKafkaStream");

    private static Properties config = new Properties();

    public static void main(String[] args) {
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "topten-bought-stream-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, ProductView> kStreamProductView = builder.stream(Constants.PRODUCT_VIEW_TOPIC, Consumed
                .with(Serdes.String(), new ProductViewSerde()));
        kStreamProductView.selectKey((key, value) -> value.getProperties().getProductid())
                .mapValues(value -> value.getUserid())
                .to(Constants.TOP_CATEGORIES_BY_DISTINCT_USER_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        Topology topology = builder.build();
        System.out.println(topology.describe());
        KafkaStreams kafkaStreams = new KafkaStreams(topology, config);

        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            kafkaStreams.start();
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Error occurred when countdownlatch awaiting", e);
            e.printStackTrace();
        }

        //gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
            @Override
            public void run() {
                kafkaStreams.close();
                countDownLatch.countDown();
            }
        });
    }

    static public final class ProductViewSerde extends WrapperSerde<ProductView> {
        public ProductViewSerde() {
            super(new JsonSerializer<ProductView>(), new JsonDeserializer<ProductView>(ProductView.class));
        }
    }
}
