import sbt._
import Keys._

object Dependencies extends AutoPlugin {

  object versions {
    object akka {
      val core = "2.5.26"
      val http = "10.1.11"
    }
    val slick = "3.3.1"
  }

  object autoImport {
    implicit final class DependenciesOps(val project: Project) extends AnyVal {

      def withAkkaDependencies: Project =
        project
          .settings(
            libraryDependencies ++= Seq(
              "com.typesafe.akka" %% "akka-http" % versions.akka.http,
              "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3",
              "com.typesafe.akka" %% "akka-actor" % versions.akka.core,
              "com.typesafe.akka" %% "akka-stream" % versions.akka.core
            )
          )

      def withSlickDependencies: Project =
        project
          .settings(
            libraryDependencies ++= Seq(
              "com.typesafe.slick" %% "slick" % versions.slick,
              "com.typesafe.slick" %% "slick-hikaricp" % versions.slick
            )
          )
    }
  }
}
