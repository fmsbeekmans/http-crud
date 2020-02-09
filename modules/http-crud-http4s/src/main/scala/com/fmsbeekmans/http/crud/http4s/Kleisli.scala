package com.fmsbeekmans.http.crud.http4s

import cats.data.Kleisli
import cats.data.OptionT

object Kl {

  val a: Kleisli[Option, String, Int] = Kleisli { s: String =>
    Some(s.length)
  }

  type O[A] = OptionT[List, A]

  val at: Kleisli[O, String, Int] = Kleisli { s: String =>
    OptionT[List, Int](List(Some(s.length)))
  }

}
