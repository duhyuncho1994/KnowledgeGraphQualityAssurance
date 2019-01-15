# KnowledgeGraph
for Quality Assurance

# Knowledge Graph Quality Evaluation and Enhancement

A (sort of) compiler for KGs using ShEx/SHACL to create quality reports and provide optional corrections for faulty KGs.

## Research Direction
Quality assessment and enhancement of KGs.

## Used Platforms
- TopQuadrant java library (for SHACL) ([info](https://www.topquadrant.com/technology/shacl/) and [code](https://github.com/TopQuadrant/shacl))
- Apache Jena ([info](https://jena.apache.org/) and [code](https://github.com/apache/jena))

## Outcome
Tool (Java code) for evaluating large KGs with ShEx/SHACL that creates a quality report for a KG given some SHACL constraints.

## Repository Structure
The source code of the application can be found under _.\programming\src\\_.  
The source files of the report can be found under _.\latex\report\\_.  
The source files of the presentation can be found under _.\latex\presentation\\_.  
The report pdf can be found under snippets ($352).

## Usage
1. Install the necessary programms according to their respective installation instructions.
    1. Install Java.
    2. Install Apache Maven
2. Clone the repository.
3. Place the necessary files into the _.\programming\assets\\_ folder in the root directory.
    1. (Create the folder.)
    2. (Download the necessary asset files [here](https://goo.gl/ewMLGS))
    3. Place the _A_drugbank_part-constraints.ttl_ constraints into the folder.
    4. Place the _drugbank_whole_dataset.nt_ dataset into the folder.
4. (Modifiy the data set, so that it violates some constraints formulated in the _A_drugbank_part-constraints.ttl_ file)
5. Run ```mvn package ``` in the _programming_ directory of the repository.
6. Run ```java -jar .\target\kg_qa-%VERSION%.jar``` in the _programming_ directory, where _%VERSION%_ should be replaced with the current version of the app. (The startup may take a couple of seconds, depending on the speed of your computer)
