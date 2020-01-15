package com.fmsbeekmans.http.crud.core.instances

import com.fmsbeekmans.http.crud.core._

import scala.collection.mutable

trait MapInstances {
  implicit def mapKeys[K, V, F[_]](
      implicit Pure: Pure[F]
  ): RepositoryKeys[mutable.Map[K, V], K, V, F] =
    new RepositoryKeys[mutable.Map[K, V], K, V, F] {
      override def keys(map: mutable.Map[K, V]): F[Seq[K]] =
        Pure.pure(map.keys.toList)
    }

  implicit def mapGet[K, V, F[_]](
      implicit Pure: Pure[F]
  ): RepositoryGet[mutable.Map[K, V], K, V, F] =
    new RepositoryGet[mutable.Map[K, V], K, V, F] {
      override def get(map: mutable.Map[K, V], key: K): F[Option[V]] =
        Pure.pure(map.get(key))
    }

  implicit def mapSet[K, V, F[_]](
      implicit Pure: Pure[F]
  ): RepositorySet[mutable.Map[K, V], K, V, F] =
    new RepositorySet[mutable.Map[K, V], K, V, F] {
      override def set(map: mutable.Map[K, V], key: K, value: V): F[Boolean] = {
        if (map.isDefinedAt(key)) {
          map.put(key, value)

          Pure.pure(true)
        } else {
          Pure.pure(false)
        }
      }
    }

  implicit def mapRemove[K, V, F[_]](
      implicit Pure: Pure[F]
  ) =
    new RepositoryRemove[mutable.Map[K, V], K, V, F] {
      override def remove(map: mutable.Map[K, V], key: K): F[Boolean] = {
        if (map.isDefinedAt(key)) {
          map.remove(key)

          Pure.pure(true)
        } else {
          Pure.pure(false)
        }
      }
    }
}
