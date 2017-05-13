#!/bin/bash
# 
# Decription: 
#  A Shell script used to download the data from Internet.
# Author:
#  Yuankai Wang
# Date:
#  2017-04-29 00:16:59
#

BASE_URL="https://s3.amazonaws.com/nyc-tlc/trip+data/yellow_tripdata_2013-0"

# declare STIRNG variable
STRING="Beginning Data download..."

#print var on screen 
echo $STRING

sleep 1
echo "...."

CSV=".csv"

for (( i=1;i<10;i++)); do
IMG_URL=${BASE_URL}${i}${CSV}
echo "final url=" ${IMG_URL}

wget ${IMG_URL}
sleep 1
done

BASE_URL="https://s3.amazonaws.com/nyc-tlc/trip+data/yellow_tripdata_2013-"

for (( i=10;i<13;i++)); do
IMG_URL=${BASE_URL}${i}${CSV}
echo "final url=" ${IMG_URL}

wget ${IMG_URL}
sleep 1
done

hadoop fs -mkdir /user/yw2504/finalProject/yellow/2013
hadoop fs -put $HOME/*.csv /user/yw2504/finalProject/yellow/2013
rm *.csv