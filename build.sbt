def removeSnapshot(str: String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str
def katLibDependecy(module: String) = "com.github.Katrix-.KatLib" % s"katlib-$module" % "develop-SNAPSHOT" % Provided

lazy val publishResolver = {
  val artifactPattern = s"""${file("publish").absolutePath}/[revision]/[artifact]-[revision](-[classifier]).[ext]"""
  Resolver.file("publish").artifacts(artifactPattern)
}

//Taken from http://tpolecat.github.io/2017/04/25/scalac-flags.html with modifications
lazy val extraScalacOptions = Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfuture",                          // Turn on future language features.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  //"-Ywarn-unused:params",            // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates"             // Warn if a private member is unused.
)

lazy val commonSettings = Seq(
  name := s"ChitChat-${removeSnapshot(spongeApiVersion.value)}",
  organization := "net.katsstuff",
  version := "2.0.1",
  scalaVersion := "2.12.2",
  resolvers += "jitpack" at "https://jitpack.io",
  libraryDependencies += katLibDependecy("shared"),
  scalacOptions ++= extraScalacOptions,
  scalacOptions in (Compile, console) ~= (_.filterNot(Set("-Ywarn-unused:imports"))),
  crossPaths := false,
  assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("scala.**"     -> "io.github.katrix.katlib.shade.scala.@1").inAll,
    ShadeRule.rename("shapeless.**" -> "io.github.katrix.katlib.shade.shapeless.@1").inAll
  ),
  autoScalaLibrary := false,
  publishTo := Some(publishResolver),
  publishArtifact in makePom := false,
  publishArtifact in (Compile, packageBin) := false,
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in (Compile, packageSrc) := false,
  artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.copy(`classifier` = Some("assembly"))
  },
  spongePluginInfo := spongePluginInfo.value.copy(
    id = "chitchat",
    name = Some("ChitChat"),
    version = Some(s"${removeSnapshot(spongeApiVersion.value)}-${version.value}"),
    authors = Seq("Katrix"),
    description = Some("A chat plugin to manage several different chat channels and do chatty stuff."),
    dependencies = Set(
      DependencyInfo("spongeapi", Some(removeSnapshot(spongeApiVersion.value))),
      DependencyInfo("katlib", Some(s"${removeSnapshot(spongeApiVersion.value)}-2.1.0"))
    )
  )
) ++ addArtifact(artifact in (Compile, assembly), assembly)

lazy val chitChatShared = (project in file("shared"))
  .enablePlugins(SpongePlugin)
  .settings(
    commonSettings,
    name := "ChitChat-Shared",
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    assembleArtifact := false,
    spongeMetaCreate := false,
    //Default version, needs to build correctly against all supported versions
    spongeApiVersion := "4.1.0"
  )

lazy val chitChatV410 = (project in file("4.1.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(chitChatShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "4.1.0", libraryDependencies += katLibDependecy("4-1-0"))

lazy val chitChatV500 = (project in file("5.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(chitChatShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "5.0.0", libraryDependencies += katLibDependecy("5-0-0"))

lazy val chitChatV600 = (project in file("6.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(chitChatShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "6.0.0-SNAPSHOT", libraryDependencies += katLibDependecy("6-0-0"))

lazy val chitChatRoot = (project in file("."))
  .settings(
    publishArtifact := false,
    assembleArtifact := false,
    spongeMetaCreate := false,
    publish := {},
    publishLocal := {}
  )
  .disablePlugins(AssemblyPlugin)
  .aggregate(chitChatShared, chitChatV410, chitChatV500, chitChatV600)
