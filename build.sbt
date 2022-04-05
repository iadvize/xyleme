val scala212 = "2.12.15"
val scala213 = "2.13.8"

inThisBuild(
  List(
    organization := "com.iadvize",
    homepage := Some(url("https://github.com/iadvize/xyleme")),
    licenses := List("MIT" -> url("https://mit-license.org")),
    developers := List(
      Developer(
        "xela85",
        "Alexandre LEBRUN",
        "alexandre.lebrun@iadvize.com",
        url("https://github.com/xela85")
      )
    ),
    scalaVersion := scala212,
    crossScalaVersions := List(scala212, scala213),
    scalacOptions += "-Ypartial-unification"
  )
)

lazy val core = (project in file("modules/core")).settings(
  name := "xyleme",
  libraryDependencies ++= List(
    Dependencies.scalaXml,
    Dependencies.catsCore,
    Dependencies.scalaTest % Test
  )
)
