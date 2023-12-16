lazy val exclusions = Seq(ExclusionRule("org.spongepowered", "spongeapi"), ExclusionRule("com.typesafe", "config"))

def removeSnapshot(str: String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str
def katLibDependecy(module: String) =
  "com.github.Katrix-.KatLib" % s"katlib-$module" % "develop3.0.0-SNAPSHOT" excludeAll (exclusions: _*)

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

lazy val chitChat =
  crossProject(SpongePlatform("5.1.0"), SpongePlatform("6.0.0"), SpongePlatform("7.0.0"))
    .enablePlugins(SbtProguard)
    .settings(
      name := s"HomeSweetHome-${removeSnapshot(spongeApiVersion.value)}",
      organization := "net.katsstuff",
      version := "3.0.0-SNAPSHOT",
      scalaVersion := "2.12.5",
      resolvers += "jitpack" at "https://jitpack.io",
      addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
      libraryDependencies += "org.jetbrains" % "annotations" % "15.0" % Provided,
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      scalacOptions ++= extraScalacOptions,
      proguardOptions in Proguard ++= Seq(
        "-dontwarn", //"-dontwarn io.github.katrix.homesweethome.shade.scala.**",
        "-dontnote",
        "-dontoptimize",
        "-dontobfuscate",
        "-keepparameternames",
        "-keepattributes *",
        "-keep public class net.katsstuff.homesweethome.HomeSweetHome",
        """|-keepclassmembers class * {
           |    ** MODULE$;
           |    @org.spongepowered.api.event.Listener *;
           |    @com.google.inject.Inject *;
           |}""".stripMargin
      ),
      proguardInputs in Proguard := Seq(assembly.value),
      javaOptions in (Proguard, proguard) := Seq("-Xmx1G"),
      crossPaths := false,
      assemblyShadeRules in assembly := Seq(
        ShadeRule.rename("cats.**"                     -> "net.katsstuff.homesweethomeshade.cats.@1").inAll,
        ShadeRule.rename("com.google.protobuf.**"      -> "net.katsstuff.homesweethomeshade.protobuf.@1").inAll,
        ShadeRule.rename("com.trueaccord.lenses.**"    -> "net.katsstuff.homesweethomeshade.lenses.@1").inAll,
        ShadeRule.rename("com.trueaccord.scalapb.**"   -> "net.katsstuff.homesweethomeshade.scalapb.@1").inAll,
        ShadeRule.rename("fansi.**"                    -> "net.katsstuff.homesweethomeshade.fansi.@1").inAll,
        ShadeRule.rename("fastparse.**"                -> "net.katsstuff.homesweethomeshade.fastparse.@1").inAll,
        ShadeRule.rename("io.circe.**"                 -> "net.katsstuff.homesweethomeshade.circe.@1").inAll,
        ShadeRule.rename("jawn.**"                     -> "net.katsstuff.homesweethomeshade.jawn.@1").inAll,
        ShadeRule.rename("machinist.**"                -> "net.katsstuff.homesweethomeshade.machinist.@1").inAll, //Zap?
        ShadeRule.rename("metaconfig.**"               -> "net.katsstuff.homesweethomeshade.metaconfig.@1").inAll,
        ShadeRule.rename("org.langmeta.**"             -> "net.katsstuff.homesweethomeshade.langmeta.@1").inAll,
        ShadeRule.rename("org.scalameta.**"            -> "net.katsstuff.homesweethomeshade.scalameta.@1").inAll,
        ShadeRule.rename("org.typelevel.paiges.**"     -> "net.katsstuff.homesweethomeshade.paiges.@1").inAll,
        ShadeRule.rename("pprint.**"                   -> "net.katsstuff.homesweethomeshade.pprint.@1").inAll,
        ShadeRule.rename("scala.**"                    -> "net.katsstuff.homesweethomeshade.scala.@1").inAll,
        ShadeRule.rename("scalapb.**"                  -> "net.katsstuff.homesweethomeshade.scalapb.@1").inAll,
        ShadeRule.rename("shapeless.**"                -> "net.katsstuff.homesweethomeshade.shapeless.@1").inAll,
        ShadeRule.rename("sourcecode.**"               -> "net.katsstuff.homesweethomeshade.sourcecode.@1").inAll,
        ShadeRule.rename("net.katsstuff.katlib.**"     -> "net.katsstuff.homesweethomeshade.katlib.@1").inAll,
        ShadeRule.rename("net.katsstuff.scammander.**" -> "net.katsstuff.homesweethomeshade.scammander.@1").inAll,
        ShadeRule.zap("macrocompat.**").inAll,
      ),
      assemblyJarName := s"${name.value}-assembly-${version.value}.jar",
      spongePluginInfo := spongePluginInfo.value.copy(
        id = "chitchat",
        name = Some("ChitChat"),
        version = Some(s"${version.value}-${removeSnapshot(spongeApiVersion.value)}"),
        authors = Seq("Katrix"),
        description = Some("A chat plugin to manage several different chat channels and do chatty stuff."),
        dependencies = Set(
          DependencyInfo(LoadOrder.None, "spongeapi", Some(removeSnapshot(spongeApiVersion.value)), optional = false)
        )
      ),
      oreDeploymentKey := (oreDeploymentKey in Scope.Global).?.value.flatten,
      //https://github.com/portable-scala/sbt-crossproject/issues/74
      Seq(Compile, Test).flatMap(inConfig(_) {
        unmanagedResourceDirectories ++= {
          unmanagedSourceDirectories.value
            .map(src => (src / ".." / "resources").getCanonicalFile)
            .filterNot(unmanagedResourceDirectories.value.contains)
            .distinct
        }
      })
    )
    .spongeSettings("5.1.0")(libraryDependencies += katLibDependecy("5-1-0"))
    .spongeSettings("6.0.0")(libraryDependencies += katLibDependecy("6-0-0"))
    .spongeSettings("7.0.0")(libraryDependencies += katLibDependecy("7-0-0"))

lazy val chitChatV500 = chitChat.spongeProject("5.1.0")
lazy val chitChatV600 = chitChat.spongeProject("6.0.0")
lazy val chitChatV700 = chitChat.spongeProject("7.0.0")

lazy val chitChatRoot = (project in file("."))
  .disablePlugins(AssemblyPlugin)
  .aggregate(chitChatV500, chitChatV600, chitChatV700)
