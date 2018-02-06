package twitterCrawler

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.danielasfregola.twitter4s.entities.streaming.common.{DisconnectMessage, LimitNotice, WarningMessage}
import com.danielasfregola.twitter4s.entities.{Tweet, User}
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}


object RestAPISingleton {
  private var restAPI: TwitterRestClient = _
  def getRestAPI: TwitterRestClient = {
    this.restAPI
  }

  def setRestAPI(restAPI: TwitterRestClient): Unit = {
    this.restAPI = restAPI
  }

}

/**
  *
  * StreamingAPI class is the connector for the Twitter Streaming Api
  *
  * It mainly has the purpose to get Tweets of the users from the Lists and Hashtags defined inside the Config
  *
  * @param streamingClient holds the current Client for the Twitter Streaming API
  */
class StreamingApi(val streamingClient: TwitterStreamingClient) extends StrictLogging {
  val conf: Config = ConfigFactory.load()
  val restClient: TwitterRestClient = RestAPISingleton.getRestAPI

  /**
    * fetchTweets is a async function, that listens on Twitter's Streaming API for the defined Lists and Hastags
    *
    * @return
    */
  def fetchTweets: Future[TwitterStream] = {
    val trackedWordsConf =
      conf.getStringList("twitter.trackedWords")
    var trackedWords: Seq[String] = Seq()
    val trackedLists: List[String] =
      conf.getStringList("twitter.lists").asScala.toList
    val trackedUsers: Seq[Long] = Await.result(
      this.getListUsers(trackedLists),
      scala.concurrent.duration.Duration.Inf
    )

    trackedWordsConf.forEach((s: String) =>{
      trackedWords = trackedWords :+ s
    })

    logger.info(
      s"Launching streaming session with tracked keywords: $trackedWords\r\n" +
        s"And with tracked Users: $trackedUsers"
    )

    streamingClient.filterStatuses(tracks = trackedWords, follow = trackedUsers) {
      case tweet: Tweet =>
        println("=============")
        logger.info("Found a new Tweet... Saving...")
        logger.debug(tweet.text)
        this.saveIDforLater(tweet)
        logger.debug("Done Saving")
        println("=============")
      case disconnect: DisconnectMessage =>
        logger.warn("Disconnect: ", disconnect.disconnect)
      case limit: LimitNotice =>
        logger.warn("Limit: ", limit)
      case warning: WarningMessage =>
        logger.warn("Warning: ", warning.warning)
      case default =>
        logger.debug(default.toString)
    }
  }

  /**
    * saveIDforLater saves the tweetID and indexing Date to a csv as soon as the Tweet gets published
    *
    * @todo implement
    * @param tweet holds the currently processed Tweet
    */
  private def saveIDforLater(tweet: Tweet): Unit = {
    val currentDate =
      new Date(DateTime.now(DateTimeZone.UTC).getMillis)
    val today = new SimpleDateFormat("dd_MM_yyyy").format(currentDate)
    val dir = new File("data/")
    if (!dir.exists()) {
      dir.mkdir()
    }
    val todaysTweets = new File(s"data/tweets.$today")
    if (!todaysTweets.exists()) {
      todaysTweets.createNewFile()
    }
    val writer = CSVWriter.open(todaysTweets, append = true)
    writer.writeRow(List(tweet.id_str, currentDate.toString))

    writer.close()
  }

  /**
    * getListUsers is used to ask the Twitter API about what users are in a List
    *
    * Uses the Twitter Rest API
    *
    * @param trackedLists holds the list slugs and corresponding Users for all tracked lists
    * @return
    */
  private def getListUsers(trackedLists: List[String]): Future[Seq[Long]] = {
    Future {
      var trackedUsers: Seq[Long] = Seq()
      trackedLists.foreach((v: String) => {
        val splitted = v.split("/")
        val username = splitted(0)
        val slug = splitted(1)

        var listUsers = Await.result(
          restClient.listMembersBySlugAndOwnerName(
            slug = slug,
            owner_screen_name = username,
            include_entities = false
          ),
          scala.concurrent.duration.Duration.Inf
        )
        listUsers.data.users.foreach((user: User) => {
          trackedUsers = trackedUsers :+ user.id
        })
        while (listUsers.data.next_cursor != listUsers.data.previous_cursor) {
          listUsers = Await.result(
            restClient.listMembersBySlugAndOwnerName(
              slug = slug,
              owner_screen_name = username,
              include_entities = false,
              cursor = listUsers.data.next_cursor
            ),
            scala.concurrent.duration.Duration.Inf
          )
          listUsers.data.users.foreach((user: User) => {
            trackedUsers = trackedUsers :+ user.id
          })
        }

      })
      trackedUsers
    }
  }
}
