import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object cleanWikiData {
	def main(args: Array[String]) {
		val sparkConf = new SparkConf()
			.setAppName("cleanWikiData")
		val sc = new SparkContext(sparkConf)

		def toLong(s: String): Long = {
			try {
				s.toLong
			} catch {
				case e: Exception => -1
			}
		}

		def toInt(s: String): Int = {
			try {
				s.toInt
			} catch {
				case e: Exception => -1
			}
		}

		val linksFile = sc.textFile("/wikipedia/pagelinks.txt")

		val links = linksFile.map {
			line => line.split(" ")
		}.filter {
			case linkTuple => (linkTuple.length == 4)
		}.map {
			case (id,ns1,title,ns2) => (toLong(id),toInt(ns1),title,toInt(ns2))
		}.filter {
			case (id,ns1,_,ns2) => ((id != -1) && (ns1 != -1) && (ns2 != -1))
		}.filter {
			case (_, ns1, _, ns2) => (ns1 == 0 && ns2 == 0)
		}

		val pagesFile = sc.textFile("/wikipedia/en-pages.txt")

		val pages = pagesFile.map {
			line => line.split(" ")
		}.filter {
			case pageTuple => (pageTuple.length == 3)
		}.map { line =>
			case (id,ns,title) => (toLong(id),toInt(ns),title)
		}.filter {
			case (id,ns,title) => ((id != -1) && (ns != -1))
		}.filter {
			case (_, ns, _) => ns == 0
		}.map {
			case (id, _, title) => (id, title)
		}.persist(org.apache.spark.storage.StorageLevel.MEMORY_AND_DISK)

		val pagesReverse = pages.map {
			case (id, title) => (title, id)
		}

		val linksReverse = links.map {
			case (fromPageId, _, toPageTitle, _) => (toPageTitle, fromPageId)
		}

		//remove edges with destination title that don't have corresponding pageId
		val validDestinationsOnly = pagesReverse.join(linksReverse).map {
			case (name, (toPageId, fromPageId)) => (fromPageId, toPageId)
		}

		//remove edges with source pageId that don't have corresponding page title
		val edges = pages.join(validDestinationsOnly).map {
			case (fromPageId, (title, toPageId )) => (fromPageId, toPageId)
		}

		val edgesForPrint = edges.map {
			case (fromId, toId) => Array(fromId, toId).mkString(",")
		}
		edgesForPrint.saveAsTextFile("/wikipedia/en/cleanedLinks")

		val ranksForPrint = pages.map {
			case (id, title) => Array(id, title).mkString(",")
		}
		ranksForPrint.saveAsTextFile("/wikipedia/en/cleanedPages")
	}
}
