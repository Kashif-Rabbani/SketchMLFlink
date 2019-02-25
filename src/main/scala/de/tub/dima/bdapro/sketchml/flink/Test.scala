package org.dma.sketchml.flink

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.ml.MLUtils
import org.apache.flink.ml.common.LabeledVector
import org.apache.flink.ml.preprocessing.Splitter
import org.apache.flink.ml.regression.{FlinkMultipleLinearRegression, MultipleLinearRegression, SketchMultipleLinearRegression}


object Test {
  def main(args: Array[String]): Unit = {

    val params = ParameterTool.fromArgs(args)

    val env = ExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(params.get("parallelism").toInt)

    // make parameters available in the web interface
    env.getConfig.setGlobalJobParameters(params)

    // Obtain training and testing data set
    /*  val trainingDS: DataSet[LabeledVector] = MLUtils.readLibSVM(env, "/home/batuhan/Downloads/kddb/kddb")
        val astroTestingDS: DataSet[LabeledVector] = MLUtils.readLibSVM(env, "/home/batuhan/Downloads/kddb/kddb.t")
        val testingDS : DataSet[Vector] = astroTestingDS.map(lv => lv.vector)*/


    val dataSet: DataSet[LabeledVector] = MLUtils.readLibSVM(env, params.get("inputTrain")).first(10)
    val trainTestData = Splitter.trainTestSplit(dataSet, 0.75)
    val trainingDS: DataSet[LabeledVector] = trainTestData.training
    val testingDS = trainTestData.testing.map(lv => (lv.vector, lv.label))

    // parameter "Sketch" will run SGD with compression
    if (params.get("sketchOrFlink") == "Sketch") {
      val mlr = SketchMultipleLinearRegression()
        .setIterations(params.get("iterations").toInt)
        .setStepsize(params.get("stepSize").toDouble)
        .setConvergenceThreshold(params.get("threshold").toDouble)

      mlr.fit(trainingDS)

      // Calculate the predictions for the test data
      // val predictions: DataSet[(Vector,Double)] = mlr.predict(testingDS)

      val evaluationPairs = mlr.evaluate(testingDS)
      evaluationPairs.writeAsText(params.get("outputPathSketch"), WriteMode.OVERWRITE).setParallelism(1)
    }

    // parameter "Flink" will run SGD without compression as original Flink SGD

    if (params.get("sketchOrFlink") == "Flink") {
      val mlr = FlinkMultipleLinearRegression()
        .setIterations(params.get("iterations").toInt)
        .setStepsize(params.get("stepSize").toDouble)
        .setConvergenceThreshold(params.get("threshold").toDouble)

      mlr.fit(trainingDS)
      // Calculate the predictions for the test data
      // val predictions: DataSet[(Vector,Double)] = mlr.predict(testingDS)

      val evaluationPairs = mlr.evaluate(testingDS)
      evaluationPairs.writeAsText(params.get("outputPathFlink"), WriteMode.OVERWRITE).setParallelism(1)
    }

    //    val absoluteErrorSum = evaluationPairs.collect().map{
    //    case (truth, prediction) => Math.abs(truth - prediction)}.sum
    //    print(absoluteErrorSum)
    //    absoluteErrorSum should be < 50.0
    env.execute
    //print(env.getExecutionPlan())
  }
}