package com.fmsbeekmans.http.crud.http4s

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import com.fmsbeekmans.http.crud.core.Repository
import org.http4s.{
  EntityDecoder,
  EntityEncoder,
  QueryParamDecoder,
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
    encodeMaybeValue: EntityEncoder[F, Option[V]],
    F: MonadError[F, Throwable]
) {
  val dsl = org.http4s.dsl.Http4sDsl[F]

  type Opt[A] = OptionT[F, A]
  type Route = Kleisli[Opt, Request[F], Response[F]]

  val browse = Browse(backend, path)
  val create = Create(backend, path)
  val read = Read(backend, path)
  val update = Update(backend, path)
  val delete = Delete(backend, path)

  val route: Route = {
    Kleisli[Opt, Request[F], Response[F]] {
      case CrudResponseF(response) => OptionT(response.map(Option.apply))
      case _                       => OptionT(F.pure(None))

    }
  }

  object CrudResponseF {
    def unapply(req: Request[F]): Option[F[Response[F]]] = req match {
      case browse.BrowseResponseF(response) => Some(response)
      case create.CreateResponseF(response) => Some(response)
      case read.ReadResponseF(response)     => Some(response)
      case update.UpdateResponseF(response) => Some(response)
      case delete.DeleteResponseF(response) => Some(response)
    }
  }
}
