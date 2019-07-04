package com.onurtokat.stream;

import com.onurtokat.Constants;
import com.onurtokat.model.Item;
import com.onurtokat.model.ItemAccumulator;
import com.onurtokat.model.Order;
import com.onurtokat.serde.JsonDeserializer;
import com.onurtokat.serde.JsonSerializer;
import com.onurtokat.serde.WrapperSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class TopTenBoughtKafkaStream {

    private static final Logger logger = LoggerFactory.getLogger("TopTenBoughtKafkaStream");

    private static Properties config = new Properties();

    public static void main(String[] args) {
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "topten-bought-stream-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP);

        StreamsBuilder builder = new StreamsBuilder();

        //Order topic read stream
        KStream<String, Order> kStreamOrder = builder
                .stream(Constants.ORDER_TOPIC, Consumed.with(Serdes.String(), new TopCategories.OrderSerde()));

        kStreamOrder.selectKey((key, value) -> value.getUserid())
                .flatMapValues(value -> Arrays.asList(value.getLineitems()))
                .groupByKey(Grouped.with(Serdes.String(), new TopCategories.ItemSerde()))
                .aggregate(ItemAccumulator::new,
                        (key, value, itemAgg) -> itemAgg.add(value)
                        ,
                        Materialized.<String, ItemAccumulator, KeyValueStore<Bytes, byte[]>>
                                as("topten-bought-category-by-user-stream5")
                                .withKeySerde(Serdes.String()).withValueSerde(new TopCategories.ItemAggregatorSerde())).mapValues(value -> {
            ItemAccumulator itemAccumulator = new ItemAccumulator();
            Map<String, Integer> tmpMap = new HashMap<>();
            for (Item item : value.getItemList()) {
                if (tmpMap.containsKey(item.getProductid())) {
                    tmpMap.put(item.getProductid(), item.getQuantity() + tmpMap.get(item.getProductid()));
                } else {
                    tmpMap.put(item.getProductid(), item.getQuantity());
                }
            }
            for (Map.Entry<String, Integer> entry : tmpMap.entrySet()) {
                itemAccumulator.add(new Item(entry.getKey(), entry.getValue()));
            }
            return itemAccumulator;
        })
                .toStream().flatMapValues(value -> Arrays.asList(value.getItemListAsArray()))
                .selectKey((key, value) -> value.getProductid())
                .mapValues(value -> Integer.valueOf(value.getQuantity()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Integer()))
                .aggregate(() -> 0,
                        (key, value, aggValue) -> aggValue + value,
                        Materialized.<String, Integer, KeyValueStore<Bytes, byte[]>>as("bought-product-total-stream5")
                                .withKeySerde(Serdes.String()).withValueSerde(Serdes.Integer()))
                .toStream().mapValues(value->value.toString())
                .to(Constants.BOUGHT_CATEGORY_BY_USER_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        Topology topology = builder.build();
        System.out.println(topology.describe());
        KafkaStreams kafkaStreams = new KafkaStreams(topology, config);

        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            kafkaStreams.start();
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Error occurred when countdownlatch await",e);
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

    static public final class OrderSerde extends WrapperSerde<Order> {
        public OrderSerde() {
            super(new JsonSerializer<Order>(), new JsonDeserializer<Order>(Order.class));
        }
    }

    static public final class ItemSerde extends WrapperSerde<Item> {
        public ItemSerde() {
            super(new JsonSerializer<Item>(), new JsonDeserializer<Item>(Item.class));
        }
    }

    static public final class ItemAggregatorSerde extends WrapperSerde<ItemAccumulator> {
        public ItemAggregatorSerde() {
            super(new JsonSerializer<ItemAccumulator>(), new JsonDeserializer<ItemAccumulator>(ItemAccumulator.class));
        }
    }
}
