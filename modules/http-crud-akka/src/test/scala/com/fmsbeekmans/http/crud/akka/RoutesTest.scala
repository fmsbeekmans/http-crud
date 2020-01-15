package com.fmsbeekmans.http.crud.akka

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.fmsbeekmans.http.crud.akka.ToFuture._
import com.fmsbeekmans.http.crud.core._
import com.fmsbeekmans.http.crud.core.instances.bufferInstances._
import com.fmsbeekmans.http.crud.core.instances.seqInstances._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class RouteTest
    extends AnyFreeSpec
    with Matchers
    with ScalatestRouteTest
    with Directives
    with FailFastCirceSupport {

  implicit val keyMatcher: NumberMatcher[Int] = IntNumber

  case class Person(name: String)

  implicitly[ToResponseMarshaller[Int]]

  val people = mutable.ArrayBuffer[Person]()

  val crudRoute = Routes[mutable.ArrayBuffer[Person], Int, Person, Id](people)

  "A Crud route" - {
    "Create" - {
      "Can store an entity" in {
        Post("/", Person("Ferdy")) ~> crudRoute ~> check {
          responseAs[Int] shouldBe 0
          status shouldBe StatusCodes.OK

          people(0) shouldBe Person("Ferdy")
        }
      }
    }

    "Read" - {
      "Can retrieve an entity" in {
        Get("/0") ~> crudRoute ~> check {
          responseAs[Person] shouldBe people(0)
          status shouldBe StatusCodes.OK
        }
      }

      "Can't retrieve nothing" in {
        Get("/123") ~> crudRoute ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }
    }

    "Update" - {
      "Can update an entity" in {
        Put("/0", Person("Ferdy Beekmans")) ~> crudRoute ~> check {
          people(0) shouldBe Person("Ferdy Beekmans")
          status shouldBe StatusCodes.OK
        }
      }

      "Can't create a new entity" in {
        Put("/1", Person("Ferdy Beekmans")) ~> crudRoute ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
    }

    "Remove" - {
      "Can remove an entity" in {
        Delete("/0") ~> crudRoute ~> check {
          status shouldBe StatusCodes.OK
          people shouldBe empty
        }
      }
      "Can't remove nothing" in {
        Delete("/1") ~> crudRoute ~> check {
          status shouldBe StatusCodes.NotFound
          people shouldBe empty
        }
      }
    }

    "List" - {
      "Can list empty repository" in {
        Get("/") ~> crudRoute ~> check {
          responseAs[Seq[Int]] shouldBe Seq.empty
          status shouldBe StatusCodes.OK
        }
      }

      "Can list repository with single entity" in {

        people += Person("Ferdy")

        Get("/") ~> crudRoute ~> check {
          responseAs[Seq[Int]] shouldBe Seq(0)
          status shouldBe StatusCodes.OK
        }
      }

      "Can list repository with multiple entities" in {

        people ++= Seq(
          Person("Me again"),
          Person("And again"),
        )

        Get("/") ~> crudRoute ~> check {
          responseAs[Seq[Int]] shouldBe Seq(0, 1, 2)
          status shouldBe StatusCodes.OK
        }
      }
    }
  }
}
