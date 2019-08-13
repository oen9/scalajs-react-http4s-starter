val Http4sVersion = "0.20.9"
val LogbackVersion = "1.2.3"

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val sharedSettings = Seq(
  organization := "oen",
  scalaVersion := "2.12.9",
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
  npmDependencies in Compile ++= Seq(
    "react" -> "16.9.0",
    "react-dom" -> "16.9.0",
  ),
  webpackBundlingMode := BundlingMode.LibraryAndApplication(), // LibraryOnly() for faster dev builds
  scalaJSUseMainModuleInitializer := true,
  fastOptJSDev := {
    // resources
    val targetRes = "../target/scala-2.12/classes/"
    IO.copyDirectory((resourceDirectory in Compile).value, new File(baseDirectory.value, targetRes))

    // webpack
    val webpackFiles = webpack.in(Compile, fastOptJS).value.map(_.data)
    val targetBoundle = targetRes + "scalajs-bundler/main/"
    webpackFiles.foreach { f =>
      val targetFile = new File(baseDirectory.value, targetBoundle + f.name)
      IO.copyFile(f, targetFile)
    }
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
  .enablePlugins(ScalaJSBundlerPlugin)
  .disablePlugins(RevolverPlugin)

lazy val appJVM = app.jvm
  .enablePlugins(JavaAppPackaging)
  .settings(
    dockerExposedPorts := Seq(8080),
    dockerBaseImage := "oracle/graalvm-ce:19.1.1",
    (resources in Compile) ++= webpack.in(Compile, fullOptJS).in(appJS, Compile).value.map(_.data),
    (unmanagedResourceDirectories in Compile) += (resourceDirectory in(appJS, Compile)).value
  )

disablePlugins(RevolverPlugin)
