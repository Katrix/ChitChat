package net.katsstuff.chitchat.persistant

import org.spongepowered.api.event.message.MessageEvent
import org.spongepowered.api.text.{Text, TextTemplate}

import io.github.katrix.katlib.persistant.{CommentedConfigValue, Config}
import net.katsstuff.chitchat.lib.LibPlugin

trait ChitChatConfig extends Config {

  final val TemplatePrefix = s"${LibPlugin.Id}.prefix"
  final val TemplateSuffix = s"${LibPlugin.Id}.suffix"

  final val TemplateHeader = MessageEvent.PARAM_MESSAGE_HEADER

  final val PlayerName = s"${LibPlugin.Id}.playerName"
  final val Message    = s"${LibPlugin.Id}.message"

  final val Sender   = s"${LibPlugin.Id}.sender"
  final val Receiver = s"${LibPlugin.Id}.receiver"

  def version: CommentedConfigValue[String]

  def mentionPling: CommentedConfigValue[Boolean]

  def defaultPrefix: CommentedConfigValue[Text]
  def defaultSuffix: CommentedConfigValue[Text]

  def headerTemplate: CommentedConfigValue[TextTemplate]
  def suffixTemplate: CommentedConfigValue[TextTemplate]

  def joinTemplate:       CommentedConfigValue[TextTemplate]
  def disconnectTemplate: CommentedConfigValue[TextTemplate]

  def announceTemplate: CommentedConfigValue[TextTemplate]
  def meTemplate:       CommentedConfigValue[TextTemplate]
  def pmTemplate:       CommentedConfigValue[TextTemplate]
  def shoutTemplate:    CommentedConfigValue[TextTemplate]

  // @formatter:off
  override def seq: Seq[CommentedConfigValue[_]] = Seq(
    version,

    mentionPling,

    defaultPrefix,
    defaultSuffix,

    headerTemplate,
    suffixTemplate,

    joinTemplate,
    disconnectTemplate,

    announceTemplate,
    meTemplate,
    pmTemplate,
    shoutTemplate
  )
  // @formatter:on
}
