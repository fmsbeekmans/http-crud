package com.fmsbeekmans.http.crud.core

trait Repository[Backend, K, V, F[_]]
    extends Get[Backend, K, V, F]
    with Store[Backend, K, V, F]
    with Set[Backend, K, V, F]
    with Remove[Backend, K, V, F]
    with Keys[Backend, K, V, F]

trait Get[Backend, K, V, F[_]] {
  def get(key: K): F[Option[V]]
}

trait Store[Backend, K, V, F[_]] {
  def store(value: V): F[K]
}

trait Set[Backend, K, V, F[_]] {
  def set(key: K, value: V): F[Unit]
}

trait Remove[Backend, K, V, F[_]] {
  def remove(key: K): F[Unit]
}

trait Keys[Backend, K, V, F[_]] {
  def keys: F[Seq[K]]
}
