# WikiGraph
WikiGraph is the big data project I did as part of my Insight Data Science (NY) Data Engineering fellowship from January-March 2016. The project is ment to showcase my learning experience of implementing a big data ETL pipeline. I choose to work with data from the Wikimedia Foundation, publicly available at a number of locations ([here](https://dumps.wikimedia.org/) and [here](http://dumps.wikimedia.org/other/pagecounts-raw/)).

The app: 
    
Slideshare presentation: slideshare.com/dfjdkfjdkf

## Table of contents
- [Introduction](#introduction)
- [Data](#data)
- [Ingestion](#ingestion)
- [Batch job](#batch-job)
- [Pipeline](#pipeline)
- [Future extensions](#future-extensions)

## Introduction
Wikipedia is a very popular source of knowledge and therefore interesting to take a closer look at.
WikiGraph ingests the latest dump of the english Wikipedia corpus into HDFS, calculates the PageRank using Spark GraphX, and stores the 
information in Neo4j for querying the shortest weighted path from one article to another using the normalized PageRank as weights for the 
directed links from one article to another.

## Data
The Wikipedia data used for this project comes in the form of sql-dumps from http://dumps.wikimedia.org/enwiki/YYYYMMDD where YYYYMMDD denotes the time of the dump.
The specific dumps are:

- enwiki-YYYYMMDD-page.sql.gz ~6 GB decompressed
- enwiki-YYYYMMDD-pagelinks.sql.gz ~34 GB decompressed

## Ingestion
The data is sanitised with the scripts found in the /cleaning folder. Even though the files are large the scripts never hold 
more than a few hundred bytes of each file in memory. The scripts read the files as byte-streams and matches the relevant patterns
using regular expressions.

An alternative to using the scripts would be to load the dumps into a MySQL database. This can take a considerable amount of time depending on 
the amount of memory and tweaking that goes into the MySQL database set-up.
If the dumps are loaded into a database it is possible to extract the relevant rows of each table-dump directly into HDFS using [scoop](https://sqoop.apache.org). This has considerable advantages in terms of speed. 
The problem is that extracting `varbinary` columns (which is how page titles are stored) does not easily translate to unicode.

## Batch job
The batch processing for calculating PageRank (and normalized edge-weights) in the Wikipedia-graph is done in to steps. 
- 1) In the first step the data is read, sanitised and the relevant rows are extracted.
- 2) In the second job the actual page rank is calculated using the built-in graph functionality for calculating global PageRank.
 
The PageRank-algorithm in GraphX comes in two forms. A dynamic that runs until convergence (until a pass over the entire graph does not change ANY rank more than a predefined increment/tolerance). 
Setting a low tolerance can significantly increase the time for the job to run.
The other form, which is used in the batch job, is a static version where the number of iterations is predefined. 

## Pipeline
![pipeline](https://raw.githubusercontent.com/sjher/wikigraph/master/images/pipeline.png)

## Future extensions
- Extend the PageRank calculations with page view data to a intentional surfer model (see [wikipedia](https://en.wikipedia.org/wiki/PageRank)).
- Include real-time. Wikipedia keeps a live-feed of edits to the entire wikipedia corpus and it would be very interesting to include this edit feed to look up relationships between two recently edited pages.
- Evaluate and benchmark Neo4j against other graph-databases such as OrientDB
