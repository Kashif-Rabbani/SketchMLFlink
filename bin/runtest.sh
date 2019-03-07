#!/bin/bash

minIterations=$1
maxIterations=$2
iterationsInterval=$3
minParallelism=$4
maxParallelism=$5
parallelismInterval=$6
initialDimension=$7
finalDimension=$8
dimensionInterval=$9
trainingFile=${10}
method=${11}
compression=${12}

TRAINING_DATA_PATH=/share/flink/flink-sketchml-batch/data/training/
TESTING_DATA_PATH=/share/flink/flink-sketchml-batch/data/testing/

FLINK_CLUSTER_PATH=/share/flink/flink-1.7.0/bin/flink
JAR_PATH=/share/flink/flink-sketchml-batch/SketchMLFlink/target/sketchmlFlink-1.0-SNAPSHOT.jar

usage ()
{
  echo 'Usage: ./runTest.sh minIterations maxIterations iterationsInterval minParallelism maxParallelism parallelismInterval initialDimension finalDimension dimensionInterval trainingFile method compression'
}

if [ "$#" -ne 12 ]
then
  usage
else
  for (( parallelism=$minParallelism; parallelism<=$maxParallelism; parallelism+=$parallelismInterval ));
  do
    echo "> Running with parallelism $parallelism"
    for (( dim=$initialDimension; dim<=$finalDimension; dim+=$dimensionInterval ));
      do
      echo ">>> Truncating the data to $dim dimensions"
      for (( iterations=$minIterations; iterations<=$maxIterations; iterations+=$iterationsInterval ));
        do
          echo ">>>>>> Running SGD with $iterations iterations"
          echo "$FLINK_CLUSTER_PATH run $JAR_PATH --inputTrain $TRAINING_DATA_PATH$trainingFile --parallelism $parallelism --iterations $iterations --stepSize "0.5" --compressionType $compression --threshold "0.001" --sketchOrFlink $method --outputPathSketch "sketchMLOutput"$parallelism-$iterations-$trainingFile".txt" --outputPathFlink "flinkOriginalSGDOutput"$parallelism-$iterations-$trainingFile".txt" --maxDim $dim"
          $FLINK_CLUSTER_PATH run $JAR_PATH --inputTrain $TRAINING_DATA_PATH$trainingFile --parallelism $parallelism --iterations $iterations --stepSize "0.5" --compressionType $compression --threshold "0.001" --sketchOrFlink $method --outputPathSketch "sketchMLOutput"$parallelism-$iterations-$trainingFile".txt" --outputPathFlink "flinkOriginalSGDOutput"$parallelism-$iterations-$trainingFile".txt" --maxDim $dim
      done
    done
  done
fi
