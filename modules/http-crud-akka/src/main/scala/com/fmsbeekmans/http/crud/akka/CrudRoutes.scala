package com.fmsbeekmans.http.crud.akka

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher1
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import com.fmsbeekmans.http.crud.core._

import scala.util.{Failure, Success}

object CrudRoutes {

  def apply[
      Backend,
      K: FromRequestUnmarshaller: ToResponseMarshaller: PathMatcher1,
      V: FromRequestUnmarshaller: ToResponseMarshaller,
      F[_]
  ](
      repository: Backend
  )(
      implicit Repository: Repository[Backend, K, V, F],
      KS: ToResponseMarshaller[Seq[K]],
      F: ToFuture[F]
  ): server.Route = {
    create[Backend, K, V, F](repository) ~
      read[Backend, K, V, F](repository) ~
      update[Backend, K, V, F](repository) ~
      delete[Backend, K, V, F](repository) ~
      list[Backend, K, V, F](repository)
  }

  def create[
      Backend,
      K: FromRequestUnmarshaller: ToResponseMarshaller,
      V: FromRequestUnmarshaller,
      F[_]
  ](
      repository: Backend
  )(
      implicit RStore: RStore[Backend, K, V, F],
      F: ToFuture[F]
  ): server.Route = {
    post
      .tflatMap(_ => entity(as[V]))
      .flatMap(CrudDirectives[Backend, K, V, F].create(repository, _))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(value) => complete(value)
        case Failure(ex)    => failWith(ex)
      }
  }

  def read[
      Backend,
      K: FromRequestUnmarshaller,
      V: ToResponseMarshaller,
      F[_]
  ](
      repository: Backend
  )(
      implicit RGet: RGet[Backend, K, V, F],
      KeyPathMatcher: PathMatcher1[K],
      F: ToFuture[F]
  ): server.Route = {

    get
      .tflatMap(_ => path(KeyPathMatcher))
      .flatMap(CrudDirectives[Backend, K, V, F].read(repository, _))
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
      K: FromRequestUnmarshaller,
      V: FromRequestUnmarshaller,
      F[_]
  ](
      repository: Backend
  )(
      implicit RSet: RSet[Backend, K, V, F],
      KeyPathMatcher: PathMatcher1[K],
      F: ToFuture[F]
  ): server.Route = {
    put
      .tflatMap { _ =>
        path(KeyPathMatcher)
          .flatMap { key =>
            entity(as[V]).flatMap(
              CrudDirectives[Backend, K, V, F].update(repository, key, _))
          }
      }
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(true)  => complete(StatusCodes.OK)
        case Success(false) => complete(StatusCodes.NotFound)
        case Failure(ex)    => failWith(ex)
      }
  }

  def delete[
      Backend,
      K: FromRequestUnmarshaller,
      V,
      F[_]
  ](
      repository: Backend
  )(
      implicit RRemove: RRemove[Backend, K, V, F],
      KeyPathMatcher: PathMatcher1[K],
      F: ToFuture[F]
  ): server.Route = {
    path(KeyPathMatcher)
      .flatMap(CrudDirectives[Backend, K, V, F].delete(repository, _))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(true)  => complete(StatusCodes.OK)
        case Success(false) => complete(StatusCodes.NotFound)
        case Failure(ex)    => failWith(ex)
      }
  }

  def list[
      Backend,
      K,
      V,
      F[_]
  ](
      repository: Backend
  )(
      implicit RKeys: RKeys[Backend, K, V, F],
      KS: ToResponseMarshaller[Seq[K]],
      F: ToFuture[F]
  ): server.Route = {
    get
      .tflatMap(_ => pathEndOrSingleSlash)
      .tflatMap(_ => CrudDirectives[Backend, K, V, F].list(repository))
      .map(F.toFuture)
      .flatMap(onComplete(_))
      .apply {
        case Success(keys) => complete(keys)
        case Failure(ex)   => failWith(ex)
      }
  }
}
