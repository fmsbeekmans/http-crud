package com.fmsbeekmans.http.crud.http4s

import cats.data.{Kleisli, OptionT}
import cats.implicits._
import cats.{Applicative, MonadError}
import com.fmsbeekmans.http.crud.core.Keys
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, Request, Response}

case class Browse[Backend, K, V, F[_]](
    backend: Backend,
    path: String
)(
    implicit repository: Keys[Backend, K, V, F],
    keysDecoder: EntityEncoder[F, Seq[K]],
    F: MonadError[F, Throwable]
) {
  val dsl: Http4sDsl[F] = org.http4s.dsl.Http4sDsl[F]
  import dsl._

  type Opt[A] = OptionT[F, A]
  type Route = Kleisli[Opt, Request[F], Response[F]]

  def browse: Kleisli[Opt, Request[F], Seq[K]] =
    Kleisli[Opt, Request[F], Seq[K]] {
      case BrowseF(ids) =>
        OptionT(ids.map(Option.apply))
      case _ =>
        OptionT(F.pure(None))
    }

  def toResponse[G[_]: Applicative]: Kleisli[G, Seq[K], Response[F]] =
    Kleisli[G, Seq[K], Response[F]] { keys =>
      Response[F](Ok).withEntity[Seq[K]](keys).pure[G]
    }

  val route: Kleisli[Opt, Request[F], Response[F]] =
    browse.andThen(toResponse[Opt])

  object BrowseF {
    def unapply(req: Request[F]): Option[F[Seq[K]]] = req match {
      case GET -> Root / `path` =>
        Some(repository.keys(backend))
      case _ => None
    }
  }

  object BrowseResponseF {
    def unapply(req: Request[F]): Option[F[Response[F]]] = req match {
      case GET -> Root / `path` =>
        Some {
          repository
            .keys(backend)
            .flatMap(toResponse[F].run)
        }
      case _ => None
    }
  }

  def unapply(req: Request[F]): Option[F[Response[F]]] = req match {
    case GET -> Root / `path` =>
      Some {
        repository
          .keys(backend)
          .flatMap(toResponse[F].run)
      }
    case _ => None
  }
}
