#!/bin/bash

minIterations=$1
maxIterations=$2
minParallelism=$3
maxParallelism=$4
trainingFile=$5
method=$6
compression=$7

TRAINING_DATA_PATH=/share/flink/flink-sketchml-batch/data/training/
TESTING_DATA_PATH=/share/flink/flink-sketchml-batch/data/testing/

FLINK_CLUSTER_PATH=/share/flink/flink-1.7.0/bin/flink
JAR_PATH=/share/flink/flink-sketchml-batch/SketchMLFlink/target/sketchmlFlink-1.0-SNAPSHOT.jar

usage ()
{
  echo 'Usage: ./runTest.sh minIterations maxIterations minParallelism maxParallelism trainingFile method compression'
}

if [ "$#" -ne 7 ]
then
  usage
else
  for (( iterations=$minIterations; iterations<=$maxIterations; iterations+=100 ));
  do
    echo ">> Iterations $iterations"
    for (( parallelism=$minParallelism; parallelism<=$maxParallelism; parallelism+=8 ));
      do
        echo "> Parallelism $parallelism"
        echo ">> Training file  $trainingFile"
       echo "$FLINK_CLUSTER_PATH run $JAR_PATH --inputTrain $TRAINING_DATA_PATH$trainingFile --parallelism $parallelism --iterations $iterations --stepSize "0.5" --compressionType $compression --threshold "0.001" --sketchOrFlink $method --outputPathSketch "sketchMLOutput"$parallelism-$iterations-$trainingFile".txt" --outputPathFlink "flinkOriginalSGDOutput"$parallelism-$iterations-$trainingFile".txt""
       $FLINK_CLUSTER_PATH run $JAR_PATH --inputTrain $TRAINING_DATA_PATH$trainingFile --parallelism $parallelism --iterations $iterations --stepSize "0.5" --compressionType $compression --threshold "0.001" --sketchOrFlink $method --outputPathSketch "sketchMLOutput"$parallelism-$iterations-$trainingFile".txt" --outputPathFlink "flinkOriginalSGDOutput"$parallelism-$iterations-$trainingFile".txt"
    done
  done
fi
