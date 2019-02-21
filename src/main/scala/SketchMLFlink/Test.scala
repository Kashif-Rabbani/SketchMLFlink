package SketchMLFlink

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.ml.MLUtils
import org.apache.flink.ml.common.LabeledVector
import org.apache.flink.ml.preprocessing.Splitter
import org.apache.flink.ml.regression.SketchMultipleLinearRegression

object Test {
  def main(args: Array[String]): Unit = {

    val params: ParameterTool = ParameterTool.fromArgs(args)

    val env = ExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    // make parameters available in the web interface
    env.getConfig.setGlobalJobParameters(params)
    // Create multiple linear regression learner

    val mlr = SketchMultipleLinearRegression()
      .setIterations(10)
      .setStepsize(0.5)
      .setConvergenceThreshold(0.001)

    // Obtain training and testing data set
    /*    val trainingDS: DataSet[LabeledVector] = MLUtils.readLibSVM(env, "/home/batuhan/Downloads/kddb/kddb")
        val astroTestingDS: DataSet[LabeledVector] = MLUtils.readLibSVM(env, "/home/batuhan/Downloads/kddb/kddb.t")
        val testingDS : DataSet[Vector] = astroTestingDS.map(lv => lv.vector)*/


    val dataSet: DataSet[LabeledVector] = MLUtils.readLibSVM(env, "/home/batuhan/Downloads/kddb/kddb.t").first(100)
    val trainTestData = Splitter.trainTestSplit(dataSet,0.5)
    val trainingDS: DataSet[LabeledVector] = trainTestData.training
    val testingDS = trainTestData.testing.map(lv => (lv.vector, lv.label))

    mlr.fit(trainingDS)
    // Calculate the predictions for the test data
    //val predictions: DataSet[(Vector,Double)] = mlr.predict(testingDS)

    val evaluationPairs: DataSet[(Double, Double)] = mlr.evaluate(testingDS)
    evaluationPairs.writeAsText("file:///home/batuhan/Downloads/kddb/test.txt", WriteMode.OVERWRITE).setParallelism(1)


//    val absoluteErrorSum = evaluationPairs.collect().map{
//      case (truth, prediction) => Math.abs(truth - prediction)}.sum
//    print(absoluteErrorSum)
    //absoluteErrorSum should be < 50.0

    env.execute

    //print(env.getExecutionPlan())
  }
}