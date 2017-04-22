/opt/maven/bin/mvn clean
/opt/maven/bin/mvn package 
nohup spark-submit --packages com.databricks:spark-csv_2.11:1.1.0 --name "K-means" --class Kmeans --master yarn target/KmeansModel-0.0.1.jar &