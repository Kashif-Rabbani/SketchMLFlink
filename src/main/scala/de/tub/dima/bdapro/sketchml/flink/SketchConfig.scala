package org.dma.sketchml.flink

object SketchConfig {
  var COMPRESSION_TYPE: String = "Sketch" //By default it is "Sketch". It can also be set as "None"
  var FEATURES_SIZE: Integer = 0  //This will be initialized automatically depending on the number of features in the data
  val SKETCH_GROUP_NO: Integer = 2 // Choice of Number of Groups in the Sketch
  val LOG_OUTPUT_PATH: String = "Logs.txt"
}
