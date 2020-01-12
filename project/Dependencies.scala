import sbt._
import Keys._

object Dependencies extends AutoPlugin {

  object versions {
    object akka {
      val core = "2.5.26"
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
    "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka.core % "test",
  )

  val slickDependencies = Seq(
    "com.typesafe.slick" %% "slick" % versions.slick,
    "com.typesafe.slick" %% "slick-testkit" % versions.slick,
    "com.h2database" % "h2" % versions.h2 % "test"
  )

  object autoImport {
    implicit final class DependenciesOps(val project: Project) extends AnyVal {

      def withTestDependencies: Project =
        project
          .settings(
            libraryDependencies ++= Seq(
              "org.scalatest" %% "scalatest" % versions.scalatest % "test"
            )
          )

      def withAkkaDependencies: Project =
        project
          .settings(
            libraryDependencies ++= akkaDependencies
          )

      def withSlickDependencies: Project =
        project
          .settings(
            libraryDependencies ++= slickDependencies
          )
    }
  }
}
