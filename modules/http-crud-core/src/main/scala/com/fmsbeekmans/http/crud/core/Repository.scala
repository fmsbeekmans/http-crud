package com.fmsbeekmans.http.crud.core

trait Repository[Backend, K, V, F[_]]
    extends Get[Backend, K, V, F]
    with Store[Backend, K, V, F]
    with Put[Backend, K, V, F]
    with Remove[Backend, K, V, F]
    with Keys[Backend, K, V, F]

object Repository {
  implicit def ev[
      GetBackend >: Backend,
      StoreBackend >: Backend,
      PutBackend >: Backend,
      RemoveBackend >: Backend,
      KeysBackend >: Backend,
      Backend,
      K,
      V,
      F[_]
  ](
      implicit Get: Get[GetBackend, K, V, F],
      Store: Store[StoreBackend, K, V, F],
      Put: Put[PutBackend, K, V, F],
      Remove: Remove[RemoveBackend, K, V, F],
      Keys: Keys[KeysBackend, K, V, F]
  ): Repository[Backend, K, V, F] =
    new Repository[Backend, K, V, F] {
      override def store(backend: Backend, value: V): F[K] =
        Store.store(backend, value)

      override def put(backend: Backend, key: K, value: V): F[Boolean] =
        Put.put(backend, key, value)

      override def remove(backend: Backend, key: K): F[Boolean] =
        Remove.remove(backend, key)

      override def get(backend: Backend, key: K): F[Option[V]] =
        Get.get(backend, key)

      override def keys(backend: Backend): F[Seq[K]] =
        Keys.keys(backend)
    }
}

trait Get[Backend, K, V, F[_]] {
  def get(backend: Backend, key: K): F[Option[V]]
}

trait Store[Backend, K, V, F[_]] {
  def store(backend: Backend, value: V): F[K]
}

trait Put[Backend, K, V, F[_]] {
  def put(backend: Backend, key: K, value: V): F[Boolean]
}

trait Remove[Backend, K, V, F[_]] {
  def remove(backend: Backend, key: K): F[Boolean]
}

trait Keys[Backend, K, V, F[_]] {
  def keys(backend: Backend): F[Seq[K]]
}
