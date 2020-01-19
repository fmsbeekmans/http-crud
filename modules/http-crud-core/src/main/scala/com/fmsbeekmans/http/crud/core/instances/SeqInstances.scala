package com.fmsbeekmans.http.crud.core.instances

import com.fmsbeekmans.http.crud.core._

import scala.collection.mutable

trait SeqInstances {

  implicit def seqKeys[S <: mutable.Seq[V], V, F[_]](
      implicit Pure: Pure[F]
  ): RKeys[S, Int, V, F] =
    new RKeys[S, Int, V, F] {
      override def keys(seq: S): F[Seq[Int]] =
        Pure.pure(seq.indices)
    }

  implicit def seqGet[S <: mutable.Seq[V], V, F[_]](
      implicit Pure: Pure[F]
  ): RGet[S, Int, V, F] =
    new RGet[S, Int, V, F] {
      override def get(seq: S, key: Int): F[Option[V]] =
        Pure.pure(
          seq
            .map(Some(_))
            .applyOrElse(key, { _: Int =>
              None
            })
        )
    }

  implicit def seqSet[S <: mutable.Seq[V], V, F[_]](
      implicit Pure: Pure[F]
  ): RSet[S, Int, V, F] =
    new RSet[S, Int, V, F] {
      override def set(seq: S, key: Int, value: V): F[Boolean] = {
        if (seq.isDefinedAt(key)) {
          seq.update(key, value)
          Pure.pure(true)
        } else {
          Pure.pure(false)
        }
      }
    }
}
