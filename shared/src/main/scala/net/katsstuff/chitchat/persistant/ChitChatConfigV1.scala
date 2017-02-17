package net.katsstuff.chitchat.persistant

import scala.util.Try

import org.spongepowered.api.text.{Text, TextTemplate}

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.CommentedConfigValue
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.objectmapping.ObjectMappingException

class ChitChatConfigV1(cfgRoot: CommentedConfigurationNode, default: ChitChatConfig)(implicit plugin: KatPlugin) extends ChitChatConfig {

  override val version: CommentedConfigValue[String] = configValue(default.version)

  override val mentionPling: CommentedConfigValue[Boolean] = configValue(default.mentionPling)

  override val defaultPrefix: CommentedConfigValue[Text] = configValue(default.defaultPrefix)
  override val defaultSuffix: CommentedConfigValue[Text] = configValue(default.defaultSuffix)

  override val headerTemplate: CommentedConfigValue[TextTemplate] = configValue(default.headerTemplate)
  override val suffixTemplate: CommentedConfigValue[TextTemplate] = configValue(default.suffixTemplate)

  override val joinTemplate:       CommentedConfigValue[TextTemplate] = configValue(default.joinTemplate)
  override val disconnectTemplate: CommentedConfigValue[TextTemplate] = configValue(default.disconnectTemplate)

  override val announceTemplate:  CommentedConfigValue[TextTemplate] = configValue(default.announceTemplate)
  override val meTemplate:        CommentedConfigValue[TextTemplate] = configValue(default.meTemplate)
  override val pmTemplate       :    CommentedConfigValue[TextTemplate]  = configValue(default.pmTemplate)
  override val shoutTemplate    :     CommentedConfigValue[TextTemplate] = configValue(default.shoutTemplate)

  def configValue[A](existing: CommentedConfigValue[A])(implicit plugin: KatPlugin): CommentedConfigValue[A] =
    Try(Option(cfgRoot.getNode(existing.path: _*).getValue(existing.typeToken)).get)
      .map(found => existing.value_=(found)) //Doesn't want to work with CommentedConfigValue
      .recover {
        case _: ObjectMappingException =>
          LogHelper.error(s"Failed to deserialize value of ${existing.path.mkString(", ")}, using the default instead")
          existing
        case _: NoSuchElementException =>
          LogHelper.warn(s"No value found for ${existing.path.mkString(", ")}, using default instead")
          existing
      }
      .get
}
