val Http4sVersion = "0.20.9"
val LogbackVersion = "1.2.3"

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val sharedSettings = Seq(
  organization := "oen",
  scalaVersion := "2.12.9", // react router doesn't work with 2.12.7
  version := "0.1.0-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.7.0",
    "org.typelevel" %% "cats-core" % "1.6.1",
    "io.circe" %%% "circe-generic" % "0.11.1",
    "io.circe" %%% "circe-literal" % "0.11.1",
    "io.circe" %%% "circe-generic-extras" % "0.11.1",
    "io.circe" %%% "circe-parser" % "0.11.1",
    "io.scalaland" %%% "chimney" % "0.3.2",
    "com.softwaremill.quicklens" %%% "quicklens" % "1.4.12"
  ),
  scalacOptions ++= Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ypartial-unification",
    "-language:higherKinds"
  )
)

lazy val fastOptJSDev = TaskKey[Unit]("fastOptJSDev")

lazy val jsSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.7",
    "com.github.japgolly.scalajs-react" %%% "core" % "1.4.2",
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.2",
    "io.suzaku" %%% "diode" % "1.1.5",
    "io.suzaku" %%% "diode-react" % "1.1.5.142"
  ),
  dependencyOverrides += "org.webjars.npm" % "js-tokens" % "3.0.2", // just to resolve bug with version-range
  jsDependencies ++= Seq(
    "org.webjars.npm" % "react" % "16.8.5" / "umd/react.development.js" minified "umd/react.production.min.js" commonJSName "React",
    "org.webjars.npm" % "react-dom" % "16.8.5" / "umd/react-dom.development.js" minified "umd/react-dom.production.min.js" dependsOn "umd/react.development.js" commonJSName "ReactDOM",
    "org.webjars.npm" % "react-dom" % "16.8.5" / "umd/react-dom-server.browser.development.js" minified "umd/react-dom-server.browser.production.min.js" dependsOn "umd/react-dom.development.js" commonJSName "ReactDOMServer"
  ),
  jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
  skip in packageJSDependencies := false,
  fastOptJSDev := {
    // resources
    val targetRes = "../target/scala-2.12/classes/"
    IO.copyDirectory((resourceDirectory in Compile).value, new File(baseDirectory.value, targetRes))

    // fastopt.js
    val fastOptFrom = (fastOptJS in Compile).value.data
    val fastOptTo = new File(baseDirectory.value, targetRes + fastOptFrom.name)
    IO.copyFile(fastOptFrom, fastOptTo)

    // fastopt.js.map
    val mapFileName = fastOptFrom.name + ".map"
    val fastOptMapFrom = fastOptFrom.getParentFile / mapFileName
    val fastOptMapTo = new File(baseDirectory.value, targetRes + mapFileName)
    IO.copyFile(fastOptMapFrom, fastOptMapTo)

    // jsdeps.js
    val jsdepsFile = (packageJSDependencies in Compile).value
    val jsdepsTo = new File(baseDirectory.value, targetRes + jsdepsFile.name)
    IO.copyFile(jsdepsFile, jsdepsTo)
  }
)

lazy val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "1.4.0",
    "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
    "org.http4s" %% "http4s-circe" % Http4sVersion,
    "org.http4s" %% "http4s-dsl" % Http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "com.github.pureconfig" %% "pureconfig" % "0.11.0"
  ),
  target := baseDirectory.value / ".." / "target"
)

lazy val app =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full).in(file("."))
    .settings(sharedSettings)
    .jsSettings(jsSettings)
    .jvmSettings(jvmSettings)

lazy val appJS = app.js
  .enablePlugins(WorkbenchPlugin)
  .disablePlugins(RevolverPlugin)

lazy val appJVM = app.jvm
  .enablePlugins(JavaAppPackaging)
  .settings(
    dockerExposedPorts := Seq(8080),
    dockerBaseImage := "oracle/graalvm-ce:19.1.1",
    (resources in Compile) += (fullOptJS in(appJS, Compile)).value.data,
    (resources in Compile) += (packageMinifiedJSDependencies in(appJS, Compile)).value,
    (unmanagedResourceDirectories in Compile) += (resourceDirectory in(appJS, Compile)).value
  )

disablePlugins(RevolverPlugin)
