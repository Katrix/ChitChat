package net.katsstuff.chitchat.chat

import scala.collection.JavaConverters._

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.message.MessageChannelEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.event.{Listener, Order}
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.text.channel.`type`.CombinedMessageChannel
import org.spongepowered.api.text.serializer.TextSerializers
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier
import org.spongepowered.api.text.{Text, TextElement, TranslatableText}

import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.chat.channel.Channel
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.LibPerm

class ChatListener(implicit plugin: ChitChatPlugin) {

  private def cfg = plugin.config

  @Listener(order = Order.LAST)
  def onMessageChannel(event: MessageChannelEvent): Unit = {
    event.getCause.first(classOf[Player]).toOption match {
      case Some(player) =>
        event match {
          case chat: MessageChannelEvent.Chat =>
            val formatter     = event.getFormatter
            val serializer    = TextSerializers.FORMATTING_CODE
            val defaultPrefix = cfg.defaultPrefix.value
            val defaultSuffix = cfg.defaultSuffix.value
            val plainName     = findNameChat(formatter.getHeader.getAll.asScala, "header").getOrElse(t"${player.getName}")

            val prefix = plugin.versionHelper.getSubjectOption(player, "prefix").map(serializer.deserialize).getOrElse(defaultPrefix)
            val suffix = plugin.versionHelper.getSubjectOption(player, "suffix").map(serializer.deserialize).getOrElse(defaultSuffix)

            val stringNameColor = plugin.versionHelper.getSubjectOption(player, "nameColor")
            val textFormat      = stringNameColor.flatMap(s => TextHelper.getFormatAtEnd(serializer.deserialize(s)))
            val name            = textFormat.map(f => t"$f$plainName").getOrElse(plainName)

            for (applier <- formatter.getHeader.asScala) {
              if (applier.getParameters.containsKey("header")) {
                applier.setTemplate(cfg.headerTemplate.value)
                applier.setParameter("header", name)
              }
              applier.setParameter(cfg.TemplatePrefix, prefix)
            }

            if (player.hasPermission(LibPerm.ChatColor)) {
              for (applier <- formatter.getBody.asScala if applier.getParameters.containsKey("body")) {
                //In case the message is already formatted
                applier.setParameter("body", serializer.deserialize(serializer.serialize(t"${applier.getParameter("body")}")))
              }
            }

            val suffixApplier = new SimpleTextTemplateApplier(cfg.suffixTemplate.value)
            formatter.getFooter.add(suffixApplier)

            for (applier <- formatter.getFooter.asScala) {
              applier.setParameter(cfg.TemplateSuffix, suffix)
            }

            val playerChannel: Channel = findPlayerMainChannel(player)
            event.setChannel(playerChannel.messageChannel)

            if (cfg.mentionPling.value) {
              val message = event.getFormatter.getBody.format.toPlain
              playerChannel.messageChannel.getMembers.asScala
                .withFilter(messageReceiver => messageReceiver.isInstanceOf[Player])
                .map(m => m.asInstanceOf[Player])
                .withFilter(p => message.contains(p.getName))
                .foreach(p => p.playSound(plugin.versionHelper.experiencePling, p.getLocation.getPosition, 0.5D))
            }

          case _ =>
        }
      case None =>
    }

    //Console knows all!!!
    event.getChannel.toOption.foreach(c => event.setChannel(new CombinedMessageChannel(c, MessageChannel.TO_CONSOLE)))
  }

  @Listener
  def onJoin(event: ClientConnectionEvent.Join): Unit = {
    val appliers = event.getFormatter.getBody.getAll.asScala
    val player   = event.getTargetEntity
    val name     = findNameConnect(appliers, "body").getOrElse(t"${player.getName}")

    for (applier <- appliers if applier.getParameters.containsKey("body")) {
      applier.setTemplate(cfg.joinTemplate.value)
      applier.setParameter("body", name)
    }
  }

  @Listener
  def onDisconnect(event: ClientConnectionEvent.Disconnect): Unit = {
    val appliers = event.getFormatter.getBody.getAll.asScala
    val player   = event.getTargetEntity
    val name     = findNameConnect(appliers, "body").getOrElse(t"${player.getName}")

    for (applier <- appliers if applier.getParameters.containsKey("body")) {
      applier.setTemplate(cfg.disconnectTemplate.value)
      applier.setParameter("body", name)
    }
  }

  private def findNameChat(applierList: Seq[SimpleTextTemplateApplier], parameterName: String): Option[TextElement] =
    applierList
      .withFilter(_.getParameters.containsKey(parameterName))
      .map(_.getParameter(parameterName))
      .headOption

  private def findNameConnect(applierList: Seq[SimpleTextTemplateApplier], parameterName: String): Option[Text] =
    applierList
      .withFilter(applier => applier.getParameters.containsKey(parameterName))
      .map(applier => applier.getParameter(parameterName))
      .collectFirst { case translatable: TranslatableText => translatable.getArguments.asScala.collectFirst { case t: Text => t } }
      .flatten
}
