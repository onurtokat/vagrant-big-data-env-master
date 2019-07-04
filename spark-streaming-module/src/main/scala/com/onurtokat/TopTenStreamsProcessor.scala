package com.onurtokat


import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{ArrayType, StringType, StructField, StructType}

object TopTenStreamsProcessor {
  def main(args: Array[String]): Unit = {
    new TopTenStreamsProcessor(Constants.BOOTSTRAP).process()
  }
}

class TopTenStreamsProcessor(brokers: String) {

  def process(): Unit = {

    val spark = SparkSession.builder()
      .appName("kafka-spark-stream")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    //Product view by distinct user
    val viewedProductMainDf = spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", Constants.TOP_CATEGORIES_BY_DISTINCT_USER_TOPIC)
      .load()

    //product category map
    val productCategoryMapMainDf = spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", Constants.PRODUCT_CATEGORY_MAP_TOPIC)
      .load()

    //Product category map. Formatted for columns
    val productCategoryDf = productCategoryMapMainDf
      .select(col("key").cast("string").as("pc_product_id"),
        col("value").cast("string").as("category_name"))

    //Top ten products of the categories viewed by the most distinct users
    val viewedProduct = viewedProductMainDf
        .select(col("key").cast("string").as("viewed_productid"),
          col("value").cast("string"))
      .distinct()
      .groupBy("viewed_productid")
      .count().orderBy(desc("count"))

    val top10viewedProduct = viewedProduct.limit(10)

    //Category join with product id
    println("Top ten products of the categories viewed by the most distinct users")
    productCategoryDf
      .joinWith(top10viewedProduct, productCategoryDf("pc_product_id") === top10viewedProduct("viewed_productid"),
        "inner")
      .select(col ("_1.category_name"),col("_2.count"))
      .orderBy(desc("count"))
      //.coalesce(1)
      //.write
      //.csv("hdfs://node1:50070/tmp")
      .show();

    //bought product
    val boughtProductMainDf = spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", Constants.BOUGHT_CATEGORY_BY_USER_TOPIC)
      .load()

    //top ten bought product. Formatted for columns
    val boughtProductDf = boughtProductMainDf
      .select(col("key").cast("string").as("bp_product_id"),
        col("value").cast("String").as("quantity"))
      .orderBy(desc("value")).limit(10)

    //Join with product id's. Specified columns are used.
    println("Top ten products of the categories bought by the most distinct users")
    productCategoryDf
      .joinWith(boughtProductDf, productCategoryDf("pc_product_id") === boughtProductDf("bp_product_id"),
        "inner")
      .select(col("_2.bp_product_id"), col("_1.category_name"), col("_2.quantity"))
      .orderBy(desc("quantity"))
      //.coalesce(1)
      //.write
      //.csv("hdfs://node1:50070/tmp")
      .show()

    val conversionRateMainDf = viewedProduct
      .joinWith(boughtProductDf, viewedProduct("viewed_productid") === boughtProductDf("bp_product_id"), "inner")
      .select(col("_1.viewed_productid").as("productid"), col("_1.count").as("view_count"),
        col("_2.quantity").as("order_count"))

    println("Conversion rates of each category (order count / view count)")
    conversionRateMainDf.selectExpr("productid", "view_count", "order_count", "order_count/view_count")
      //.coalesce(1)
      //.write
      //.csv("hdfs://node1:50070/tmp")
      .show()

    // Viewed product count
    /*
    val schema: StructType = StructType(
      Array(
        StructField("itemList",
          ArrayType(StructType(Array(StructField("productid", StringType),
            StructField("quantity", StringType)))))))


    inputDf.selectExpr("CAST(value AS STRING) as json")
      .select(from_json($"json", schema).as("data"))
      .select("data.itemList.productid",
        "data.itemList.quantity").show(100)
    */
    spark.streams.awaitAnyTermination()
  }
}
