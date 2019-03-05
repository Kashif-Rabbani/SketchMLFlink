
package de.tub.dima.bdapro.sketchml.flink

import java.io.{File, FileOutputStream, PrintWriter}

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

    val writer = new PrintWriter(new FileOutputStream(new File(SketchConfig.LOG_OUTPUT_PATH), true))

    writer.append("\n" + java.time.LocalDateTime.now.toString + " Experiment Started for " + params.get("sketchOrFlink") + ". " + "Parallelism " + params.get("parallelism") +
      " Iterations: " + params.get("iterations") + " StepSize: " + params.get("stepSize") + " CompressionType: " + params.get("compressionType") +
      " Train Data File: " + params.get("inputTrain") + "\n")
    writer.close()

    val dataSet: DataSet[LabeledVector] = MLUtils.readLibSVM(env, params.get("inputTrain"))
    val trainTestData = Splitter.trainTestSplit(dataSet, 0.75)
    val trainingDS: DataSet[LabeledVector] = trainTestData.training
    val testingDS = trainTestData.testing.map(lv => (lv.vector, lv.label))



    // parameter "Sketch" will run SGD with compression
    if (params.get("sketchOrFlink") == "Sketch") {
      val mlr = SketchMultipleLinearRegression()
        .setIterations(params.get("iterations").toInt)
        .setStepsize(params.get("stepSize").toDouble)
      //.setConvergenceThreshold(params.get("threshold").toDouble)

      mlr.fit(trainingDS)
      /*A residual sum of squares (RSS) is a statistical technique used to measure the amount of variance in a data set
      that is not explained by a regression model.The residual sum of squares is a measure of the amount of error remaining between
      the regression function and the data set.*/
      val writer = new PrintWriter(new FileOutputStream(new File(SketchConfig.LOG_OUTPUT_PATH), true))

      val evaluationPairs = mlr.evaluate(testingDS)

/*      val weightList = mlr.weightsOption.get.collect()
      val srs = mlr.squaredResidualSum(trainingDS).collect().head

      println("SRS: " + srs)
      println("WeightList Size: " + weightList.size)*/

      // Calculate the predictions for the test data
      // val predictions: DataSet[(Vector,Double)] = mlr.predict(testingDS)
      val absoluteErrorSum = evaluationPairs.map(pair => {
        val (truth, prediction) = pair
        Math.abs(truth - prediction)
      }).reduce((i,k) => i+k)

/*      val absoluteErrorSum = evaluationPairs.collect().map{
        case (truth, prediction) => Math.abs(truth - prediction)}.sum*/
      //println("Absolute Error Sum "+ absoluteErrorSum.toString)
      //writer.append("SRS: " + srs + " WeightListSize: " + weightList.size + " Absolute Error Sum: " + absoluteErrorSum + "\n")
      writer.append(java.time.LocalDateTime.now.toString + " ")
      writer.append("Absolute Error Sum: " + absoluteErrorSum.collect().head + "\n")
      writer.close()

      //evaluationPairs.writeAsText(params.get("outputPathSketch"), WriteMode.OVERWRITE).setParallelism(1)
    }

    // parameter "Flink" will run SGD without compression as original Flink SGD

    if (params.get("sketchOrFlink") == "Flink") {
      val mlr = FlinkMultipleLinearRegression()
        .setIterations(params.get("iterations").toInt)
        .setStepsize(params.get("stepSize").toDouble)
      // .setConvergenceThreshold(params.get("threshold").toDouble)

      mlr.fit(trainingDS)
      /*A residual sum of squares (RSS) is a statistical technique used to measure the amount of variance in a data set
      that is not explained by a regression model.The residual sum of squares is a measure of the amount of error remaining between
      the regression function and the data set.*/
      val writer = new PrintWriter(new FileOutputStream(new File(SketchConfig.LOG_OUTPUT_PATH), true))

      //val weightList = mlr.weightsOption.get.collect()
      //val srs = mlr.squaredResidualSum(trainingDS).collect().head
      /*println("SRS: " + srs)
      println("WeightList Size: " + weightList.size)
*/

      // Calculate the predictions for the test data
      // val predictions: DataSet[(Vector,Double)] = mlr.predict(testingDS)

      val evaluationPairs= mlr.evaluate(testingDS)
      val absoluteErrorSum = evaluationPairs.map(pair => {
        val (truth, prediction) = pair
        Math.abs(truth - prediction)
      }).reduce((i,k) => i+k)

/*      val absoluteErrorSum = evaluationPairs.collect().map{
        case (truth, prediction) => Math.abs(truth - prediction)}.sum*/
      //println("Absolute Error Sum "+ absoluteErrorSum)
      writer.append(java.time.LocalDateTime.now.toString + " ")
      writer.append("Absolute Error Sum: " + absoluteErrorSum.collect().head + "\n")
      writer.close()
      //evaluationPairs.writeAsText(params.get("outputPathSketch"), WriteMode.OVERWRITE).setParallelism(1)
    }

    //    val absoluteErrorSum = evaluationPairs.collect().map{
    //    case (truth, prediction) => Math.abs(truth - prediction)}.sum
    //    print(absoluteErrorSum)
    //    absoluteErrorSum should be < 50.0
    //env.execute

    //print(env.getExecutionPlan())
  }
}