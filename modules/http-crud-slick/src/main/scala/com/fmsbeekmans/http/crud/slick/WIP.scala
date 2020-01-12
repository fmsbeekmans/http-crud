package com.fmsbeekmans.http.crud.slick

import com.fmsbeekmans.http.crud.core.Repository
import slick.jdbc.JdbcProfile
//import slick.jdbc.H2Profile.api._
import slick.lifted.Rep

import scala.concurrent.{ExecutionContext, Future}

class App[Driver <: JdbcProfile](val driver: Driver) {
  def repository[V](
      db: driver.api.Database,
      table: driver.api.TableQuery[driver.api.Table[V]],
      toId: driver.api.Table[V] => Rep[Option[Int]]
  )(
      implicit ec: ExecutionContext
  ) = {
    import driver.api._

    new Repository[Database, Int, V, Future] {
      override def keys: Future[Seq[Int]] = {
        db.run(
            table
              .map(toId)
              .result
          )
          .map(_.flatten)
      }

      override def get(id: Int): Future[Option[V]] = {
        db.run(table.filter(table => toId(table) === id).result.headOption)
      }
      override def remove(id: Int): Future[Unit] = {
        db.run(
            table
              .filter(table => toId(table) === id)
              .delete
          )
          .map(_ => ())
      }

      override def set(id: Int, value: V): Future[Unit] = {
        db.run(
            table
              .filter(table => toId(table) === id)
              .update(value)
          )
          .map(_ => ())
      }

      override def store(value: V): Future[Int] = {
        db.run(
            (table returning table.map(toId)) += (value)
          )
          .map {
            case Some(id) => id
            case None     => throw new Exception("")
          }
      }
    }
  }
}
