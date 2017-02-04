package net.katsstuff.chitchat.persistant

import java.nio.file.Path

import org.spongepowered.api.text.Text

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.persistant.{ConfigLoader, ConfigValue}
import io.github.katrix.katlib.helper.Implicits._
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.helper.LogHelper

class ChitChatConfigLoader(dir: Path)(implicit plugin: KatPlugin) extends ConfigLoader[ChitChatConfig](dir, identity) {
  override def loadData: ChitChatConfig = {
    val loaded = cfgRoot.getNode("version").getString("1") match {
      case "1" => new ChitChatConfigV1(cfgRoot, default)
      case unknown =>
        LogHelper.error(s"Unknown config version $unknown, using default instead")
        default
    }
    saveData(loaded)
    loaded
  }

  val default = new ChitChatConfig {

    override def version = ConfigValue("1", "Please don't touch this", Seq("version"))

    override def mentionPling = ConfigValue(true, "Should players be plinged if they are mentioned", Seq("chat", "mention-pling"))

    override def defaultPrefix = ConfigValue(Text.EMPTY, "Prefix to add if no prefix is found", Seq("formatting", "default", "prefix"))
    override def defaultSuffix = ConfigValue(Text.EMPTY, "Suffix to add if no suffix is found", Seq("formatting", "default", "suffix"))

    override def headerTemplate =
      ConfigValue(tt"$TemplatePrefix$TemplateHeader: ", "Template to use for the header", Seq("formatting", "chat", "header-template"))
    override def suffixTemplate = ConfigValue(tt"$TemplateSuffix", "Template to use for the suffix", Seq("formatting", "chat", "suffix-template"))

    override def joinTemplate =
      ConfigValue(
        tt"${GREEN}The player $AQUA${"body"}$GREEN has joined the server",
        "Template to use when someone joins",
        Seq("formatting", "connect", "join-template")
      )
    override def disconnectTemplate =
      ConfigValue(
        tt"${RED}The player $AQUA${"body"}$RED has left the server",
        "Template to use when someone disconnects",
        Seq("formatting", "connect", "disconnect-template")
      )
  }
}
