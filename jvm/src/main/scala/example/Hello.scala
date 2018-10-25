package example

import java.util.concurrent.Executors

import cats.effect._
import cats.implicits._
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object Hello extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    createServer[IO]()
  }

  def createServer[F[_] : ContextShift : ConcurrentEffect](): F[ExitCode] = {
    val blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
    val staticEndpoints = StaticEndpoints[F](blockingEc)

    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(staticEndpoints.endpoints(), "/")
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}

