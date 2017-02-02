package net.katsstuff.chitchat.persistant

import org.spongepowered.api.text.{Text, TextTemplate}

import io.github.katrix.katlib.persistant.{Config, ConfigValue}
import net.katsstuff.chitchat.lib.LibPlugin

trait ChitChatConfig extends Config {

	final val TemplatePrefix = s"${LibPlugin.Id}.prefix"
	final val TemplateSuffix = s"${LibPlugin.Id}.suffix"

	def mentionPling: ConfigValue[Boolean]

	def defaultPrefix: ConfigValue[Text]
	def defaultSuffix: ConfigValue[Text]

	def headerTemplate: ConfigValue[TextTemplate]
	def suffixTemplate: ConfigValue[TextTemplate]

	def joinTemplate: ConfigValue[TextTemplate]
	def disconnectTemplate: ConfigValue[TextTemplate]

	override def seq: Seq[ConfigValue[_]] = Seq(
		version
	)

}
