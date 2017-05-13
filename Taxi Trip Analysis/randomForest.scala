//set the features for random forest regressor
val assembler_RF = new VectorAssembler().setInputCols(Array("hour", "temp", "weatherIndex", "dayofweek", "pickupCluster")).setOutputCol("features")
val featureVector_RF = assembler_RF.transform(featuresDF).cache()
featureVector_RF.printSchema

featureVector_RF.createOrReplaceTempView("featureVector_RF")
val featureVector_RF_100 = spark.sql("select features, num from featureVector_RF where num < 100")
val featureVector_RF_300 = spark.sql("select features, num from featureVector_RF where num < 300 and num > 100")
val featureVector_RF_999 = spark.sql("select features, num from featureVector_RF where num < 1000 and num > 300")
val featureVector_RF_1000 = spark.sql("select features, num from featureVector_RF where num > 1000")

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.regression.{RandomForestRegressionModel, RandomForestRegressor}

//to do vector index
val featureIndexer = new VectorIndexer().setInputCol("features").setOutputCol("indexedFeatures").setMaxCategories(100).fit(featureVector_RF)

//set the test and training data
val Array(trainingData, testData) = featureVector_RF.randomSplit(Array(0.7, 0.3))

//set the RandomForestRegressor
val rf = new RandomForestRegressor().setLabelCol("num").setFeaturesCol("indexedFeatures").setMaxBins(32).setNumTrees(500)

//do the pipeline
val pipeline = new Pipeline().setStages(Array(featureIndexer, rf))

//train the model
val model = pipeline.fit(trainingData)

//do the prediction and evaluation
val predictions = model.transform(testData)
predictions.select("prediction", "num", "features").show(5)

val evaluator = new RegressionEvaluator().setLabelCol("num").setPredictionCol("prediction").setMetricName("rmse")

val rmse = evaluator.evaluate(predictions)
println("Root Mean Squared Error (RMSE) on test data = " + rmse)


//out put the rf model
val rfModel = model.stages(1).asInstanceOf[RandomForestRegressionModel]
//println("Learned regression forest model:\n" + rfModel.toDebugString)
//show the importance for different features
rfModel.featureImportances