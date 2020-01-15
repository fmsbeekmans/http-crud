package com.fmsbeekmans.http.crud.akka

import com.fmsbeekmans.http.crud.core.Id

import scala.concurrent.{ExecutionContext, Future}

trait ToFuture[F[_]] {
  def toFuture[A](fa: F[A]): Future[A]
}

object ToFuture {
  implicit val futureToFuture: ToFuture[Future] = new ToFuture[Future] {
    override def toFuture[A](fa: Future[A]): Future[A] = fa
  }

  implicit def toFuture(
      implicit executionContext: ExecutionContext
  ): ToFuture[Id] =
    new ToFuture[Id] {
      override def toFuture[A](fa: Id[A]): Future[A] = Future(fa)
    }
}
