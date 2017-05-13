    import org.apache.spark.sql.types.IntegerType
    import org.apache.spark.sql.types.DoubleType
    import org.apache.spark.sql.DataFrame
    import spark.implicits._
    def castTripType( df:DataFrame) :DataFrame = {
        val pick_time = unix_timestamp($"pickup_datetime","yyyy-MM-dd HH:mm:ss").cast("timestamp")
        val dropoff_time = unix_timestamp($"dropoff_datetime","yyyy-MM-dd HH:mm:ss").cast("timestamp")
        
        /*
       if(year == 2016 || year == 2015) {
            val pick_time = unix_timestamp($"tpep_pickup_datetime","yyyy-MM-dd HH:mm:ss").cast("timestamp")
            val dropoff_time = unix_timestamp($"tpep_dropoff_datetime","yyyy-MM-dd HH:mm:ss").cast("timestamp")
       } */

       
        val pos_time = df.
            withColumn("pickup_time", pick_time).drop("pickup_datetime").
            withColumn("dropoff_time", dropoff_time).drop("dropoff_datetime").
            withColumn("pickup_latitudeTmp", df("pickup_latitude").cast(DoubleType)).drop("pickup_latitude").withColumnRenamed("pickup_latitudeTmp", "pickup_latitude").
		    withColumn("pickup_longitudeTmp", df("pickup_longitude").cast(DoubleType)).drop("pickup_longitude").withColumnRenamed("pickup_longitudeTmp", "pickup_longitude").
		    withColumn("dropoff_latitudeTmp", df("dropoff_latitude").cast(DoubleType)).drop("dropoff_latitude").withColumnRenamed("dropoff_latitudeTmp", "dropoff_latitude").
		    withColumn("dropoff_longitudeTmp", df("dropoff_longitude").cast(DoubleType)).drop("dropoff_longitude").withColumnRenamed("dropoff_longitudeTmp", "dropoff_longitude")
		return pos_time
    }
    
    def filterPosition( pos_time:DataFrame ) :DataFrame = {
        val tripData = pos_time.
            filter(pos_time("pickup_latitude") < 40.915568 && pos_time("pickup_latitude") > 40.495992).
		    filter(pos_time("pickup_longitude") < -73.699215 && pos_time("pickup_longitude") > -74.257159).
		    filter(pos_time("dropoff_latitude") < 40.915568 && pos_time("dropoff_latitude") > 40.495992).
		    filter(pos_time("dropoff_longitude") < -73.699215 && pos_time("dropoff_longitude") > -74.257159)
		return tripData
    }
	
	//read 2015 taxi data
	val taxi2015DF = spark.read.format("csv").option("header", "true").option("model", "DROPMALFORMED").load("/user/yw2504/finalProject/yellow/2015/*.csv")
	taxi2015DF.createOrReplaceTempView("trip")
	val DF = spark.sql("select * from trip where VendorID not like '%end%'")
	DF.createOrReplaceTempView("DF")
	val df = spark.sql("select pickup_latitude, pickup_longitude, dropoff_latitude, dropoff_longitude, tpep_pickup_datetime as pickup_datetime, tpep_dropoff_datetime as dropoff_datetime from DF")
	val pos_time = castTripType(df)
	val tripData2015 = filterPosition(pos_time)
	tripData2015.createOrReplaceTempView("tripData2015")
	
	//read 2014 taxi data
	val taxi2014DF = spark.read.format("csv").option("header", "true").option("model", "DROPMALFORMED").load("/user/yw2504/finalProject/yellow/2014/*.csv")
	taxi2014DF.createOrReplaceTempView("trip")
	val DF = spark.sql("select * from trip where vendor_id not like '%end%'")
	DF.createOrReplaceTempView("DF")
	val df = DF.select(" pickup_datetime", " pickup_latitude", " pickup_longitude", " dropoff_datetime", " dropoff_latitude", " dropoff_longitude")
	val newNames = Seq("pickup_datetime", "pickup_latitude", "pickup_longitude", "dropoff_datetime", "dropoff_latitude", "dropoff_longitude")
	val dfRenamed = df.toDF(newNames: _*)
	val pos_time = castTripType(dfRenamed)
	val tripData2014 = filterPosition(pos_time)
	tripData2014.createOrReplaceTempView("tripData2014")
	
	//read 2013 taxi data
	val taxi2013DF = spark.read.format("csv").option("header", "true").option("model", "DROPMALFORMED").load("/user/yw2504/finalProject/yellow/2013/*.csv")
	taxi2013DF.createOrReplaceTempView("trip")
	val DF = spark.sql("select * from trip where vendor_id not like '%end%'")
	DF.createOrReplaceTempView("DF")
	val df = DF.select("pickup_datetime", "pickup_latitude", "pickup_longitude", "dropoff_datetime", "dropoff_latitude", "dropoff_longitude")
	val pos_time = castTripType(df)
	val tripData2013 = filterPosition(pos_time)
	tripData2013.createOrReplaceTempView("tripData2013")
	
	//load an filter the weather data, transform the data type
	val weather = spark.read.format("csv").option("header", "true").option("model", "DROPMALFORMED").load("/user/yw2504/finalProject/env2.csv")
	weather.createOrReplaceTempView("weather")
	val date_weather_temp_dow = spark.sql("select DATE, WEATHER as weather, TEMPC, dayofweek from weather").filter("TEMPC is not null")
	val time = unix_timestamp($"DATE","yyyy-MM-dd HH").cast("timestamp")
	val time_weather_temp_dow = date_weather_temp_dow.withColumn("time", time).drop("DATE")
	val weatherData =   time_weather_temp_dow.withColumn("tempTmp", time_weather_temp_dow("TEMPC").cast(DoubleType)).drop("TEMPC").withColumnRenamed("tempTmp", "temp").
						withColumn("dowTmp", time_weather_temp_dow("dayofweek").cast(IntegerType)).drop("dayofweek").withColumnRenamed("dowTmp", "dayofweek").
						filter("temp is not null")
	weatherData.createOrReplaceTempView("weatherData")
	
	def joinTripWeather( tripData:DataFrame, weatherData:DataFrame) :DataFrame = {
		tripData.createOrReplaceTempView("tripData")
		weatherData.createOrReplaceTempView("weatherData")
		val DF = spark.sql("select year(t.pickup_time) as year, month(t.pickup_time) as month,  day(t.pickup_time) as day, hour(t.pickup_time) as hour, w.weather, t.pickup_latitude, t.pickup_longitude, w.temp, w.dayofweek from weatherData as w, tripData as t where year(w.time) = year(t.pickup_time) and month(w.time) = month(t.pickup_time) and day(w.time) = day(t.pickup_time) and hour(w.time) = hour(t.pickup_time)")
		return DF
    }
	
	val wtDF2015 = joinTripWeather(tripData2015, weatherData)
	val wtDF2014 = joinTripWeather(tripData2014, weatherData)
	val wtDF2013 = joinTripWeather(tripData2013, weatherData)
	wtDF2015.createOrReplaceTempView("wtDF2015")
	wtDF2014.createOrReplaceTempView("wtDF2014")
	wtDF2013.createOrReplaceTempView("wtDF2013")
	
	val wtDF = wtDF2015.union(wtDF2014).union(wtDF2013)
	wtDF.createOrReplaceTempView("wtDF")