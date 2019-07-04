package com.onurtokat.stream;

import com.google.gson.Gson;
import com.onurtokat.Constants;
import com.onurtokat.model.ItemAccumulator;
import com.onurtokat.model.ProductView;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;

import javax.xml.crypto.Data;

import java.util.Arrays;
import java.util.HashMap;

import static org.apache.spark.sql.functions.*;

public class TopCategoriesByDistinctUser {

    private static Gson gson = new Gson();

    private static String subscribeType = "subscribe";
    private static String productViewTopic = "top-categories-distinct-user-topic";
    private static String viewedProductCategoryTopic = "product-category-topic";


    public static void main(String[] args) {

        Encoder<ProductView> productViewEncoder = Encoders.bean(ProductView.class);

        SparkSession spark = SparkSession
                .builder()
                .appName("TopCategoriesOfProduct").master("local[*]")
                .getOrCreate();
/*
        // Product view
        Dataset<Row> datasetProductView = spark.read()
                .format("kafka")
                .option("kafka.bootstrap.servers", Constants.BOOTSTRAP)
                .option(subscribeType, productViewTopic)
                .load();

        // Product Category
        Dataset<Row> datasetProductCategory = spark.read()
                .format("kafka")
                .option("kafka.bootstrap.servers",Constants.BOOTSTRAP)
                .option(subscribeType, viewedProductCategoryTopic)
                .load();

        //Top ten products of the categories viewed by the most distinct users
        Dataset<Row> top10Product = datasetProductView.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
                .distinct()
                .groupBy("key")
                .count().orderBy(desc("count")).limit(10);

        datasetProductCategory.join(top10Product,
                datasetProductCategory
                        .col("key")
                        .equalTo(top10Product
                                .col("key")), "inner")
                .selectExpr("CAST(value AS STRING)", "count")
                .orderBy(desc("count")).show();

//                .coalesce(1)//all data to one output file
  //              .write()
    //            .csv("/home/vagrant/top10catergory_csv");

*/
        // top ten category of product bought by distinct user
        Dataset<Row> datasetBoughtProduct = spark.read()
                .format("kafka")
                .option("kafka.bootstrap.servers", Constants.BOOTSTRAP)
                .option(subscribeType, Constants.BOUGHT_CATEGORY_BY_USER_TOPIC)
                .load();


        datasetBoughtProduct.printSchema();

/*
        StructField[] itemListSchema = {
                new StructField("productid", DataTypes.StringType, true, Metadata.empty()),
                new StructField("quantity", DataTypes.StringType, true, Metadata.empty())};

        StructField[] schemaFields = {new StructField("itemList",
                new StructType(itemListSchema), true, Metadata.empty())};

        StructType boughtProductSchema = new StructType(schemaFields);
        */


/*
        StructType structType = DataTypes.createStructType(Arrays.asList(
                DataTypes.createStructField("itemList", DataTypes.createStructType(
                        Arrays.asList(
                                DataTypes.createStructField("productid", DataTypes.StringType, true),
                                DataTypes.createStructField("quantity", DataTypes.StringType, true))
                ), true)));
*/

        StructType structType = DataTypes.createStructType(
                Arrays.asList(
                        DataTypes.createStructField("productid", DataTypes.StringType, true),
                        DataTypes.createStructField("quantity", DataTypes.StringType, true))
        );


        //Dataset<Row> datasetBoughtProductFormatted = datasetBoughtProduct.selectExpr("CAST(value AS STRING)");

        //datasetBoughtProductFormatted.printSchema();
/*
        datasetBoughtProductFormatted.select(from_json(datasetBoughtProductFormatted
                .col("value").cast("STRING"), boughtProductSchema).as("result")).
                selectExpr("result.productid").show();
*/
        System.out.println(structType.prettyJson());
        datasetBoughtProduct.select(from_json(col("value").cast("string"), structType)).printSchema();
        datasetBoughtProduct.select(from_json( col("value").cast("string"), structType).as("result"))
                .selectExpr("result.productid")
                .show();

        datasetBoughtProduct.select(col("value").cast("string")).show();

        spark.stop();


    }
}