package example

import java.util.concurrent.Executors

import cats.effect._
import cats.implicits._
import example.config.AppConfig
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.CORSConfig
import scala.concurrent.duration._
import org.http4s.server.middleware.CORS

import scala.concurrent.ExecutionContext

object Hello extends IOApp {

  val originConfig = CORSConfig(
    anyOrigin = true,
    allowCredentials = false,
    maxAge = 1.day.toSeconds)

  override def run(args: List[String]): IO[ExitCode] = {
    createServer[IO]()
  }

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer](): F[ExitCode] = {
    for {
      conf <- AppConfig.read()
      blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
      staticEndpoints = StaticEndpoints[F](conf.assets, blockingEc)
      httpApp = staticEndpoints.endpoints().orNotFound
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(conf.http.port, conf.http.host)
        .withHttpApp(CORS(httpApp))
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    } yield exitCode
  }
}
