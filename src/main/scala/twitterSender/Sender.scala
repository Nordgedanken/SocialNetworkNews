package twitterSender

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.Tweet

import scala.concurrent.Await

class Sender(val restClient: TwitterRestClient) {
  def sendHelloWorld: Tweet = {
    Await.result(
      restClient.createTweet(
        status = "Hello world vom Scalar Freifunk News Projekt!"
      ),
      scala.concurrent.duration.Duration.Inf
    )
  }
}
