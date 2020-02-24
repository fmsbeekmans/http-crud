package com.fmsbeekmans.http.crud.http4s

import cats.Applicative
import cats.data.OptionT
import cats.effect.{IOApp, _}
import cats.implicits._
import com.fmsbeekmans.http.crud.core.Pure
import com.fmsbeekmans.http.crud.core.instances.bufferInstances._
import com.fmsbeekmans.http.crud.core.instances.seqInstances._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.server.blaze._

object App extends IOApp {

  import collection.mutable

  case class Person(name: String)

  val people: mutable.ArrayBuffer[Person] = mutable.ArrayBuffer(
    Person("A"),
    Person("B")
  )

  implicit def pure[F[_]](implicit applicative: Applicative[F]): Pure[F] = {
    new Pure[F] {
      override def pure[A](value: A): F[A] = applicative.pure((value))
    }
  }

  implicit val intJsonEncoder = jsonEncoderOf[IO, Int]
  implicit val intsJsonEncoder = jsonEncoderOf[IO, Seq[Int]]
  implicit val personJson = jsonOf[IO, Person]
  implicit val personEncoderJson = jsonEncoderOf[IO, Person]
  implicit val maybePersonJsonEncoder = jsonEncoderOf[IO, Option[Person]]
  implicit val intJson = jsonOf[IO, Int]

  val crud =
    Crud[mutable.ArrayBuffer[Person], Int, Person, IO](people, "people")

  val extracedService = HttpRoutes.of[IO] {
    case crud.CrudResponseF(resp) => resp
  }

  type Opt[A] = OptionT[IO, A]

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(extracedService.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
