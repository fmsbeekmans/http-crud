package com.fmsbeekmans.http.crud.http4s

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import com.fmsbeekmans.http.crud.core.Get
import org.http4s.dsl.Http4sDsl
import org.http4s.{
  EntityEncoder,
  QueryParamDecoder,
  QueryParameterValue,
  Request,
  Response
}

case class Read[Backend, K, V, F[_]](
    backend: Backend,
    path: String
)(
    implicit repository: Get[Backend, K, V, F],
    matchKey: QueryParamDecoder[K],
    valueEncoder: EntityEncoder[F, Option[V]],
    F: MonadError[F, Throwable]
) {
  val dsl: Http4sDsl[F] = org.http4s.dsl.Http4sDsl[F]
  import dsl._

  type Opt[A] = OptionT[F, A]
  type Route = Kleisli[Opt, Request[F], Response[F]]

  def read: Kleisli[Opt, Request[F], Option[V]] =
    Kleisli[Opt, Request[F], Option[V]] {
      case ReadF(entity) =>
        OptionT(entity.map(Option.apply))
      case _ =>
        OptionT(F.pure(None))
    }

  def toResponse: Kleisli[Opt, Option[V], Response[F]] =
    Kleisli[Opt, Option[V], Response[F]] { entity =>
      OptionT(F.pure(Some(Response[F](Ok).withEntity[Option[V]](entity))))

    }

  private object Key {
    def unapply(keyString: String): Option[K] = {
      matchKey.decode(QueryParameterValue(keyString)).toOption
    }
  }

  object ReadF {
    def unapply(req: Request[F]): Option[F[Option[V]]] = req match {
      case GET -> Root / `path` / Key(key) =>
        Some(repository.get(backend, key))
      case _ => None
    }
  }
}
