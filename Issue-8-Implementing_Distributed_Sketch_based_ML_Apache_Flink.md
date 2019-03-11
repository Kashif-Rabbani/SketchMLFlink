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
There is a recently published paper, SketchML \cite{jiang2018sketchml}, which presents a gradient compression method. This paper implements a probabilistic data structure, called Sketch, to approximate the gradients and reduce the communication cost. They analyze and report the results they achieved as well as the model correctness and error bound, by implementing the proposed data structure on top of Apache Spark.
This study aims to present the details of the compression technique that is implemented in SketchML and mimic its implementation on top of Apache Flink. Our SketchML implementation remains the same as the one proposed in the paper but its properties are tested on top of a Flink optimization algorithm. \textbf{The outcomes of this study include an effective comparison between Spark and Flink implementations and further analysis of SketchML technique on top of distributed machine learning in Apache Flink.

## Repository structure
