//set the features for the pickup position
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.ml.feature.VectorAssembler
val assembler_pickup_p = new VectorAssembler().setInputCols(Array("pickup_latitude", "pickup_longitude")).setOutputCol("features")
val featureVector_pickup_p = assembler_pickup_p.transform(wtDF).select("features").cache()

//train the kmeans model
import org.apache.spark.ml.clustering.KMeans
val kmeans = new KMeans().setK(150).setFeaturesCol("features").setPredictionCol("prediction").setMaxIter(3)
val model_pickup = kmeans.fit(featureVector_pickup_p)
//save the model
model_pickup.save("/user/yw2504/finalProject/kMeansModel3")

//load the model
import org.apache.spark.ml.clustering.{KMeansModel}
val model_pickup = KMeansModel.load("/user/yw2504/finalProject/kMeansModel3")

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.ml.feature.VectorAssembler
val assembler_pickupCluster = new VectorAssembler().setInputCols(Array("pickup_latitude", "pickup_longitude")).setOutputCol("features")
val featureVector_pickupCluster = assembler_pickupCluster.transform(wtDF).cache()//.drop("pickup_longitude").drop("pickup_latitude").cache()
val clusterDF =  model_pickup.transform(featureVector_pickupCluster).withColumnRenamed("prediction", "pickupCluster").drop("features")

val center = model_pickup.clusterCenters
for( a <- 0 to 149){
    println(center(a)(0)+","+center(a)(1))
}

