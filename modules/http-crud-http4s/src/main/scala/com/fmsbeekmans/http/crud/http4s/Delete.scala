package com.fmsbeekmans.http.crud.http4s

import cats.{Applicative, MonadError}
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import com.fmsbeekmans.http.crud.core.Remove
import org.http4s.{QueryParamDecoder, QueryParameterValue, Request, Response}

case class Delete[Backend, K, V, F[_]](
    backend: Backend,
    path: String
)(
    implicit repository: Remove[Backend, K, V, F],
    matchKey: QueryParamDecoder[K],
    F: MonadError[F, Throwable]
) {
  val dsl = org.http4s.dsl.Http4sDsl[F]
  import dsl._

  type Opt[A] = OptionT[F, A]
  type Route = Kleisli[Opt, Request[F], Response[F]]

  def delete: Kleisli[Opt, Request[F], Boolean] =
    Kleisli[Opt, Request[F], Boolean] {
      case DeleteF(deleteResultF) =>
        OptionT(deleteResultF.map(Option.apply))
      case _ =>
        OptionT(F.pure(None))
    }

  def toResponse[G[_]: Applicative]: Kleisli[G, Boolean, Response[F]] =
    Kleisli[G, Boolean, Response[F]] {
      case true  => Response(Ok).pure[G]
      case false => Response(NotFound).pure[G]
    }

  val route: Kleisli[Opt, Request[F], Response[F]] =
    delete.andThen(toResponse[Opt])

  private object Key {
    def unapply(keyString: String): Option[K] = {
      matchKey.decode(QueryParameterValue(keyString)).toOption
    }
  }

  object DeleteF {
    def unapply(req: Request[F]): Option[F[Boolean]] = req match {
      case DELETE -> Root / `path` / Key(key) =>
        Some {
          repository.remove(backend, key)
        }
      case _ => None
    }
  }

  object DeleteResponseF {
    def unapply(req: Request[F]): Option[F[Response[F]]] = req match {
      case DELETE -> Root / `path` / Key(key) =>
        Some {
          repository
            .remove(backend, key)
            .flatMap(toResponse[F].run)
        }
      case _ => None
    }
  }
}
