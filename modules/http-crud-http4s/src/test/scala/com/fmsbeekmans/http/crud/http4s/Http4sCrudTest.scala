package com.fmsbeekmans.http.crud.http4s

import cats.effect.IO
import com.fmsbeekmans.http.crud.core.instances.bufferInstances._
import com.fmsbeekmans.http.crud.core.instances.seqInstances._
import com.fmsbeekmans.http.crud.cats.instances._
import io.circe.generic.auto._
import org.http4s.circe.{CirceEntityDecoder, jsonEncoderOf, jsonOf}
import org.http4s.{HttpRoutes, Method, Request, Status, Uri}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class Http4sCrudTest
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with CirceEntityDecoder {
  import collection.mutable

  case class Person(name: String)

  implicit val intJsonEncoder = jsonEncoderOf[IO, Int]
  implicit val intsJsonEncoder = jsonEncoderOf[IO, Seq[Int]]
  implicit val personJson = jsonOf[IO, Person]
  implicit val personEncoderJson = jsonEncoderOf[IO, Person]
  implicit val maybePersonJsonEncoder = jsonEncoderOf[IO, Option[Person]]
  implicit val intJson = jsonOf[IO, Int]

  "A Crud route" - {
    val people: mutable.ArrayBuffer[Person] = mutable.ArrayBuffer(
      Person("Anton"),
      Person("Bea")
    )

    val crud =
      Crud[mutable.ArrayBuffer[Person], Int, Person, IO](people, "people")

    val crudRoute = HttpRoutes.of[IO] {
      case crud.CrudResponseF(resp) => resp
    }

    "Handles Browse request" in {
      val browseRequest = Request[IO](
        method = Method.GET,
        uri = Uri(path = "/people")
      )

      val keys =
        crudRoute
          .run(browseRequest)
          .value
          .unsafeRunSync()
          .value
          .as[Seq[Int]]
          .unsafeRunSync()

      keys.toList shouldBe people.indices.toList
    }

    "Handles a Create request" - {
      "when defined" in {
        val createRequest = Request[IO](
          method = Method.POST,
          uri = Uri(path = "/people")
        ).withEntity(Person("New Guy"))

        val response =
          crudRoute
            .run(createRequest)
            .value
            .unsafeRunSync()
            .value

        response.status shouldBe Status.Ok
        response.as[Int].unsafeRunSync() shouldBe 2
        people(2) shouldBe Person("New Guy")
      }
    }

    "Handles a Read request" - {
      "when defined" in {
        val readRequest = Request[IO](
          method = Method.GET,
          uri = Uri(path = "/people/1")
        )

        val person =
          crudRoute
            .run(readRequest)
            .value
            .unsafeRunSync()
            .value
            .as[Person]
            .unsafeRunSync()

        person shouldBe people(1)
      }

      "when undefined" in {
        val readRequest = Request[IO](
          method = Method.GET,
          uri = Uri(path = "/people/4")
        )

        val response =
          crudRoute
            .run(readRequest)
            .value
            .unsafeRunSync()
            .value

        response.status shouldBe Status.NotFound
      }
    }

    "Handles an Update request" - {
      "when defined" in {
        val updateRequest = Request[IO](
          method = Method.PUT,
          uri = Uri(path = "/people/0")
        ).withEntity(Person("Tony"))

        val response =
          crudRoute
            .run(updateRequest)
            .value
            .unsafeRunSync()
            .value

        response.status shouldBe Status.Ok
        people(0).name shouldBe "Tony"
      }

      "when undefined" in {
        val updateRequest = Request[IO](
          method = Method.PUT,
          uri = Uri(path = "/people/4")
        ).withEntity(Person("Lost"))

        val response =
          crudRoute
            .run(updateRequest)
            .value
            .unsafeRunSync()
            .value

        response.status shouldBe Status.NotFound
      }

      "with invalid json" in {
        val updateRequest = Request[IO](
          method = Method.PUT,
          uri = Uri(path = "/people/4")
        ).withEntity("Not a person")

        val response =
          crudRoute
            .run(updateRequest)
            .value
            .unsafeRunSync()
            .value

        response.status shouldBe Status.BadRequest
      }
    }

    "Handles a Delete request" - {
      "when defined" in {
        val deleteRequest = Request[IO](
          method = Method.DELETE,
          uri = Uri(path = "/people/0")
        )

        val response =
          crudRoute
            .run(deleteRequest)
            .value
            .unsafeRunSync()
            .value

        response.status shouldBe Status.Ok
        people(0).name shouldBe "Bea"
      }

      "when undefined" in {
        val deleteRequest = Request[IO](
          method = Method.DELETE,
          uri = Uri(path = "/people/4")
        )

        val response =
          crudRoute
            .run(deleteRequest)
            .value
            .unsafeRunSync()
            .value

        response.status shouldBe Status.NotFound
      }
    }
  }
}
