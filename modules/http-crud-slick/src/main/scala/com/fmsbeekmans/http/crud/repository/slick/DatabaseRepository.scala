package com.fmsbeekmans.http.crud.repository.slick

import com.fmsbeekmans.http.crud.core.Repository
import slick.jdbc.JdbcProfile
import slick.lifted.Rep

import scala.concurrent.{ExecutionContext, Future}

case class DatabaseRepository[Driver <: JdbcProfile](val driver: Driver) {
  def repository[V, Table <: driver.api.Table[V]](
      db: driver.api.Database,
      table: driver.api.TableQuery[Table],
      toId: Table => Rep[Option[Int]]
  )(
      implicit ec: ExecutionContext
  ): Repository[driver.api.Database, Int, V, Future] = {
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
            (table returning table.map(toId)) += value
          )
          .map {
            case Some(id) => id
            case None     => throw new Exception("")
          }
      }
    }
  }
}
