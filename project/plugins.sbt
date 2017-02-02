logLevel := Level.Warn
addSbtPlugin("net.katsstuff" % "sbt-spongyinfo" % "1.0")
//resolvers += Resolver.bintrayRepo("katrix", "sbt-plugins")

resolvers += Resolver.url(
	"katrix-sbtplugins",
	url("https://dl.bintray.com/katrix/sbt-plugins"))(
	Resolver.ivyStylePatterns)