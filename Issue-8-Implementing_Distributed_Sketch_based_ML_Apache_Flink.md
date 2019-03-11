Big Data Project, class of 2019
------------------------------

### Technische Universität Berlin

BDAPRO Course [repository](https://github.com/TU-Berlin-DIMA/BDAPRO.WS1819).

Project [repository](https://github.com/Kashif-Rabbani/SketchMLFlink).

Group id:

-   Batuhan Tüter - @tuterbatuhan - tuterbatuhan\@gmail.com
-   Kashif Rabbani - @Kashif-Rabbani - kashif.rabbani\@camput.tu-berlin.de
-   Marc Garnica Caparrós - @marcgarnica13 - marcgarnicacaparros\@gmail.com

Mentorship:

-    Dr.Alireza Alireza Mahdiraji - @alirezarm - alireza.rm\@dfki.de
-    Behrouz Derakhshan - @d-behi - behrouz.derakhshan\@dfki.de

### [Issue #8](https://github.com/TU-Berlin-DIMA/BDAPRO.WS1819/issues/8) Implementing Distributed Sketch-based Machine Learning in Apache Flink.

## Abstract

Gradient Descent is one of the most used optimization methods in supervised learning and distributed Machine Learning algorithms. In recent years, several researchers have proposed strategies and approaches to parallelize the computation of Stochastic Gradient Descent and distribute the workload across a cluster of nodes. These methods usually involve communicating the gradients through the network. Since the computation has become more powerful and new software tools allow the automatic and efficient distribution of the tasks, there is an active research field studying how to reduce the communication cost between nodes by means of gradient compression.
There is a recently published paper, SketchML [1] presents a gradient compression method. This paper implements a probabilistic data structure, called Sketch, to approximate the gradients and reduce the communication cost. They analyze and report the results they achieved as well as the model correctness and error bound, by implementing the proposed data structure on top of Apache Spark.
This study aims to present the details of the compression technique that is implemented in SketchML and mimic its implementation on top of Apache Flink. Our SketchML implementation remains the same as the one proposed in the paper but its properties are tested on top of a Flink optimization algorithm. The outcomes of this study include an effective comparison between Spark and Flink implementations and further analysis of SketchML technique on top of distributed machine learning in Apache Flink.

## Repository structure

## Installing library dependencies
To correctly compile the project and generate the desired executable jars the local Maven repository must contain the [SketchML.jar](./lib/SketchML.jar) with the SketchML libraries and methods.

```bash
mvn install:install-file -Dfile=<path-to-SketchML-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>
```

## Generate the executable file
Navigate to the root folder of the project and run the following command:
```bash
mvn install
mvn clean package
```
The generate jar file is going to be accessible in the target folder.

## Running the tests
The tests cases are fully customizable and can be executed either in a standalone manner executing the jar file or running the jar file as a Flink job in a Flink cluster. For both options, the parameters are the following:

- parallelism
- iterations
- stepSize
- compressionType
- threshold
- sketchOrFlink
- maxDim
- outputPathSketch
- outputPathFlink

### Running a single execution

Running a single test is possible with the following command:

```bash
java -jar <path-to-the-jar> --inputTrain <path-to-the-data-file> --parallelism <parallelism> --iterations <iterations> --stepSize <stepSize> --compressionType <Sketch | None> --threshold <threshold> --sketchOrFlink <Flink | Sketch> --outputPathSketch <output-file-sketch> --outputPathFlink <output-file-flink> --maxDim <dimension>
```

To run the same execution but as a Flink job in a running Flink cluster it is only needed to execute the following command:

```bash
<FLINK_CLUSTER_PATH/bin/flink run <JAR_PATH> --inputTrain <TRAINING_DATA_FILE_PATH> --parallelism <parallelism> --iterations <iterations> --stepSize <stepSize> --compressionType <Sketch | None> --threshold <threshold> --sketchOrFlink <Flink | Sketch> --outputPathSketch <output-file-sketch> --outputPathFlink <output-file-flink> --maxDim <dimension>
```

### Batch test cases

To optimize the execution of the designed experiments a bash script was implemented executing a customizable batch of tests as specified in the command line interface. The usage of the script is the following:

To correctly execute this script, edit the following lines:

```bash
TRAINING_DATA_PATH=<path-to-the-training-data-folder>
TESTING_DATA_PATH=<path-to-the-testing-data-folder>

FLINK_CLUSTER_PATH=<path-to-flink-cluster-root-dir>/bin/flink
JAR_PATH=<jar-file-path>
```

```bash
./runTest.sh minIterations maxIterations iterationsInterval minParallelism maxParallelism parallelismInterval initialDimension finalDimension dimensionInterval trainingFile method compression
```


## References
[1] Jiang, Jiawei, et al. "SketchML: Accelerating Distributed Machine Learning with Data Sketches." Proceedings of the 2018 International Conference on Management of Data. ACM, 2018.
[2] https://github.com/ccchengff/SketchML
