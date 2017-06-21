import com.lightbend.lagom.sbt.LagomImport.lagomScaladslApi

organization in ThisBuild := "io.digitalcat"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val base64 = "me.lessis" %% "base64" % "0.2.0"
val jwt = "com.pauldijou" %% "jwt-play-json" % "0.12.1"
val accord = "com.wix" %% "accord-core" % "0.6.1"

lazy val `public-transportation-services` = (project in file("."))
  .aggregate(`common`, `identity-api`, `identity-impl`)

lazy val `common` = (project in file("common"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer,
      jwt,
      accord
    )
  )

lazy val `identity-api` = (project in file("identity-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      accord
    )
  )
  .dependsOn(`common`)

lazy val `identity-impl` = (project in file("identity-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      base64,
      jwt
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`common`, `identity-api`)

