package com.fmsbeekmans.http.crud.core

trait Pure[F[_]] {
  def pure[A](value: A): F[A]
}

object Pure {
  implicit val idPure: Pure[Id] = new Pure[Id] {
    override def pure[A](value: A): Id[A] = value
  }
}
