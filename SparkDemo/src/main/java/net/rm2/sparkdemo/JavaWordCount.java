package net.rm2.sparkdemo;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.desc;

public class JavaWordCount {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JavaWordCount <file>");
            System.exit(1);
        }

        SparkSession spark = SparkSession
                .builder()
                .appName("JavaWordCount")
                .getOrCreate();

        Dataset<Row> df = spark.time(() -> spark.read().text(args[0]));
        Dataset<Row> wordCounts = spark.time(() -> df
                .selectExpr("explode(split(value, ' ')) as word")
                .groupBy("word")
                .count()
                .orderBy(desc("count")));

        wordCounts.show();
        spark.log().info("<!> Chunk Number: {}", df.rdd().getNumPartitions());
        spark.log().info("<!> Chunk Size: {}", spark.conf().get("spark.sql.files.maxPartitionBytes"));
        while (true) {} // to keep WEB GUI alive
    }
}
