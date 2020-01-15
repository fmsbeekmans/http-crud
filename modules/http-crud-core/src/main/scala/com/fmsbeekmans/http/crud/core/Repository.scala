package com.fmsbeekmans.http.crud.core

trait Repository[Backend, K, V, F[_]]
    extends RepositoryGet[Backend, K, V, F]
    with RepositoryStore[Backend, K, V, F]
    with RepositorySet[Backend, K, V, F]
    with RepositoryRemove[Backend, K, V, F]
    with RepositoryKeys[Backend, K, V, F]

object Repository {
  implicit def ev[
      BGet >: Backend,
      BStore >: Backend,
      BSet >: Backend,
      BRemove >: Backend,
      BKeys >: Backend,
      Backend,
//      Backend <: BGet with BStore with BSet with BRemove with BKeys,
      K,
      V,
      F[_]
  ](
      implicit RepositoryGet: RepositoryGet[BGet, K, V, F],
      RepositoryStore: RepositoryStore[BStore, K, V, F],
      RepositorySet: RepositorySet[BSet, K, V, F],
      RepositoryRemove: RepositoryRemove[BRemove, K, V, F],
      RepositoryKeys: RepositoryKeys[BKeys, K, V, F]
  ): Repository[Backend, K, V, F] =
    new Repository[Backend, K, V, F] {
      override def store(backend: Backend, value: V): F[K] =
        RepositoryStore.store(backend, value)

      override def set(backend: Backend, key: K, value: V): F[Boolean] =
        RepositorySet.set(backend, key, value)

      override def remove(backend: Backend, key: K): F[Boolean] =
        RepositoryRemove.remove(backend, key)

      override def get(backend: Backend, key: K): F[Option[V]] =
        RepositoryGet.get(backend, key)

      override def keys(backend: Backend): F[Seq[K]] =
        RepositoryKeys.keys(backend)
    }
}

trait RepositoryGet[Backend, K, V, F[_]] {
  def get(backend: Backend, key: K): F[Option[V]]
}

trait RepositoryStore[Backend, K, V, F[_]] {
  def store(backend: Backend, value: V): F[K]
}

trait RepositorySet[Backend, K, V, F[_]] {
  def set(backend: Backend, key: K, value: V): F[Boolean]
}

trait RepositoryRemove[Backend, K, V, F[_]] {
  def remove(backend: Backend, key: K): F[Boolean]
}

trait RepositoryKeys[Backend, K, V, F[_]] {
  def keys(backend: Backend): F[Seq[K]]
}
