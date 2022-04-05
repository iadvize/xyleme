import Dependencies._

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
    scalaVersion := "2.13.8"
  )
)

lazy val root = (project in file("modules/core")).settings(
  name := "xyleme",
  libraryDependencies += scalaTest % Test
)