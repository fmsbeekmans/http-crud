package com.fmsbeekmans.http.crud.akka.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.fmsbeekmans.http.crud.core._

object Delete extends Delete

trait Delete {
  def delete[
      Backend,
      K,
      V,
      F[_]
  ](
      backend: Backend,
      key: K
  )(
      implicit RepositoryRemove: RepositoryRemove[Backend, K, V, F]
  ): Directive1[F[Boolean]] = {
    provide(key).flatMap { k =>
      provide(RepositoryRemove.remove(backend, k))
    }
  }
}
