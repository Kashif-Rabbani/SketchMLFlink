
package de.tub.dima.bdapro.sketchml.flink

import java.io.{File, FileOutputStream, PrintWriter}

import org.apache.flink.api.common.functions.{MapFunction, RichFlatMapFunction, RichMapFunction}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.ml.common.LabeledVector
import org.apache.flink.ml.preprocessing.Splitter
import org.apache.flink.ml.regression.{FlinkMultipleLinearRegression, MultipleLinearRegression, SketchMultipleLinearRegression}
import org.apache.flink.util.Collector
import org.apache.flink.configuration.Configuration
import org.apache.flink.ml.math.SparseVector
import org.apache.flink.ml.pipeline.Predictor


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
      " Train Data File: " + params.get("inputTrain") + " Maximum dimension: " + params.get("maxDim") + "\n")

    val dataSet: DataSet[LabeledVector] = readLibSVMDimension(env, params.get("inputTrain"), params.get("maxDim").toInt)
    val trainTestData = Splitter.trainTestSplit(dataSet, 0.75)
    val trainingDS: DataSet[LabeledVector] = trainTestData.training
    val testingDS = trainTestData.testing.map(lv => (lv.vector, lv.label))

    if (params.get("sketchOrFlink") == "Sketch") {
      val mlr = SketchMultipleLinearRegression()
        .setIterations(params.get("iterations").toInt)
        .setStepsize(params.get("stepSize").toDouble)
      //.setConvergenceThreshold(params.get("threshold").toDouble)

      val startTime = System.currentTimeMillis()
      mlr.fit(trainingDS)

      val evaluationPairs = mlr.evaluate(testingDS)

      // Calculate the predictions for the test data
      // val predictions: DataSet[(Vector,Double)] = mlr.predict(testingDS)
      val absoluteErrorSum = evaluationPairs.map(pair => {
        val (truth, prediction) = pair
        Math.abs(truth - prediction)
      }).reduce((i, k) => i + k)

      var abs = absoluteErrorSum.collect().head
      var testingRows = testingDS.collect().size.toDouble

      val elapsedTime = System.currentTimeMillis() - startTime

      writer.append(java.time.LocalDateTime.now.toString + " Total elapsed time: " + elapsedTime.toDouble + "\n")
      writer.append(java.time.LocalDateTime.now.toString + " Average time per epoch: " + elapsedTime.toDouble / params.get("iterations").toInt+ "\n")
      writer.append(java.time.LocalDateTime.now.toString + " Absolute Error Sum: " + abs + "\n")
      writer.append(java.time.LocalDateTime.now.toString + " Avg Error Sum: " + abs / testingRows + "\n")

      //csv line as [sketchOrFlink, par, iter, step, compression, input, dimension, totalTime, timePerEpoch, absoluteError, avgError
      writer.append("CSV_Line: " + params.get("sketchOrFlink") + "," + params.get("parallelism") +
        "," + params.get("iterations") + "," + params.get("stepSize") + "," + params.get("compressionType") +
        "," + params.get("inputTrain").split("/").last + "," + params.get("maxDim") +
        "," + elapsedTime.toDouble + "," + (elapsedTime.toDouble / params.get("iterations").toInt) +
        "," + abs + "," + abs/testingRows
        )
    }

    // parameter "Flink" will run SGD without compression as original Flink SGD

    else if (params.get("sketchOrFlink") == "Flink") {
      val mlr = FlinkMultipleLinearRegression()
        .setIterations(params.get("iterations").toInt)
        .setStepsize(params.get("stepSize").toDouble)
      // .setConvergenceThreshold(params.get("threshold").toDouble)

      val startTime = System.currentTimeMillis()
      mlr.fit(trainingDS)

      val evaluationPairs = mlr.evaluate(testingDS)

      // Calculate the predictions for the test data
      // val predictions: DataSet[(Vector,Double)] = mlr.predict(testingDS)
      val absoluteErrorSum = evaluationPairs.map(pair => {
        val (truth, prediction) = pair
        Math.abs(truth - prediction)
      }).reduce((i, k) => i + k)

      var abs = absoluteErrorSum.collect().head
      var testingRows = testingDS.collect().size.toDouble

      val elapsedTime = System.currentTimeMillis() - startTime

      writer.append(java.time.LocalDateTime.now.toString + " Total elapsed time: " + elapsedTime.toDouble + "\n")
      writer.append(java.time.LocalDateTime.now.toString + " Average time per epoch: " + elapsedTime.toDouble / params.get("iterations").toInt+ "\n")
      writer.append(java.time.LocalDateTime.now.toString + " Absolute Error Sum: " + abs + "\n")
      writer.append(java.time.LocalDateTime.now.toString + " Avg Error Sum: " + abs / testingRows + "\n")

      //csv line as [sketchOrFlink, par, iter, step, compression, input, dimension, totalTime, timePerEpoch, absoluteError, avgError
      writer.append("CSV_Line: " + params.get("sketchOrFlink") + "," + params.get("parallelism") +
        "," + params.get("iterations") + "," + params.get("stepSize") + "," + params.get("compressionType") +
        "," + params.get("inputTrain").split("/").last + "," + params.get("maxDim") +
        "," + elapsedTime.toDouble + "," + (elapsedTime.toDouble / params.get("iterations").toInt) +
        "," + abs + "," + abs/testingRows
      )
    }
    writer.close()
  }

  def readLibSVMDimension(env: ExecutionEnvironment, filePath: String, maxDim: Int): DataSet[LabeledVector] = {
    val labelCOODS = env.readTextFile(filePath).flatMap(
      new RichFlatMapFunction[String, (Double, Array[(Int, Double)])] {
        val splitPattern = "\\s+".r

        override def flatMap(
                              line: String,
                              out: Collector[(Double, Array[(Int, Double)])]
                            ): Unit = {
          val commentFreeLine = line.takeWhile(_ != '#').trim

          if (commentFreeLine.nonEmpty) {
            val splits = splitPattern.split(commentFreeLine)
            val label = splits.head.toDouble
            val sparseFeatures = splits.tail
            val coos = sparseFeatures.flatMap { str =>
              val pair = str.split(':')
              require(pair.length == 2, "Each feature entry has to have the form <feature>:<value>")

              // libSVM index is 1-based, but we expect it to be 0-based
              val index = pair(0).toInt - 1
              val value = pair(1).toDouble
              Some((index, value))
            }
              .filter(value => value._1 < maxDim)
            if (coos.size > 0)
              out.collect((label, coos))
          }
        }
      })
    //labelCOODS.map(labelCOO => new LabeledVector(labelCOO._1, SparseVector.fromCOO(maxDim, labelCOO._2)))
    // Calculate maximum dimension of vectors
    val dimensionDS = labelCOODS.map {
      labelCOO =>
        labelCOO._2.map(_._1 + 1).max
    }.reduce(scala.math.max(_, _))

    labelCOODS.map {
      new RichMapFunction[(Double, Array[(Int, Double)]), LabeledVector] {
        var dimension = 0

        override def open(configuration: Configuration): Unit = {
          dimension = getRuntimeContext.getBroadcastVariable("dim").get(0)
        }

        override def map(value: (Double, Array[(Int, Double)])): LabeledVector = {
          new LabeledVector(value._1, SparseVector.fromCOO(dimension, value._2))
        }
      }
    }.withBroadcastSet(dimensionDS, "dim")


  }

}
