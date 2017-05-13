import java.io._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.ml.feature.VectorAssembler


// Import Row.
import org.apache.spark.sql.Row;


object kMeans {

	def main(args : Array[String]) {
		val sc = new SparkContext(new SparkConf().setAppName("kMeansModel"))
		val sqlContext = new org.apache.spark.sql.SQLContext(sc)
	  
		println("------------ load data ftom hdfs ---------------")
		val taxiDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("model", "DROPMALFORMED").load("/user/yw2504/finalProject/yellow/2016/*.csv")
		taxiDF.registerTempTable("trip")
		val DF = sqlContext.sql("select * from trip where VendorID not like '%end%'")
		DF.registerTempTable("DF")
		
		val position = sqlContext.sql("select pickup_latitude, pickup_longitude, dropoff_latitude, dropoff_longitude from DF")
		
		
		import org.apache.spark.sql.types.DoubleType		
		val pos_double = position.
		withColumn("pickup_latitudeTmp", position("pickup_latitude").cast(DoubleType)).drop("pickup_latitude").withColumnRenamed("pickup_latitudeTmp", "pickup_latitude").
		withColumn("pickup_longitudeTmp", position("pickup_longitude").cast(DoubleType)).drop("pickup_longitude").withColumnRenamed("pickup_longitudeTmp", "pickup_longitude").
		withColumn("dropoff_latitudeTmp", position("dropoff_latitude").cast(DoubleType)).drop("dropoff_latitude").withColumnRenamed("dropoff_latitudeTmp", "dropoff_latitude").
		withColumn("dropoff_longitudeTmp", position("dropoff_longitude").cast(DoubleType)).drop("dropoff_longitude").withColumnRenamed("dropoff_longitudeTmp", "dropoff_longitude")
				
		val pos_filter = pos_double.filter(pos_double("pickup_latitude") < 40.915568 && pos_double("pickup_latitude") > 40.495992).
		filter(pos_double("pickup_longitude") < -73.699215 && pos_double("pickup_longitude") > -74.257159).
		filter(pos_double("dropoff_latitude") < 40.915568 && pos_double("dropoff_latitude") > 40.495992).
		filter(pos_double("dropoff_longitude") < -73.699215 && pos_double("dropoff_longitude") > -74.257159)

		val assembler_pickup_p = new VectorAssembler().setInputCols(Array("pickup_latitude", "pickup_longitude")).setOutputCol("features")
		val featureVector_pickup_p = assembler_pickup_p.transform(pos_filter).select("features").cache()
		val file_pickup = new File("/home/yw2504/kMeansModel/outputResults/SSE_For_K_num_pickup2")
		val bw_pickup = new BufferedWriter(new FileWriter(file_pickup))
		
		val assembler_dropoff_p = new VectorAssembler().setInputCols(Array("dropoff_latitude", "dropoff_longitude")).setOutputCol("features")
		val featureVector_dropoff_p = assembler_dropoff_p.transform(pos_filter).select("features").cache()
		val file_dropoff = new File("/home/yw2504/kMeansModel/outputResults/SSE_For_K_num_dropoff2")
		val bw_dropoff = new BufferedWriter(new FileWriter(file_dropoff))
		
		println("####### begin to train Kmeans model ########")
		
		var a = 0;
		for( a <- 3 to 300){
			val kmeans = new KMeans().setK(a).setFeaturesCol("features").setPredictionCol("prediction").setMaxIter(10)
			val model_pickup = kmeans.fit(featureVector_pickup_p)
			val model_dropoff = kmeans.fit(featureVector_dropoff_p)
			val ssd_pickup = model_pickup.computeCost(featureVector_pickup_p)
			val ssd_dropoff = model_dropoff.computeCost(featureVector_dropoff_p)
			bw_pickup.write(a + "," + ssd_pickup)
			bw_pickup.newLine()
			bw_dropoff.write(a + "," + ssd_dropoff)
			bw_dropoff.newLine()
        }
		bw_pickup.close()
		bw_dropoff.close()
		return

  }
}
