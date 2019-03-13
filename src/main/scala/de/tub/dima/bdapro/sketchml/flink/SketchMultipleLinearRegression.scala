/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.ml.regression

import de.tub.dima.bdapro.sketchml.flink.SketchGradientDescent
import org.apache.flink.api.scala.DataSet
import org.apache.flink.ml.math.{Breeze, Vector}
import org.apache.flink.ml.common._
import org.apache.flink.api.scala._
import org.apache.flink.ml.optimization.LearningRateMethod.LearningRateMethodTrait
import org.apache.flink.ml.optimization._
import org.apache.flink.ml.pipeline.{FitOperation, PredictOperation, Predictor}


/** SketchMultipleLinearRegression class, implementing the same functionalities as the original
  * FlinkML MultipleLinearRegression class
  */
class SketchMultipleLinearRegression extends Predictor[SketchMultipleLinearRegression] {
  import org.apache.flink.ml._
  import SketchMultipleLinearRegression._

  // Stores the weights of the linear model after the fitting phase
  var weightsOption: Option[DataSet[WeightVector]] = None

  def setIterations(iterations: Int): SketchMultipleLinearRegression = {
    parameters.add(Iterations, iterations)
    this
  }

  def setStepsize(stepsize: Double): SketchMultipleLinearRegression = {
    parameters.add(Stepsize, stepsize)
    this
  }

  def setConvergenceThreshold(convergenceThreshold: Double): SketchMultipleLinearRegression = {
    parameters.add(ConvergenceThreshold, convergenceThreshold)
    this
  }

  def setLearningRateMethod(learningRateMethod: LearningRateMethodTrait)
  : SketchMultipleLinearRegression = {
    parameters.add(LearningRateMethodValue, learningRateMethod)
    this
  }

  def squaredResidualSum(input: DataSet[LabeledVector]): DataSet[Double] = {
    weightsOption match {
      case Some(weights) => {
        input.mapWithBcVariable(weights){
          (dataPoint, weights) => lossFunction.loss(dataPoint, weights)
        }.reduce {
          _ + _
        }
      }

      case None => {
        throw new RuntimeException("The MultipleLinearRegression has not been fitted to the " +
          "data. This is necessary to learn the weight vector of the linear function.")
      }
    }

  }
}

object SketchMultipleLinearRegression {

  val WEIGHTVECTOR_BROADCAST = "weights_broadcast"

  val lossFunction = GenericLossFunction(SquaredLoss, LinearPrediction)

  // ====================================== Parameters =============================================

  case object Stepsize extends Parameter[Double] {
    val defaultValue = Some(0.1)
  }

  case object Iterations extends Parameter[Int] {
    val defaultValue = Some(10)
  }

  case object ConvergenceThreshold extends Parameter[Double] {
    val defaultValue = None
  }

  case object LearningRateMethodValue extends Parameter[LearningRateMethodTrait] {
    val defaultValue = None
  }

  // ======================================== Factory methods ======================================

  def apply(): SketchMultipleLinearRegression = {
    new SketchMultipleLinearRegression()
  }

  // ====================================== Operations =============================================

  /** Trains the linear model to fit the training data. The resulting weight vector is stored in
    * the [[MultipleLinearRegression]] instance.
    *
    */
  implicit val fitMLR = new FitOperation[SketchMultipleLinearRegression, LabeledVector] {
    override def fit(
                      instance: SketchMultipleLinearRegression,
                      fitParameters: ParameterMap,
                      input: DataSet[LabeledVector])
    : Unit = {
      val map = instance.parameters ++ fitParameters

      // retrieve parameters of the algorithm
      val numberOfIterations = map(Iterations)
      val stepsize = map(Stepsize)
      val convergenceThreshold = map.get(ConvergenceThreshold)
      val learningRateMethod = map.get(LearningRateMethodValue)

      val lossFunction = GenericLossFunction(SquaredLoss, LinearPrediction)

      val optimizer = SketchGradientDescent()
        .setIterations(numberOfIterations)
        .setStepsize(stepsize)
        .setLossFunction(lossFunction)

      convergenceThreshold match {
        case Some(threshold) => optimizer.setConvergenceThreshold(threshold)
        case None =>
      }

      learningRateMethod match {
        case Some(method) => optimizer.setLearningRateMethod(method)
        case None =>
      }

      instance.weightsOption = Some(optimizer.optimize(input, None))
    }
  }

  implicit def predictVectors[T <: Vector] = {
    new PredictOperation[SketchMultipleLinearRegression, WeightVector, T, Double]() {
      override def getModel(self: SketchMultipleLinearRegression, predictParameters: ParameterMap)
      : DataSet[WeightVector] = {
        self.weightsOption match {
          case Some(weights) => weights


          case None => {
            throw new RuntimeException("The MultipleLinearRegression has not been fitted to the " +
              "data. This is necessary to learn the weight vector of the linear function.")
          }
        }
      }
      override def predict(value: T, model: WeightVector): Double = {
        import Breeze._
        val WeightVector(weights, weight0) = model
        val dotProduct = value.asBreeze.dot(weights.asBreeze)
        dotProduct + weight0
      }
    }
  }
}
