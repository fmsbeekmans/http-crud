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
      repository: Remove[Backend, K, V, F],
      key: K
  ): Directive1[F[Unit]] = {
    provide(key).flatMap { k =>
      provide(repository.remove(k))
    }
  }
}
