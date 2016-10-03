lazy val commonSettings = Seq(
	name := s"ChitChat-${spongeApiVersion.value}",
	organization := "net.katsstuff",
	version := "2.0.0-SNAPSHOT",
	scalaVersion := "2.11.8",
	resolvers += "jitpack" at "https://jitpack.io",
	libraryDependencies += "com.github.Katrix-.KatLib" % "katlib-shared" % "1.1.0" % "provided",
	assemblyShadeRules in assembly := Seq(
		ShadeRule.rename("scala.**" -> "io.github.katrix.katlib.shade.scala.@1").inProject
	),
	scalacOptions += "-Xexperimental",
	crossPaths := false,

	spongePluginInfo := PluginInfo(
		id = "chitchat",
		name = Some("ChitChat"),
		version = Some(s"${spongeApiVersion.value}-${version.value}"),
		authors = Seq("Katrix")
	)
)

lazy val chitChatShared = (project in file("shared"))
	.enablePlugins(SpongePlugin)
	.settings(commonSettings: _*)
	.settings(
		name := "ChitChat-Shared",
		assembleArtifact := false,
		spongeMetaCreate := false,
		//Default version, needs to build correctly against all supported versions
		spongeApiVersion := "4.1.0"
	)

lazy val chitChatV410 = (project in file("4.1.0"))
	.enablePlugins(SpongePlugin)
	.dependsOn(chitChatShared)
	.settings(commonSettings: _*)
	.settings(
		spongeApiVersion := "4.1.0",
		libraryDependencies += "com.github.Katrix-.KatLib" % "katlib-4-1-0" % "1.1.0" % "provided"
	)

lazy val chitChatV500 = (project in file("5.0.0"))
	.enablePlugins(SpongePlugin)
	.dependsOn(chitChatShared)
	.settings(commonSettings: _*)
	.settings(
		spongeApiVersion := "5.0.0-SNAPSHOT",
		libraryDependencies += "com.github.Katrix-.KatLib" % "katlib-5-0-0" % "1.1.0" % "provided"
	)

lazy val chitChatRoot = (project in file("."))
	.settings(publishArtifact := false)
	.disablePlugins(AssemblyPlugin)
	.aggregate(chitChatShared, chitChatV410, chitChatV500)