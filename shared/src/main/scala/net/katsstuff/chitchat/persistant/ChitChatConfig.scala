package net.katsstuff.chitchat.persistant

import org.spongepowered.api.event.message.MessageEvent
import org.spongepowered.api.text.{Text, TextTemplate}

import io.github.katrix.katlib.persistant.{CommentedConfigValue, Config}
import net.katsstuff.chitchat.lib.LibPlugin

trait ChitChatConfig extends Config {

  final val TemplatePrefix = s"${LibPlugin.Id}.prefix"
  final val TemplateSuffix = s"${LibPlugin.Id}.suffix"

  final val TemplateHeader = MessageEvent.PARAM_MESSAGE_HEADER

  def version: CommentedConfigValue[String]

  def mentionPling: CommentedConfigValue[Boolean]

  def defaultPrefix: CommentedConfigValue[Text]
  def defaultSuffix: CommentedConfigValue[Text]

  def headerTemplate: CommentedConfigValue[TextTemplate]
  def suffixTemplate: CommentedConfigValue[TextTemplate]

  def joinTemplate:       CommentedConfigValue[TextTemplate]
  def disconnectTemplate: CommentedConfigValue[TextTemplate]

  override def seq: Seq[CommentedConfigValue[_]] = Seq(
    version,

    mentionPling,

    defaultPrefix,
    defaultSuffix,

    headerTemplate,
    suffixTemplate,

    joinTemplate,
    disconnectTemplate
  )
}
