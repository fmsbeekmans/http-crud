import sbt._
import Keys._

object Dependencies extends AutoPlugin {

  object versions {
    val akka = "2.5.26"
    val akkaHttp = "10.1.11"
  }

  object autoImport {
    implicit final class DependenciesOps(val project: Project) extends AnyVal {

      def withDependencies: Project =
        project
          .settings(
            libraryDependencies ++= Seq(
              "com.typesafe.akka" %% "akka-http" % versions.akkaHttp,
              "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3",
              "com.typesafe.akka" %% "akka-actor" % versions.akka,
              "com.typesafe.akka" %% "akka-stream" % versions.akka
            )
          )
    }
  }
}
