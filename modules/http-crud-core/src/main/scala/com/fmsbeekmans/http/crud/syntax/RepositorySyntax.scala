package com.fmsbeekmans.http.crud.syntax

import com.fmsbeekmans.http.crud.core._

trait RepositorySyntax {
  implicit class GetSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryGet: Get[Backend, K, V, F]
  ) {
    def get(key: K): F[Option[V]] = RepositoryGet.get(backend, key)
  }

  implicit class KeysSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryKeys: Keys[Backend, K, V, F]
  ) {
    def keys: F[Seq[K]] = RepositoryKeys.keys(backend)
  }

  implicit class RemoveSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryRemove: Remove[Backend, K, V, F]
  ) {
    def remove(key: K): F[Boolean] = RepositoryRemove.remove(backend, key)
  }

  implicit class PutSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryPut: Put[Backend, K, V, F]
  ) {
    def put(key: K, value: V): F[Boolean] =
      RepositoryPut.put(backend, key, value)
  }

  implicit class StoreSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryStore: Store[Backend, K, V, F]
  ) {
    def store(value: V): F[K] = RepositoryStore.store(backend, value)
  }
}
