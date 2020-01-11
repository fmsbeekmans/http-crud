package com.fmsbeekmans.http.crud.akka.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.fmsbeekmans.http.crud.core._

object Update extends Update

trait Update {
  def update[
      Backend,
      K,
      V,
      F[_]
  ](
      repository: Set[Backend, K, V, F],
      key: K,
      value: V
  ): Directive1[F[Unit]] = {
    provide(key).flatMap { k =>
      provide(value).flatMap { v =>
        provide(repository.set(k, v))
      }
    }
  }
}
