package com.fmsbeekmans.http.crud.akka.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.fmsbeekmans.http.crud.core._

object Read extends Read

trait Read {
  def read[
      Backend,
      K,
      V,
      F[_]
  ](
      repository: Get[Backend, K, V, F],
      key: K
  ): Directive1[F[Option[V]]] = {
    provide(key).flatMap { k =>
      provide(repository.get(k))
    }
  }
}
