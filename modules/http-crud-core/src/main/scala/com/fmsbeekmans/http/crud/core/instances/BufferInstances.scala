package com.fmsbeekmans.http.crud.core.instances

import com.fmsbeekmans.http.crud.core._

import scala.collection.mutable

trait BufferInstances {
  implicit def bufferStore[B <: mutable.Buffer[V], V, F[_]](
      implicit Pure: Pure[F]
  ): RepositoryStore[B, Int, V, F] =
    new RepositoryStore[B, Int, V, F] {
      override def store(buffer: B, value: V): F[Int] = {
        val n = buffer.length
        buffer.append(value)

        Pure.pure(n)
      }
    }

  implicit def bufferRemove[B <: mutable.Buffer[V], V, F[_]](
      implicit Pure: Pure[F]
  ): RepositoryRemove[B, Int, V, F] =
    new RepositoryRemove[B, Int, V, F] {
      override def remove(buffer: B, key: Int): F[Boolean] = {
        if (buffer.isDefinedAt(key)) {
          buffer.remove(key)

          Pure.pure(true)
        } else {
          Pure.pure(false)
        }
      }
    }
}
