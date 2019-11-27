import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

organization in ThisBuild := "me.shadaj"

scalaVersion in ThisBuild := "2.13.1"

addCommandAlias(
  "publishSignedAll",
  (scalaPyNumpy: ProjectDefinition[ProjectReference])
    .aggregate
    .map(p => s"+ ${p.asInstanceOf[LocalProject].project}/publishSigned")
    .mkString(";", ";", "")
)

lazy val scalaPyNumpy = project.in(file(".")).aggregate(
  scalaPyNumpyJVM,
  scalaPyNumpyNative
).settings(
  publish := {},
  publishLocal := {},
  scalaSource in Compile := baseDirectory.value / "no-src",
  scalaSource in Test := baseDirectory.value / "no-src"
)

lazy val scalapyCore = ProjectRef(uri("git://github.com/MaximeKjaer/scalapy#port-2.13"), "coreJVM")

lazy val scalaPyNumpyCross = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    name := "scalapy-numpy"
  ).jvmSettings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.8" % Test,
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
    fork in Test := true,
    javaOptions in Test += s"-Djava.library.path=${sys.env.getOrElse("JEP_PATH", "/usr/local/lib/python3.7/site-packages/jep")}"
  ).nativeSettings(
    scalaVersion := "2.11.12",
    libraryDependencies += "me.shadaj" %%% "scalapy-core" % "0.3.0",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.0-SNAP8" % Test,
    libraryDependencies += "com.github.lolgab" %%% "scalacheck" % "1.14.1" % Test,
    nativeLinkStubs := true,
    nativeLinkingOptions ++= {
      import scala.sys.process._
      "python3-config --ldflags".!!.split(' ').map(_.trim).filter(_.nonEmpty).toSeq
    }
  )

lazy val scalaPyNumpyJVM = scalaPyNumpyCross.jvm.dependsOn(scalapyCore)
lazy val scalaPyNumpyNative = scalaPyNumpyCross.native
