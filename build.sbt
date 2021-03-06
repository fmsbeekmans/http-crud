lazy val root = (project in file("."))
  .settings(projectMetaData)
  .settings(
    name := "http-crud",
    moduleName := "http-crud",
    description := "http-crud helps you expose CRUD repositories as an API"
  )
  .settings(scalaSettings)
  .aggregate(`akka-http`, core, slick)
  .dependsOn(`akka-http`, core, slick)
  .settings(docSettings)
  .enablePlugins(MdocPlugin)
  .settings(noReleaseSettings)
  .withDocsDependencies

lazy val core = (project in file("modules/http-crud-core"))
  .settings(
    moduleName := "http-crud core",
    name := "http-crud-core",
    description := "The essential types for http-curd"
  )
  .settings(projectMetaData)
  .settings(scalaSettings)

lazy val `akka-http` = (project in file("modules/http-crud-akka"))
  .settings(projectMetaData)
  .settings(
    moduleName := "http-crud akka",
    name := "http-crud-akka",
    description := "Adapter to expose a repository through akka-http"
  )
  .settings(scalaSettings)
  .dependsOn(core)
  .withTestDependencies
  .withAkkaDependencies

lazy val slick = (project in file("modules/http-crud-slick"))
  .settings(projectMetaData)
  .settings(
    moduleName := "http-crud slick",
    name := "http-crud-core",
    description := "Adapter to use slick tables as repository"
  )
  .settings(scalaSettings)
  .dependsOn(core)
  .withTestDependencies
  .withSlickDependencies

lazy val projectMetaData = Seq(
  name := "http-crud",
  bintrayPackage := "http-crud",
  organization := "com.fmsbeekmans",
  version := "0.1.0",
  developers := List(
    Developer(
      "fmsbeekmans",
      "Ferdy Moon Soo Beekmans",
      "contact@fmsbeekmans.com",
      url("https://fmsbeekmans.com")
    )
  ),
  scmInfo := Some(ScmInfo(
    browseUrl = url("https://github.com/fmsbeekmans/http-crud"),
    connection = "scm:git:git://github.com/fmsbeekmans/crud-http.git",
  )),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))
)

lazy val scalaSettings = Seq(
  scalaVersion := "2.12.8",
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xfuture", // Turn on future language features.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match", // Pattern match may not be typesafe.
    "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification", // Enable partial unification in type constructor inference
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals", // Warn if a local definition is unused.
    "-Ywarn-unused:params", // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates", // Warn if a private member is unused.
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
  )
)

lazy val releaseSettings = {
  import sbtrelease.ReleaseStateTransformations._
  Seq(
    releaseCrossBuild := false,
    releaseTagComment := s"Release version ${(version in ThisBuild).value}",
    releaseTagName := s"v${(version in ThisBuild).value}",
    releaseCommitMessage := s"Release version ${(version in ThisBuild).value} [ci skip]",
    releaseUseGlobalVersion := true,
    publishArtifact in Test := false,
    publishMavenStyle := true,
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value}  [ci skip]",
    bintrayOrganization := None,
    bintrayRepository := "maven",
    bintrayOmitLicense := true,
    bintrayVcsUrl := Some("git@github.com:fmsbeekmans/http-crud.git"),
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}

lazy val noReleaseSettings = Seq(
  skip in publish := true,
  skip in publishLocal := true,
  publishArtifact := false,
  releaseProcess := Seq()
)

lazy val docSettings = Seq(
  mdocVariables := Map(
    "VERSION" -> version.value
  ),
  mdocOut := file(".")
)