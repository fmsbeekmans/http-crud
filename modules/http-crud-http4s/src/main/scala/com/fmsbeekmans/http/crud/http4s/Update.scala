package com.fmsbeekmans.http.crud.http4s

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import com.fmsbeekmans.http.crud.core.Put
import org.http4s.dsl.Http4sDsl
import org.http4s.{
  EntityDecoder,
  QueryParamDecoder,
  QueryParameterValue,
  Request,
  Response
}

case class Update[Backend, K, V, F[_]](
    backend: Backend,
    path: String
)(
    implicit repository: Put[Backend, K, V, F],
    matchKey: QueryParamDecoder[K],
    decodeValue: EntityDecoder[F, V],
    F: MonadError[F, Throwable]
) {
  val dsl: Http4sDsl[F] = org.http4s.dsl.Http4sDsl[F]
  import dsl._

  type Opt[A] = OptionT[F, A]
  type Route = Kleisli[Opt, Request[F], Response[F]]

  def update: Kleisli[Opt, Request[F], Boolean] =
    Kleisli[Opt, Request[F], Boolean] {
      case UpdateResultF(updateResultF) =>
        OptionT(updateResultF.map(Option.apply))
      case _ =>
        OptionT(F.pure(None))
    }

  val toResponse: Kleisli[Opt, Boolean, Response[F]] =
    Kleisli[Opt, Boolean, Response[F]] {
      case true  => OptionT(Ok().map(Option.apply))
      case false => OptionT(NotFound().map(Option.apply))
    }

  val route: Kleisli[Opt, Request[F], Response[F]] = update.andThen(toResponse)

  private object Key {
    def unapply(keyString: String): Option[K] = {
      matchKey.decode(QueryParameterValue(keyString)).toOption
    }
  }

  object UpdateResultF {
    def unapply(req: Request[F]): Option[F[Boolean]] = req match {
      case req @ PUT -> Root / `path` / Key(key) =>
        Some {
          for {
            entity <- req.as[V]
            updated <- repository.put(backend, key, entity)
          } yield updated
        }
      case _ => None
    }
  }
}
