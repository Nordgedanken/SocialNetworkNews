import com.danielasfregola.twitter4s.entities.enums.AccessType
import com.danielasfregola.twitter4s.entities.enums.AccessType.AccessType
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import com.danielasfregola.twitter4s.util.Configurations.{
  consumerTokenKey,
  consumerTokenSecret
}
import com.danielasfregola.twitter4s.{
  TwitterAuthenticationClient,
  TwitterRestClient,
  TwitterStreamingClient
}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Main extends App {

  val consumerToken =
    ConsumerToken(key = consumerTokenKey, secret = consumerTokenSecret)
  val TwitterAuthClient = new TwitterAuthenticationClient(consumerToken)

  val write_access: AccessType = AccessType.Write

  val reqToken =
    TwitterAuthClient.requestToken(x_auth_access_type = Some(write_access))

  reqToken onComplete {
    case Success(token) =>
      println(
        TwitterAuthClient
          .authenticateUrl(token = token.token, force_login = false)
      )

      val pin = scala.io.StdIn.readLine("Insert Pin: ")

      val AccessTokenResp =
        Await.result(
          TwitterAuthClient.accessToken(token.token, pin),
          10 seconds
        )

      val consumerToken =
        ConsumerToken(key = consumerTokenKey, secret = consumerTokenSecret)
      val accessToken = AccessToken(
        key = AccessTokenResp.accessToken.key,
        secret = AccessTokenResp.accessToken.secret
      )

      val streamingClient =
        TwitterStreamingClient.apply(
          accessToken = accessToken,
          consumerToken = consumerToken
        )
      val restClient = TwitterRestClient.apply(
        accessToken = accessToken,
        consumerToken = consumerToken
      )

      val sender = new twitterSender.Sender(restClient = restClient)
      //sender.sendHelloWorld

      val stream = new twitterCrawler.StreamingApi(
        streamingClient = streamingClient,
        restClient = restClient
      )
      stream.fetchTweets

      webServer.WebServer.main()
    case Failure(err) => println(err.toString)
  }

  //Keep alive workaround
  val waitFunc = Future {
    while (true) {
      Thread.sleep(1000)
    }
  }

  Await.result(waitFunc, scala.concurrent.duration.Duration.Inf)
}
