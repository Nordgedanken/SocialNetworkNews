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
    val reader = this.getReader
    val api = RestAPISingleton.getRestAPI
    reader.foreach(line => {
      val tweetID: Long = line.head.toLong
      println(tweetID.toString)
      //Get Tweet from id
      val tweets = Await.result(api.getTweet(id = tweetID), scala.concurrent.duration.Duration.Inf)
      val tweet = tweets.data
      tweet.in_reply_to_status_id_str match {
        case Some(value) => println("inReplyTo: ", value)
        case None => println("inReplyTo: None")
      }
      tweet.extended_entities match {
        case Some(value) => println("ExtendedEntities: ", value)
        case None => println("ExtendedEntities: None")
      }
      val df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
      println("Created at: ", df.format(tweet.created_at))
      println("ID: ", tweet.id)
      println("RetweetCount: ", tweet.retweet_count)
      println("RetweetStatus: ", tweet.retweeted)
      println("FavoriteCount: ", tweet.favorite_count)
      println("Text:", tweet.text)
      tweet.user match {
        case Some(value) => println("User: ", value)
        case None => println("User: None")
      }
    })
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
