# http-crud

This library is meant to help expose data from different sources as an API.

## Install

To use the library add the following to your project settings

```scala
object versions {
  val `http-curd` = 0.1.0
}

libraryDependencies ++= Seq(
  "com.fmsbeekmans" %% "http-crud" % versions.`http-crud-akka`,
  "com.fmsbeekmans" %% "http-crud" % versions.`http-crud-core`,
  "com.fmsbeekmans" %% "http-crud" % versions.`http-crud-slick`
)
```

## Modules

The project has been split into modules to limit unwanted transitive dependencies.
These are the available modules and their description

| module             | description                                                      |
|--------------------|------------------------------------------------------------------|
| `http-crud-core`   | Core abstractions and typeclasses with instances for collections |
| `http-crud-akka`   | Routes and directives to expose a repository through `akka-http` |
| `http-crud-slick`  | Wrappers to create a Repository out of a `slick` Table           |

### Repository

The central typeclass is a `Repository`.
A repository is a (connection to) a datastructure with crud capabilities create, read, update delete and listing the contained items.

In case not all the operations are supported each of these are separate typeclasses

```scala
trait Get[Backend, K, V, F[_]] {
  def get(backend: Backend, key: K): F[Option[V]]
}

trait Store[Backend, K, V, F[_]] {
  def store(backend: Backend, value: V): F[K]
}

trait Put[Backend, K, V, F[_]] {
  def put(backend: Backend, key: K, value: V): F[Boolean]
}

trait Remove[Backend, K, V, F[_]] {
  def remove(backend: Backend, key: K): F[Boolean]
}

trait Keys[Backend, K, V, F[_]] {
  def keys(backend: Backend): F[Seq[K]]
}
```

The core package includes instances for mutable datastructures

| type             | instances                                    | notes                                                                                   |
|------------------|----------------------------------------------|-----------------------------------------------------------------------------------------|
| `mutable.Seq`    | `Get`, `Put`, `Keys`                      | Only implemented with Int as key                                                        |
| `mutable.Buffer` | `Get`, `Store`, `Put`, `Remove`, `Keys` | Only implemented with Int as key, `Get`, `Put` & `Keys` inherited from `mutable.Seq` |
| `mutable.Map`    | `Get`, `Put`, `Remove`, `Keys`           |                                                                                         |



## Example

The examples assume the following imports.
```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import com.fmsbeekmans.http.crud.akka.CrudDirectives
import com.fmsbeekmans.http.crud.akka.CrudRoutes
import com.fmsbeekmans.http.crud.akka.ToFuture._  
import com.fmsbeekmans.http.crud.core._
import com.fmsbeekmans.http.crud.repository.slick.DatabaseRepository
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
```

The data model for our examples

```scala
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
```


```scala
object RouteImplicits extends FailFastCirceSupport {

  type DB = slick.jdbc.JdbcBackend#DatabaseDef
  val database =
    Database.forURL(
      "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
      driver = "org.h2.Driver"
    )


  implicit val system = ActorSystem("my-system")
  implicit val executionContext = system.dispatcher  

  val table = TableQuery[People]

  implicit val repository: Repository[DB, Int, Person, Future] = DatabaseRepository(H2Profile)
    .repository[Person, People](table, _.id)

  implicit val keyMatcher = Directives.IntNumber
}
```

Wholesale route
```scala
object RouteExample extends Directives {
  import RouteImplicits._
    

  // expose everything
  val crudlRoute = path("people/"){
    CrudRoutes[DB, Int, Person, Future](database)
  }

  val appendOnlyRoute = path("peopleAppendOnlyExample") { 
    CrudRoutes.create[DB, Int, Person, Future](database) ~ 
      CrudRoutes.create[DB, Int, Person, Future](database)
  }

  def main(): Unit = {
    val bindingFuture = Http().bindAndHandle(crudlRoute, "localhost", 8080)

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
```

If more control is required, the directives used to create these routes are also exposed.
```scala
object DirectiveExample extends Directives {
  import RouteImplicits._

  // expose everything
  val directives = CrudDirectives[DB, Int, Person, Future]
  
  // using a helper to ease implicit resolution
  def read(key: Int) = directives.read(database, key)

  val route: Route = path("people") {
    read(1) { fetchPerson =>
      onComplete(fetchPerson) {
        case Success(Some(person)) => complete(person.name)
        case Success(None) => complete(StatusCodes.NotFound)
        case Failure(ex) => failWith(ex)
      }
    }
  }

  def main(): Unit = {
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
```