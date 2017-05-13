//set the weather index
import org.apache.spark.ml.feature.StringIndexer
val indexer = new StringIndexer().setInputCol("weather").setOutputCol("weatherIndex")
val indexDF = indexer.fit(clusterDF).transform(clusterDF)

indexDF.createOrReplaceTempView("indexDF")
val featuresDF = spark.sql("select year, month, day, hour, weatherIndex, temp, pickupCluster, dayofweek, count(*) as num from indexDF group by year, month, day, hour, weatherIndex, temp, pickupCluster, dayofweek")

