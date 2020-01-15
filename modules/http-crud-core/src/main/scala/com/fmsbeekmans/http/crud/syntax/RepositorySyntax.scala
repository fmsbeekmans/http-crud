package com.fmsbeekmans.http.crud.syntax

import com.fmsbeekmans.http.crud.core._

trait RepositorySyntax {
  implicit class GetSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryGet: RepositoryGet[Backend, K, V, F]
  ) {
    def get(key: K): F[Option[V]] = RepositoryGet.get(backend, key)
  }

  implicit class KeysSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryKeys: RepositoryKeys[Backend, K, V, F]
  ) {
    def keys: F[Seq[K]] = RepositoryKeys.keys(backend)
  }

  implicit class RemoveSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryRemove: RepositoryRemove[Backend, K, V, F]
  ) {
    def remove(key: K): F[Boolean] = RepositoryRemove.remove(backend, key)
  }

  implicit class SetSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositorySet: RepositorySet[Backend, K, V, F]
  ) {
    def set(key: K, value: V): F[Boolean] =
      RepositorySet.set(backend, key, value)
  }

  implicit class StoreSyntax[Backend, K, V, F[_]](
      backend: Backend
  )(
      implicit RepositoryStore: RepositoryStore[Backend, K, V, F]
  ) {
    def store(value: V): F[K] = RepositoryStore.store(backend, value)
  }
}
