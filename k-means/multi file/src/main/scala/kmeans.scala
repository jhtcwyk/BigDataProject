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
		val taxiDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").load("/user/yw2504/finalProject/yellow/2016/*.csv")
		taxiDF.registerTempTable("trip")
		val DF = sqlContext.sql("select * from trip where VendorID not like '%end%'")
		DF.registerTempTable("DF")
		
		val pickup_position = sqlContext.sql("select pickup_latitude, pickup_longitude from DF")
		
		import org.apache.spark.sql.types.DoubleType		
		val pickup_pos = pickup_position.
		withColumn("pickup_latitudeTmp", pickup_position("pickup_latitude").cast(DoubleType)).drop("pickup_latitude").withColumnRenamed("pickup_latitudeTmp", "pickup_latitude").
		withColumn("pickup_longitudeTmp", pickup_position("pickup_longitude").cast(DoubleType)).drop("pickup_longitude").withColumnRenamed("pickup_longitudeTmp", "pickup_longitude")




		val assembler = new VectorAssembler().setInputCols(Array("pickup_latitude", "pickup_longitude")).setOutputCol("features")
		val featureVector = assembler.transform(pickup_pos).select("features").cache()
		val file = new File("/home/yw2504/kMeansModel/cost2")
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
