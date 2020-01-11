package com.fmsbeekmans.http.crud.akka.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.fmsbeekmans.http.crud.core._

object Create extends Create

trait Create {
  def create[
      Backend,
      K,
      V,
      F[_]
  ](
      repository: Store[Backend, K, V, F],
      value: V
  ): Directive1[F[K]] = {
    provide(value).flatMap { v =>
      provide(repository.store(value))
    }
  }
}
