package com.fmsbeekmans.http.crud.akka.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.fmsbeekmans.http.crud.core._

object List extends List

trait List {
  def list[
      Backend,
      K,
      V,
      F[_]
  ](
      repository: Keys[Backend, K, V, F]
  ): Directive1[F[Seq[K]]] = {
    provide(repository.keys)
  }
}
