package com.mapr.logstreams

import org.apache.spark.sql.SparkSession
import org.elasticsearch.hadoop.cfg.ConfigurationOptions

object StreamLogs {

  def main(args: Array[String]):Unit = {

    val stream = args(0)
    val topic = args(1)

    val srcTopic = stream + ":" + topic

//    val spark = SparkSession.builder()
//      .appName("stream-log-dispatcher")
//      .master("local[*]")
//      //      .master("yarn")
//      .getOrCreate()


    val spark = SparkSession.builder()
      .config(ConfigurationOptions.ES_NET_HTTP_AUTH_USER, "root")
      .config(ConfigurationOptions.ES_NET_HTTP_AUTH_PASS, "mapr")
      .config(ConfigurationOptions.ES_NODES, "10.250.51.119")
      .config(ConfigurationOptions.ES_PORT, "9200")
      .master("local[*]")
      .appName("sample-structured-streaming")
      .getOrCreate()

    val brokers = "localhost:9092"

    import spark.implicits._

    val topicDF = spark.readStream
      .format("kafka")
//      .option("group.id","group1")
      .option("kafka.bootstrap.servers", brokers)
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", false)
      .option("subscribe", srcTopic)
//      .option("subscribePattern", stream +"")
      .option("maxOffsetsPerTrigger", 100)
      .load()



    val resultDF = topicDF.select($"topic".cast("String"), $"partition", $"offset",
      $"key".cast("String"),$"value".cast("String"),
      $"timestamp".cast("String") as "ts")

//    resultDF.show(false)

//    val query = resultDF.writeStream.outputMode("append")
//      .format("console")
//      //.trigger(Trigger.ProcessingTime(1.seconds))
//      .option("truncate", false)
//      .option("numRows", 100).start()


//    resultDF.show()

    resultDF.writeStream
      .outputMode("append")
      .format("org.elasticsearch.spark.sql")
//      .format("ec")
      .option("checkpointLocation", "/user/mapr")
      .start("index-name/doc-type").awaitTermination()

    println("***********End**************")


  }

}
