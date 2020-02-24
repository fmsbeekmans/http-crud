package com.fmsbeekmans.http.crud.cats

import cats.Applicative
import com.fmsbeekmans.http.crud.core.Pure

object instances extends instances

trait instances {
  implicit def pure[F[_]](implicit applicative: Applicative[F]): Pure[F] = {
    new Pure[F] {
      override def pure[A](value: A): F[A] = applicative.pure((value))
    }
  }
}
