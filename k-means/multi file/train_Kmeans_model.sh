/opt/maven/bin/mvn clean
/opt/maven/bin/mvn package
spark-submit --packages com.databricks:spark-csv_2.10:1.5.0  --class kMeans --master yarn target/kMeansModel-0.0.1.jar