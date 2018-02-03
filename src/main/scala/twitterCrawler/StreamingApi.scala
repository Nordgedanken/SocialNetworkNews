package twitterCrawler

import com.danielasfregola.twitter4s.entities.streaming.common.DisconnectMessage
import com.danielasfregola.twitter4s.entities.{Tweet, User}
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class StreamingApi (val streamingClient: TwitterStreamingClient, val restClient: TwitterRestClient) {
  val conf: Config = ConfigFactory.load()
  def fetchHastags: Future[TwitterStream] = {
    val trackedWords = conf.getStringList("twitter.trackedWords").toSeq
    val trackedLists = conf.getStringList("twitter.lists").toList
    val trackedUsers: Seq[Long] = Await.result(this.getListUsers(trackedLists), scala.concurrent.duration.Duration.Inf)

    println(s"Launching streaming session with tracked keywords: $trackedWords\r\n" +
      s"And with tracked Users: $trackedUsers")

    streamingClient.filterStatuses(tracks = trackedWords, follow = trackedUsers) {
      case tweet: Tweet => println(tweet.text)
      case disconnect: DisconnectMessage => println("Disconnect: ", disconnect.disconnect.reason)
    }
  }

  private def getListUsers(trackedLists: List[String]): Future[Seq[Long]] = {
    Future {
      var trackedUsers: Seq[Long] = Seq()
      trackedLists.forEach((v: String) => {
        val splitted = v.split("/")
        val username = splitted(0)
        val slug = splitted(1)

        var listUsers = Await.result(restClient.listMembersBySlugAndOwnerName(slug = slug, owner_screen_name = username, include_entities= false), scala.concurrent.duration.Duration.Inf)
        listUsers.data.users.foreach((user: User) => {
          trackedUsers = trackedUsers:+user.id
        })
        while (listUsers.data.next_cursor != listUsers.data.previous_cursor) {
          listUsers = Await.result(restClient.listMembersBySlugAndOwnerName(slug = slug, owner_screen_name = username, include_entities= false, cursor = listUsers.data.next_cursor), scala.concurrent.duration.Duration.Inf)
          listUsers.data.users.foreach((user: User) => {
            trackedUsers = trackedUsers:+user.id
          })
        }

      })
      trackedUsers
    }
  }
}