package com.fmsbeekmans.http.crud.core.instances

import com.fmsbeekmans.http.crud.core._

import scala.collection.mutable

trait SeqInstances {

  implicit def seqKeys[S <: mutable.Seq[V], V, F[_]](
      implicit Pure: Pure[F]
  ): Keys[S, Int, V, F] =
    new Keys[S, Int, V, F] {
      override def keys(seq: S): F[Seq[Int]] =
        Pure.pure(seq.indices)
    }

  implicit def seqGet[S <: mutable.Seq[V], V, F[_]](
      implicit Pure: Pure[F]
  ): Get[S, Int, V, F] =
    new Get[S, Int, V, F] {
      override def get(seq: S, key: Int): F[Option[V]] =
        Pure.pure(
          seq
            .map(Some(_))
            .applyOrElse(key, { _: Int =>
              None
            })
        )
    }

  implicit def seqPut[S <: mutable.Seq[V], V, F[_]](
      implicit Pure: Pure[F]
  ): Put[S, Int, V, F] =
    new Put[S, Int, V, F] {
      override def put(seq: S, key: Int, value: V): F[Boolean] = {
        if (seq.isDefinedAt(key)) {
          seq.update(key, value)
          Pure.pure(true)
        } else {
          Pure.pure(false)
        }
      }
    }
}
