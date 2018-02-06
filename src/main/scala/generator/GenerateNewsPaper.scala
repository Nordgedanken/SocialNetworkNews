package generator

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.StrictLogging
import org.quartz.{Job, JobExecutionContext}
import twitterCrawler.RestAPISingleton

import scala.concurrent.Await

class GenerateNewsPaper extends Job with StrictLogging {
  def execute(context: JobExecutionContext): Unit = {
    //TODO parse CSV from yesterday and generate Feed
    println("=============")
    val reader = this.getReader
    val api = RestAPISingleton.getRestAPI
    reader.foreach(line => {
      val tweetID: Long = line.head.toLong
      println("--------------")
      //Get Tweet from id
      val tweets = Await.result(api.getTweet(id = tweetID), scala.concurrent.duration.Duration.Inf)
      val tweet = tweets.data
      tweet.in_reply_to_status_id_str match {
        case Some(value) => logger.info(s"inReplyTo: $value")
        case None => logger.info("inReplyTo: None")
      }
      val df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
      val created_at = df.format(tweet.created_at)
      val retweet_count = tweet.retweet_count
      val retweeted = tweet.retweeted
      val favorite_count = tweet.favorite_count
      logger.info(s"Created at: $created_at")
      logger.info(s"ID: $tweetID")
      logger.info(s"RetweetCount: $retweet_count")
      logger.info(s"RetweetStatus: $retweeted")
      logger.info(s"FavoriteCount: $favorite_count")
      println("--------------")
    })
    println("=============")
  }

  private def getReader: CSVReader = {
    var reader: CSVReader = null
    val yesterdayDate = this.yesterday
    val yesterday = new SimpleDateFormat("dd_MM_yyyy").format(yesterdayDate)
    val yesterdaysTweets = new File(s"data/tweets.$yesterday")
    if (yesterdaysTweets.exists()) {
      reader = CSVReader.open(yesterdaysTweets)
    }
    reader
  }

  import java.util.Calendar

  private def yesterday: Date = {
    val cal = Calendar.getInstance
    cal.add(Calendar.DATE, -1)
    cal.getTime
  }
}
