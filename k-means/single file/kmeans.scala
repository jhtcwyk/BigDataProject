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


object Kmeans {

  def main(args : Array[String]) {
	  val sc = new SparkContext(new SparkConf().setAppName("KmeansModel"))
	  val sqlContext = new org.apache.spark.sql.SQLContext(sc)
	  
	  import sqlContext.implicits._
  
	  val yellowSchema = StructType(Array(
		  StructField("VendorID", IntegerType, true),
		  StructField("tpep_pickup_datetime", StringType, true),
		  StructField("tpep_dropoff_datetime", StringType, true),
		  StructField("Passenger_count", IntegerType, true),
		  StructField("Trip_distance", DoubleType, true),
		  StructField("Pickup_longitude", DoubleType, true),
		  StructField("Pickup_latitude", DoubleType, true),
		  StructField("RateCodeID", IntegerType, true),
		  StructField("Store_and_fwd_flag", StringType, true),
		  StructField("Dropoff_longitude", DoubleType, true),
		  StructField("Dropoff_latitude", DoubleType, true),
		  StructField("Payment_type", IntegerType, true),
		  StructField("Fare_amount", DoubleType, true),
		  StructField("Extra", DoubleType, true),
		  StructField("MTA_tax", DoubleType, true),
		  StructField("Improvement_surcharge", DoubleType, true),
		  StructField("Tip_amount", DoubleType, true),
		  StructField("Tolls_amount", DoubleType, true),
		  StructField("Total_amount", DoubleType, true)
		))
		
		val taxiDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "false").schema(yellowSchema).load("/user/yw2504/finalProject/yellow/2016/yellow_tripdata_2016-04.csv")
		
		taxiDF.registerTempTable("trip")
		val DF = sqlContext.sql("Select tpep_pickup_datetime, Pickup_latitude, Pickup_longitude From trip")
		val assembler = new VectorAssembler().setInputCols(Array("Pickup_latitude", "Pickup_longitude")).setOutputCol("features")
		val featureVector = assembler.transform(DF).select("features").cache()
		val file = new File("/home/yw2504/project/cost")
		val bw = new BufferedWriter(new FileWriter(file))
		
		println("####### begin to train Kmeans model ########")
		
		var a = 0;
		for( a <- 3 to 100){
			val kmeans = new KMeans().setK(a).setFeaturesCol("features").setPredictionCol("prediction").setMaxIter(30)
			val model = kmeans.fit(featureVector)
			val ssd = model.computeCost(featureVector)
			bw.write(a + "," + ssd)
			bw.newLine()
        }
		bw.close()
		
		return

  }
}
