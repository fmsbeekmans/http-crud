package com.fmsbeekmans.http.crud.core

trait Repository[Backend, K, V, F[_]]
    extends RGet[Backend, K, V, F]
    with RStore[Backend, K, V, F]
    with RSet[Backend, K, V, F]
    with RRemove[Backend, K, V, F]
    with RKeys[Backend, K, V, F]

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
      implicit RGet: RGet[BGet, K, V, F],
      RStore: RStore[BStore, K, V, F],
      RSet: RSet[BSet, K, V, F],
      RRemove: RRemove[BRemove, K, V, F],
      RKeys: RKeys[BKeys, K, V, F]
  ): Repository[Backend, K, V, F] =
    new Repository[Backend, K, V, F] {
      override def store(backend: Backend, value: V): F[K] =
        RStore.store(backend, value)

      override def set(backend: Backend, key: K, value: V): F[Boolean] =
        RSet.set(backend, key, value)

      override def remove(backend: Backend, key: K): F[Boolean] =
        RRemove.remove(backend, key)

      override def get(backend: Backend, key: K): F[Option[V]] =
        RGet.get(backend, key)

      override def keys(backend: Backend): F[Seq[K]] =
        RKeys.keys(backend)
    }
}

trait RGet[Backend, K, V, F[_]] {
  def get(backend: Backend, key: K): F[Option[V]]
}

trait RStore[Backend, K, V, F[_]] {
  def store(backend: Backend, value: V): F[K]
}

trait RSet[Backend, K, V, F[_]] {
  def set(backend: Backend, key: K, value: V): F[Boolean]
}

trait RRemove[Backend, K, V, F[_]] {
  def remove(backend: Backend, key: K): F[Boolean]
}

trait RKeys[Backend, K, V, F[_]] {
  def keys(backend: Backend): F[Seq[K]]
}
