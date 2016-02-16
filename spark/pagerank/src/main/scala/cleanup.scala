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

		val linksFile = sc.textFile("/wikipedia/pagelinks.txt")
		val links = getLinksFromFile(linksFile)

		val pagesFile = sc.textFile("/wikipedia/en-pages.txt")
		val pages = getPagesFromFile(pagesFile).persist(org.apache.spark.storage.StorageLevel.MEMORY_AND_DISK)

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
			case (fromId, toId) => Array(fromId, toId).mkString(" ")
		}
		edgesForPrint.saveAsTextFile("/wikipedia/en/cleanedLinks")

		val ranksForPrint = pages.map {
			case (id, title) => Array(id, title).mkString(" ")
		}
		ranksForPrint.saveAsTextFile("/wikipedia/en/cleanedPages")
	}

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

	def getLinksFromFile(linksFile: RDD[String]): RDD[(Long, Int, String, Int)] = {
		//remove invalid entries
		val sanitizedLinks = linksFile.map {
			line => line.split(" ")
		}.filter {
			case line => (line.length == 4)
		}.map {
			case line => (toLong(line(0)),toInt(line(1)),line(2),toInt(line(3)))
		}.filter {
			case (id,ns1,_,ns2) => ((id != -1) && (ns1 != -1) && (ns2 != -1))
		}

		//only keep name-space 0
		//https://en.wikipedia.org/wiki/Wikipedia:What_is_an_article%3F
		sanitizedLinks.filter {
			case (_, ns1, _, ns2) => (ns1 == 0 && ns2 == 0)
		}
	}

	def getPagesFromFile(pagesFile: RDD[String]): RDD[(Long, String)] = {
		val sanitizedPages = pagesFile.map {
			line => line.split(" ")
		}.filter {
			case line => (line.length == 3)
		}.map {
			case line => (toLong(line(0)),toInt(line(1)),line(2))
		}.filter {
			case (id,ns,title) => ((id != -1) && (ns != -1))
		}

		//only keep name-space 0
		//https://en.wikipedia.org/wiki/Wikipedia:What_is_an_article%3F
		sanitizedPages.filter {
			case (_, ns, _) => ns == 0
		}.map {
			case (id, _, title) => (id, title)
		}
	}
}
