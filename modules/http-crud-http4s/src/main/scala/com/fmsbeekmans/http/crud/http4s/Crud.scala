package com.fmsbeekmans.http.crud.http4s

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import com.fmsbeekmans.http.crud.core.Repository
import org.http4s.{
  EntityDecoder,
  EntityEncoder,
  QueryParamDecoder,
  QueryParameterValue,
  Request,
  Response
}

case class Crud[Backend, K, V, F[_]](
    backend: Backend,
    path: String
)(
    implicit repository: Repository[Backend, K, V, F],
    matchKey: QueryParamDecoder[K],
    decodeValue: EntityDecoder[F, V],
    encodeKey: EntityEncoder[F, K],
    encodeKeys: EntityEncoder[F, Seq[K]],
    encodeValue: EntityEncoder[F, V],
    encodeMaybeValue: EntityEncoder[F, Option[V]],
    F: MonadError[F, Throwable]
) {
  val dsl = org.http4s.dsl.Http4sDsl[F]
  import dsl._

  type Opt[A] = OptionT[F, A]
  type Route = Kleisli[Opt, Request[F], Response[F]]

  private object Key {
    def unapply(keyString: String): Option[K] = {
      matchKey.decode(QueryParameterValue(keyString)).toOption
    }
  }

  val browse = Browse(backend, path)
  val create = Create(backend, path)
  val read = Read(backend, path)
  val update = Update(backend, path)
  val delete = Delete(backend, path)

  browse.route.ap(create.route)
}
