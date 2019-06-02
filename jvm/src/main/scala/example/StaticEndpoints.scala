package example

import cats.effect.{ContextShift, Effect}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, StaticFile}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

class StaticEndpoints[F[_] : ContextShift : Effect](blockingEc: ExecutionContextExecutorService) extends Http4sDsl[F] {

  private[this] def static(file: String, blockingEc: ExecutionContext, request: Request[F]) =
    StaticFile.fromResource("/" + file, blockingEc, Some(request)).getOrElseF(NotFound())

  def endpoints(): HttpRoutes[F] = HttpRoutes.of[F] {
    case request@GET -> Root =>
      static("index.html", blockingEc, request)
    case request@GET -> Root / path if List(".js", ".css", ".map", ".html", ".ico").exists(path.endsWith) =>
      static(path, blockingEc, request)
    case request@GET -> "front-res" /: path =>
      val fullPath = "front-res/" + path.toList.mkString("/")
      static(fullPath, blockingEc, request)
  }
}

object StaticEndpoints {
  def apply[F[_] : ContextShift : Effect](blockingEc: ExecutionContextExecutorService): StaticEndpoints[F] =
    new StaticEndpoints[F](blockingEc)
}
