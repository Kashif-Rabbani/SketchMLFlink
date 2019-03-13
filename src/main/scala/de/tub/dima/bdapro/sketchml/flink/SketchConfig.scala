package de.tub.dima.bdapro.sketchml.flink

/*
Singleton class gathering configuration values for the SketchGradient algorithm.
 - COMPRESSION_TYPE: Select gradient compression between Sketch and None.
 - FEATURES_SIZE: Maximum number of features.
 - SKETCH_GROUP_NO: Sketch data structure group number.
 - LOG_OUTPUT_PATH: Custom log file for the tests.
 - ReduceOurReduceGroup: Select gradient descent algorithm between Reduce and Reducegroup
 */

object SketchConfig {
  var COMPRESSION_TYPE: String = _ //By default it is "Sketch". It can also be set as "None"
  var FEATURES_SIZE: Integer = _  //This will be initialized automatically depending on the number of features in the data
  val SKETCH_GROUP_NO: Integer = 2 // Choice of Number of Groups in the Sketch
  val LOG_OUTPUT_PATH: String = "SketchMLFlinkLogs.txt"
  val ReduceOurReduceGroup: String = "Reduce" // "Reduce" or "ReduceGroup"
}
