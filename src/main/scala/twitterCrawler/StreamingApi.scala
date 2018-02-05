package twitterCrawler

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.danielasfregola.twitter4s.entities.streaming.common.DisconnectMessage
import com.danielasfregola.twitter4s.entities.{Tweet, User}
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

/**
  * StreamingAPI class is the connector for the Twitter Streaming Api
  *
  * It mainly has the purpose to get Tweets of the users from the Lists and Hashtags defined inside the Config
  */
class StreamingApi(val streamingClient: TwitterStreamingClient,
                   val restClient: TwitterRestClient) {
  val conf: Config = ConfigFactory.load()

  /**
    * fetchTweets is a async function, that listens on Twitter's Streaming API for the defined Lists and Hastags
    *
    * @return
    */
  def fetchTweets: Future[TwitterStream] = {
    val trackedWords: Seq[String] =
      conf.getStringList("twitter.trackedWords").asScala
    val trackedLists: List[String] =
      conf.getStringList("twitter.lists").asScala.toList
    val trackedUsers: Seq[Long] = Await.result(
      this.getListUsers(trackedLists),
      scala.concurrent.duration.Duration.Inf
    )

    println(
      s"Launching streaming session with tracked keywords: $trackedWords\r\n" +
        s"And with tracked Users: $trackedUsers"
    )

    streamingClient.filterStatuses(tracks = trackedWords, follow = trackedUsers) {
      case tweet: Tweet =>
        println(tweet.text)
        this.saveIDforLater(tweet)
        println("Done Saving")
      case disconnect: DisconnectMessage =>
        println("Disconnect: ", disconnect.disconnect.reason)
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
    val today = new SimpleDateFormat("dd/MM/yyyy").format(currentDate)
    val todaysTweets = new File(s"data/tweets.$today")
    if (!todaysTweets.exists()) {
      todaysTweets
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
