package com.fmsbeekmans.http.crud.akka

import scala.concurrent.Future

trait ToFuture[F[_]] {
  def toFuture[A](fa: F[A]): Future[A]
}
