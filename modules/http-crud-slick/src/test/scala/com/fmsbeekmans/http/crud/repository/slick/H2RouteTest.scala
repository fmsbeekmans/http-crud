package com.fmsbeekmans.http.crud.repository.slick

import org.scalatest.{BeforeAndAfterAll, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class H2RouteTest
    extends AnyFreeSpec
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures
    with OptionValues {

  import H2Profile.api._

  case class Person(id: Option[Int], name: String)

  class People(tag: Tag) extends Table[Person](tag, "people") {

    def id: Rep[Option[Int]] =
      column[Option[Int]](
        "id",
        O.PrimaryKey,
        O.AutoInc
      )

    def name: Rep[String] = column[String]("name")

    override def * = ((id, name)) <> (Person.tupled, Person.unapply)
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val database =
    Database.forURL(
      "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
      driver = "org.h2.Driver"
    )
  lazy val table = TableQuery[People]

  val repository = DatabaseRepository(H2Profile)
    .repository[Person, People](database, table, _.id)

  "A DatabaseRepository" - {
    "can store an entry" in {
      whenReady(repository.store(Person(None, "testValue"))) { id =>
        id shouldBe 1
      }

      ()
    }

    "can retrieve an entry" in {
      whenReady(repository.get(1)) { result =>
        result.value shouldBe Person(Some(1), "testValue")
      }

      ()
    }

    "can update an entry" in {
      whenReady {
        for {
          _ <- repository.set(1, Person(Some(1), "Test Value"))
          updated <- repository.get(1)
        } yield updated
      } { result =>
        result.value shouldBe Person(Some(1), "Test Value")
      }

      ()
    }

    "can list contained entries" in {
      whenReady {
        for {
          _ <- repository.store(Person(None, "Extra Value"))
          keys <- repository.keys
        } yield keys
      } { result =>
        result should contain(1)
        result should contain(2)
      }

      ()
    }

    "can remove an entry" in {
      whenReady {
        for {
          _ <- repository.remove(1)
          removed <- repository.get(1)
        } yield removed
      } { _ shouldBe None }

      ()
    }
  }

  override def beforeAll(): Unit = {
    Await.ready(
      database.run(table.schema.create),
      Duration.Inf
    )

    ()
  }
}
