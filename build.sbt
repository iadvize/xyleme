val scala212 = "2.12.17"
val scala213 = "2.13.10"

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
    scalaVersion := scala213,
    crossScalaVersions := List(scala212, scala213),
    scalafixDependencies += "com.github.vovapolu" %% "scaluzzi" % "0.1.23",
    scalafixScalaBinaryVersion := "2.13",
    githubWorkflowBuild := Seq(
      WorkflowStep.Sbt(
        List("ci"),
        id = None,
        name = Some("Test JVM (with coverage)")
      ),
      WorkflowStep.Sbt(
        List("coverageReport"),
        id = None,
        name = Some("Coverage")
      ),
      WorkflowStep.Use(
        UseRef.Public(
          "codecov",
          "codecov-action",
          "v2"
        ),
        params = Map(
          "flags" -> List("${{matrix.scala}}", "${{matrix.java}}").mkString(","),
          "token" -> "${{ secrets.CODECOV_TOKEN }}"
        )
      )
    ),
    githubWorkflowTargetTags ++= Seq("v*"),
    githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
    githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
  )
)

lazy val core = (project in file("modules/core")).settings(
  name := "xyleme",
  libraryDependencies ++= List(
    Dependencies.scalaXml,
    Dependencies.catsCore,
    Dependencies.scalaTest % Test
  ),
  Test / scalacOptions -= "-Wnonunit-statement"
)

def addCommandsAlias(name: String, values: List[String]) =
  addCommandAlias(name, values.mkString(";", ";", ""))

addCommandsAlias(
  "ci",
  List(
    "scalafmtCheckAll",
    "scalafmtSbtCheck",
    "scalafixEnable",
    "scalafix --check",
    "coverageOn",
    "test"
  )
)
