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
      backend: Backend,
      key: K,
      value: V
  )(
      implicit RepositorySet: RepositorySet[Backend, K, V, F]
  ): Directive1[F[Boolean]] = {
    provide(key).flatMap { k =>
      provide(value).flatMap { v =>
        provide(RepositorySet.set(backend, k, v))
      }
    }
  }
}
