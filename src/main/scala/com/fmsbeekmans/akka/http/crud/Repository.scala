package com.fmsbeekmans.akka.http.crud

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.util.Tuple

import scala.util.{Failure, Success}
import spray.json._

import scala.concurrent.Future

trait Repository[K, V, F[_]] {

  def keys: F[List[K]]

  def get(key: K): F[Option[V]]

  def store(value: V): F[K]

  def set(key: K, value: V): F[Unit]

  def remove(key: K): F[Option[V]]

}

trait ToFuture[F[_]] {
  def toFuture[A](fa: F[A]): Future[A]
}

object CrudDirectives {
  def create[K, V, F[_]](
      repository: Repository[K, V, F],
      value: V
  ): Directive1[F[K]] = {
    provide(value).flatMap { v =>
      provide(repository.store(value))
    }
  }

  def read[K, V, F[_]](
      repository: Repository[K, V, F],
      key: K
  ): Directive1[F[Option[V]]] = {
    provide(key).flatMap { k =>
      provide(repository.get(k))
    }
  }

  def update[K, V, F[_]](
      repository: Repository[K, V, F],
      key: K,
      value: V
  ): Directive1[F[Unit]] = {
    provide(key).flatMap { k =>
      provide(value).flatMap { v =>
        provide(repository.set(k, v))
      }
    }
  }

  def delete[K, V, F[_]](
      repository: Repository[K, V, F],
      key: K
  ): Directive1[F[Option[V]]] = {
    provide(key).flatMap { k =>
      provide(repository.remove(k))
    }
  }

  def list[K, V, F[_]](
      repository: Repository[K, V, F]
  ): Directive1[F[List[K]]] = {
    provide(repository.keys)
  }
}

object Routes extends SprayJsonSupport {
  def create[K: RootJsonFormat, V: RootJsonFormat, F[_]](
      repository: Repository[K, V, F]
  )(
      implicit F: ToFuture[F]
  ): Route = {
    post
      .tflatMap(_ => entity(as[V]))
      .flatMap(CrudDirectives.create(repository, _))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(value) => complete(value)
        case Failure(ex)    => failWith(ex)
      }
  }

  def read[K: PathMatcher1: Tuple, V: RootJsonFormat, F[_]](
      repository: Repository[K, V, F]
  )(
      implicit K: PathMatcher[K],
      F: ToFuture[F]
  ): Route = {
    get
      .tflatMap(_ => path(K))
      .tflatMap(CrudDirectives.read(repository, _))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(Some(key)) => complete(key)
        case Success(None)      => complete(StatusCodes.NotFound)
        case Failure(ex)        => failWith(ex)
      }
  }

  def update[K: PathMatcher1: Tuple, V: RootJsonFormat, F[_]](
      repository: Repository[K, V, F]
  )(
      implicit K: PathMatcher[K],
      F: ToFuture[F]
  ): Route = {
    put
      .tflatMap { _ =>
        path(K)
          .tflatMap { key``` =>
            entity(as[V]).flatMap(CrudDirectives.update(repository, key, _))
          }
      }
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(_)  => complete(StatusCodes.OK)
        case Failure(ex) => failWith(ex)
      }
  }

  def delete[K: PathMatcher1, V: RootJsonFormat, F[_]](
      repository: Repository[K, V, F]
  )(
      implicit K: PathMatcher[K],
      F: ToFuture[F]
  ): Route = {
    path(K)
      .tflatMap(CrudDirectives.delete(repository, _))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(_)  => complete(StatusCodes.OK)
        case Failure(ex) => failWith(ex)
      }
  }
}
