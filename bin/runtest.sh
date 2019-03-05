#!/bin/bash

minIterations=$1
maxIterations=$2
minParallelism=$3
maxParallelism=$4
trainingFile=$5
method=$6
compression=$7

TRAINING_DATA_PATH=/home/marc/Documents/SketchMLData/
TESTING_DATA_PATH=/home/marc/Documents/SketchMLData/

FLINK_CLUSTER_PATH=/share/flink/flink-1.7.0/bin/flink
JAR_PATH=/home/marc/Development/SketchMLFlink/target/sketchmlFlink-1.0-SNAPSHOT.jar

usage ()
{
  echo 'Usage: ./runTest.sh minIterations maxIterations minParallelism maxParallelism trainingFile method compression'
}

if [ "$#" -ne 7 ]
then
  usage
else
  for parallelism  in $( seq $minParallelism $maxParallelism)
  do
    echo "> Parallelism $parallelism"
    for iterations  in $( seq $minIterations $maxIterations)
      do
        echo ">> Iterations $iterations"
        echo ">> Training file  $trainingFile"
       echo "$FLINK_CLUSTER_PATH run $JAR_PATH --inputTrain $TRAINING_DATA_PATH$trainingFile --parallelism $parallelism --iterations $iterations --stepSize "0.5" --compressionType $compression --threshold "0.001" --sketchOrFlink $method --outputPathSketch "sketchMLOutput"$parallelism-$iterations-$trainingFile".txt" --outputPathFlink "flinkOriginalSGDOutput"$parallelism-$iterations-$trainingFile".txt""
       $FLINK_CLUSTER_PATH run $JAR_PATH --inputTrain $TRAINING_DATA_PATH$trainingFile --parallelism $parallelism --iterations $iterations --stepSize "0.5" --compressionType $compression --threshold "0.001" --sketchOrFlink $method --outputPathSketch "sketchMLOutput"$parallelism-$iterations-$trainingFile".txt" --outputPathFlink "flinkOriginalSGDOutput"$parallelism-$iterations-$trainingFile".txt"
    done
  done
fi
