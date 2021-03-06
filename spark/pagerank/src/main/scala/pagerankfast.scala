import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object wikiPageRank {
	def main(args: Array[String]) {
		val sparkConf = new SparkConf()
			.setAppName("wikiPageRank")
		val sc = new SparkContext(sparkConf)

		val linkFiles = sc.textFile("/wikipedia/en/cleanedLinks/part-*")
		val linkLines = sc.union(linkFiles)

		val links = linkLines.map {
			line => line.split(" ")
		}.map {
			line => (line(0).toLong, line(1).toLong)
		}

		val pageFiles = sc.textFile("/wikipedia/en/cleanedPages/part-*")
		val pageLines = sc.union(pageFiles)

		val pages = pageLines.map{
			line => line.split(" ")
		}.map {
			line => (line(0).toLong, line(1))
		}

		val graph = Graph.fromEdgeTuples(links,1)

		//Run PageRank
		val ranks = graph.staticPageRank(30).persist(org.apache.spark.storage.StorageLevel.MEMORY_AND_DISK)

		val ranksByVertex = pages.join(ranks.vertices)
		val ranksForPrint = ranksByVertex.map {
			case (id, (title, rank)) => Array(id, title, rank).mkString("\t")
		}
		ranksForPrint.saveAsTextFile("/wikipedia/en/tabPageRanks")

		val weightsForPrint = ranks.edges.map {
			edge => (edge.srcId.toLong,edge.dstId.toLong,edge.attr.toString)
		}.map {
			case (fromId, toId, weight) => Array(fromId,toId,weight).mkString("\t")
		}
		weightsForPrint.saveAsTextFile("/wikipedia/en/tabLinkWeights")
	}
}
