package com.fmsbeekmans.http.crud.core

trait Repository[K, V, F[_]]
    extends Get[K, V, F]
    with Store[K, V, F]
    with Set[K, V, F]
    with Remove[K, V, F]
    with Keys[K, V, F]

trait Get[K, V, F[_]] {
  def get(key: K): F[Option[V]]
}

trait Store[K, V, F[_]] {
  def store(value: V): F[K]
}

trait Set[K, V, F[_]] {
  def set(key: K, value: V): F[Unit]
}

trait Remove[K, V, F[_]] {
  def remove(key: K): F[Option[V]]
}

trait Keys[K, V, F[_]] {
  def keys: F[Seq[K]]
}
