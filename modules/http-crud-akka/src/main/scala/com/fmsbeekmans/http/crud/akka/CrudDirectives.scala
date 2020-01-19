package com.fmsbeekmans.http.crud.akka

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.fmsbeekmans.http.crud.core._

trait CrudDirectives[Backend, K, V, F[_]] {
  def create(
      backend: Backend,
      value: V
  )(
      implicit RepositoryStore: RStore[Backend, K, V, F]
  ): Directive1[F[K]] = {
    provide(RepositoryStore.store(backend, value))
  }

  def read(
      backend: Backend,
      key: K
  )(
      implicit RepositoryGet: RGet[Backend, K, V, F],
  ): Directive1[F[Option[V]]] = {
    provide(key).flatMap { k =>
      provide(RepositoryGet.get(backend, k))
    }
  }

  def update(
      backend: Backend,
      key: K,
      value: V
  )(
      implicit RepositorySet: RSet[Backend, K, V, F]
  ): Directive1[F[Boolean]] = {
    provide(key).flatMap { k =>
      provide(value).flatMap { v =>
        provide(RepositorySet.set(backend, k, v))
      }
    }
  }

  def delete(
      backend: Backend,
      key: K
  )(
      implicit RepositoryRemove: RRemove[Backend, K, V, F]
  ): Directive1[F[Boolean]] = {
    provide(key).flatMap { k =>
      provide(RepositoryRemove.remove(backend, k))
    }
  }

  def list(
      backend: Backend
  )(
      implicit RepositoryKeys: RKeys[Backend, K, V, F]
  ): Directive1[F[Seq[K]]] = {
    provide(RepositoryKeys.keys(backend))
  }
}

object CrudDirectives {
  def apply[Backend, K, V, F[_]]: CrudDirectives[Backend, K, V, F] =
    new CrudDirectives[Backend, K, V, F] {}
}
