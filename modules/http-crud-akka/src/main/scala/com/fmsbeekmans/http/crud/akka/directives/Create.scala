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
      backend: Backend,
      value: V
  )(
      implicit RepositoryStore: RepositoryStore[Backend, K, V, F]
  ): Directive1[F[K]] = {
    provide(RepositoryStore.store(backend, value))
  }
}
