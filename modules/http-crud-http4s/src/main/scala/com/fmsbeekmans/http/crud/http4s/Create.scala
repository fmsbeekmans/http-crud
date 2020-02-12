package com.fmsbeekmans.http.crud.http4s

import cats.{Applicative, MonadError}
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import com.fmsbeekmans.http.crud.core.Store
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, Request, Response}

case class Create[Backend, K, V, F[_]](
    backend: Backend,
    path: String
)(
    implicit repository: Store[Backend, K, V, F],
    decodeValue: EntityDecoder[F, V],
    encodeKey: EntityEncoder[F, K],
    F: MonadError[F, Throwable]
) {
  val dsl: Http4sDsl[F] = org.http4s.dsl.Http4sDsl[F]
  import dsl._

  type Opt[A] = OptionT[F, A]
  type Route = Kleisli[Opt, Request[F], Response[F]]

  def create: Kleisli[Opt, Request[F], K] =
    Kleisli[Opt, Request[F], K] {
      case CreateF(id) =>
        OptionT(id.map(Option.apply))
      case _ =>
        OptionT(F.pure(None))
    }

  def toResponse[G[_]: Applicative]: Kleisli[G, K, Response[F]] =
    Kleisli[G, K, Response[F]] { id =>
      Response[F](Ok).withEntity[K](id).pure[G]
    }

  val route: Kleisli[Opt, Request[F], Response[F]] =
    create.andThen(toResponse[Opt])

  object CreateF {
    def unapply(req: Request[F]): Option[F[K]] = req match {
      case req @ POST -> Root / `path` =>
        Some {
          for {
            entity <- req.as[V]
            id <- repository.store(backend, entity)
          } yield id
        }
      case _ => None
    }
  }

  object CreateResponseF {
    def unapply(req: Request[F]): Option[F[Response[F]]] = req match {
      case req @ POST -> Root / `path` =>
        Some {
          for {
            entity <- req.as[V]
            id <- repository.store(backend, entity)
            resp <- toResponse[F].run(id)
          } yield resp
        }
      case _ => None
    }
  }
}
