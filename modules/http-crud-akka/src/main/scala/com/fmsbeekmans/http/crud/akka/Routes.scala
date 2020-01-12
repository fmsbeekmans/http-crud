package com.fmsbeekmans.http.crud.akka

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.Slash./
import akka.http.scaladsl.server.util.Tuple
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import com.fmsbeekmans.http.crud.akka.directives.{
  Create,
  Delete,
  List,
  Read,
  Update
}
import com.fmsbeekmans.http.crud.core.{Get, Keys, Remove, Set, Store}

import scala.util.{Failure, Success}

object Routes {
  def create[
      Backend,
      K: ToResponseMarshaller,
      V: FromRequestUnmarshaller,
      F[_]
  ](
      repository: Store[Backend, K, V, F]
  )(
      implicit F: ToFuture[F]
  ): Route = {
    post
      .tflatMap(_ => entity(as[V]))
      .flatMap(Create.create(repository, _))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(value) => complete(value)
        case Failure(ex)    => failWith(ex)
      }
  }

  def read[
      Backend,
      K: PathMatcher1: Tuple,
      V: ToResponseMarshaller,
      F[_]
  ](
      repository: Get[Backend, K, V, F]
  )(
      implicit K: PathMatcher[K],
      F: ToFuture[F]
  ): Route = {
    get
      .tflatMap(_ => path(K))
      .tflatMap(Read.read(repository, _))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(Some(key)) => complete(key)
        case Success(None)      => complete(StatusCodes.NotFound)
        case Failure(ex)        => failWith(ex)
      }
  }

  def update[
      Backend,
      K: PathMatcher1: Tuple,
      V: FromRequestUnmarshaller,
      F[_]
  ](
      repository: Set[Backend, K, V, F]
  )(
      implicit K: PathMatcher[K],
      F: ToFuture[F]
  ): Route = {
    put
      .tflatMap { _ =>
        path(K).tflatMap { key =>
          entity(as[V]).flatMap(Update.update(repository, key, _))
        }
      }
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(_)  => complete(StatusCodes.OK)
        case Failure(ex) => failWith(ex)
      }
  }

  def delete[
      Backend,
      K: PathMatcher1,
      V,
      F[_]
  ](
      repository: Remove[Backend, K, V, F]
  )(
      implicit K: PathMatcher[K],
      F: ToFuture[F]
  ): Route = {
    path(K)
      .tflatMap(Delete.delete(repository, _))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(_)  => complete(StatusCodes.OK)
        case Failure(ex) => failWith(ex)
      }
  }

  def list[
      Backend,
      K: PathMatcher1,
      V: ToResponseMarshaller,
      F[_]
  ](
      repository: Keys[Backend, K, V, F]
  )(
      implicit F: ToFuture[F]
  ): Route = {
    get
      .tflatMap(_ => path(/))
      .tflatMap(_ => List.list(repository))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(_)  => complete(StatusCodes.OK)
        case Failure(ex) => failWith(ex)
      }
  }
}
