import sbt._
import Keys._

object Dependencies extends AutoPlugin {
  object autoImport {
    implicit final class DependenciesOps(val project: Project) extends AnyVal {

      def withDependencies: Project =
        project
	        .settings(
            libraryDependencies ++= Seq(
              "com.typesafe.akka" %% "akka-http" % "10.1.1"
            )
          )
    }
  }


}