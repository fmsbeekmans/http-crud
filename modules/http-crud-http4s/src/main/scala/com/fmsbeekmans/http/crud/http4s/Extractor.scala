package com.fmsbeekmans.http.crud.http4s

import cats.data.{Kleisli, OptionT}
import cats.effect.IOApp
import cats.{Applicative, MonadError}
import cats.implicits._
import com.fmsbeekmans.http.crud.core.{Pure, Repository}
import org.http4s.{
  EntityDecoder,
  QueryParamDecoder,
  QueryParamEncoder,
  QueryParameterValue,
  Request,
  Response
}

case class CRUD[Backend, K, V, F[_]](
    backend: Backend,
    path: String
)(
    implicit repository: Repository[Backend, K, V, F],
    decodeKey: QueryParamDecoder[K],
    encodeKey: QueryParamEncoder[K],
    encodeValue: EntityDecoder[F, V],
    F: MonadError[F, Throwable]
) {

  import org.http4s.dsl

  val fdsl = dsl.Http4sDsl[F]
  import fdsl._

  val o: List[Option[Int]] = List(Some(2))
  val fo: OptionT[List, Int] = OptionT(o)

  type Opt[A] = OptionT[F, A]

  def browseIds(
      implicit F: Applicative[F]
  ): Kleisli[Opt, Request[F], Seq[K]] = Kleisli[Opt, Request[F], Seq[K]] {
    case GET -> Root / `path` =>
      OptionT(repository.keys(backend).map(Option.apply))
    case _ =>
      OptionT(F.pure(None))
  }

  val toResponse: Kleisli[Opt, Seq[K], Response[F]] =
    Kleisli[Opt, Seq[K], Response[F]] { keys =>
      OptionT(Ok(keys.map(_.toString).mkString(", ")).map(Option(_)))
    }

  def browse = browseIds.andThen(toResponse)

  object Key {
    def unapply(keyString: String): Option[K] = {
      decodeKey.decode(QueryParameterValue(keyString)).toOption
    }
  }

  object Browse {
    def unapply(req: Request[F]): Option[F[Seq[K]]] = req match {
      case GET -> Root / `path` =>
        Some(repository.keys(backend))
      case _ => None
    }
  }

  object Creat {
    def unapply(req: Request[F]): Option[F[K]] = req match {
      case req @ POST -> Root / `path` =>
        Some {
          for {
            value <- req.as[V]
            id <- repository.store(backend, value)
          } yield id
        }

      case _ => None
    }
  }

  object Read {
    def unapply(req: Request[F]): Option[F[Option[V]]] = req match {
      case GET -> Root / `path` / Key(key) =>
        Some(repository.get(backend, key))
      case _ => None
    }
  }
}

object App extends IOApp {
  import cats.effect._
  import cats.implicits._
  import com.fmsbeekmans.http.crud.core.instances.bufferInstances._
  import com.fmsbeekmans.http.crud.core.instances.seqInstances._
  import org.http4s.HttpRoutes
  import org.http4s.dsl.io._
  import org.http4s.implicits._
  import org.http4s.server.blaze._

  import collection.mutable

  case class Person(id: Int, name: String)

  val people: mutable.ArrayBuffer[Person] = mutable.ArrayBuffer(
    Person(0, "")
  )

  implicit def pure[F[_]](implicit applicative: Applicative[F]): Pure[F] = {
    new Pure[F] {
      override def pure[A](value: A): F[A] = applicative.pure((value))
    }
  }

  val crud =
    CRUD[mutable.ArrayBuffer[Person], Int, Person, IO](people, "people")

  val extracedService = HttpRoutes.of[IO] {
    case crud.Browse(keys) =>
      keys.flatMap(ks => Ok(ks.map(_.toString).mkString(", ")))
  }

  type Opt[A] = OptionT[IO, A]

  val helloWorldService: HttpRoutes[IO] = crud.browse

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(extracedService.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
