import sbt._
import Keys._

object Dependencies extends AutoPlugin {

  object versions {
    object akka {
      val core = "2.6.1"
      val http = "10.1.11"
    }

    val h2 = "1.4.200"
    val scalatest = "3.1.0"
    val slick = "3.3.1"
  }

  val akkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-http" % versions.akka.http,
    "com.typesafe.akka" %% "akka-http-testkit" % versions.akka.http % "test",
    "com.typesafe.akka" %% "akka-actor" % versions.akka.core,
    "com.typesafe.akka" %% "akka-stream" % versions.akka.core,
    "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka.core % "test"
  )

  val circe = Seq(
    "io.circe" %% "circe-generic" % "0.12.3",
    "de.heikoseeberger" %% "akka-http-circe" % "1.30.0"
  )

  val h2 = Seq(
    "com.h2database" % "h2" % versions.h2
  )

  val scalatest = Seq(
    "org.scalatest" %% "scalatest" % versions.scalatest
  )

  val slickDependencies = Seq(
    "com.typesafe.slick" %% "slick" % versions.slick,
    "com.typesafe.slick" %% "slick-testkit" % versions.slick
  )

  object autoImport {
    implicit final class DependenciesOps(val project: Project) extends AnyVal {

      def withDocsDependencies: Project =
        project
          .settings(
            libraryDependencies ++=
              scalatest ++
                circe ++
                h2 ++
                slickDependencies
          )

      def withTestDependencies: Project =
        project
          .settings(
            libraryDependencies ++=
              scalatest.map(_ % "test")
          )

      def withAkkaDependencies: Project =
        project
          .settings(
            libraryDependencies ++=
              akkaDependencies ++
                circe.map(_ % "test")
          )

      def withSlickDependencies: Project =
        project
          .settings(
            libraryDependencies ++=
              slickDependencies ++
                h2.map(_ % "test")
          )
    }
  }
}
