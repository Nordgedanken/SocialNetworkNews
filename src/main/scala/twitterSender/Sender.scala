package twitterSender

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.Tweet

import scala.concurrent.Await

class Sender(val restClient: TwitterRestClient) {
  def sendHelloWorld: Tweet = {
    Await.result(
      restClient.createTweet(
        status = "Test123"
      ),
      scala.concurrent.duration.Duration.Inf
    )
  }
}
