package twitterSender

import com.danielasfregola.twitter4s.TwitterRestClient

import scala.concurrent.Await

class Sender (val restClient: TwitterRestClient) {
  def sendHelloWorld = {
    Await.result(restClient.createTweet(status = "Hello world vom Scalar Freifunk News Projekt!"), scala.concurrent.duration.Duration.Inf)
  }
}
