package net.katsstuff.chitchat.chat

import scala.collection.JavaConverters._

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent
import org.spongepowered.api.event.message.{MessageChannelEvent, MessageEvent}
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.event.{Listener, Order}
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.text.channel.`type`.CombinedMessageChannel
import org.spongepowered.api.text.serializer.TextSerializers
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier
import org.spongepowered.api.text.{Text, TextElement, TextTemplate, TranslatableText}

import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.LibPerm

class ChatListener(handler: ChannelHandler)(implicit plugin: ChitChatPlugin) {

  private def cfg = plugin.config

  @Listener(order = Order.LAST)
  def onMessageChannel(event: MessageChannelEvent.Chat): Unit = {
    event.getCause.first(classOf[Player]).toOption match {
      case Some(player) =>
        val formatter      = event.getFormatter
        val oldSerializer  = TextSerializers.FORMATTING_CODE
        val jsonSerializer = TextSerializers.JSON
        val plainName      = findNameChat(formatter.getHeader.getAll.asScala, "header").getOrElse(t"${player.getName}")

        val versionHelper = plugin.versionHelper

        def getTextOption(variant: String, default: => Text) =
          versionHelper
            .getSubjectOption(player, s"json$variant")
            .map(jsonSerializer.deserialize)
            .orElse(
              versionHelper
                .getSubjectOption(player, variant)
                .map(oldSerializer.deserialize)
            )
            .getOrElse(default)

        val prefix = getTextOption("prefix", cfg.defaultPrefix.value)
        val suffix = getTextOption("suffix", cfg.defaultSuffix.value)

        val stringNameColor = versionHelper.getSubjectOption(player, "namestyle")
        val textFormat      = stringNameColor.flatMap(s => TextHelper.getFormatAtEnd(oldSerializer.deserialize(s)))
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
            applier.getParameter("body") match {
              case body: Text if body.toPlain.contains('&') =>
                applier.setParameter("body", oldSerializer.deserialize(oldSerializer.serialize(body)))
              case _ =>
            }
          }
        }

        val suffixApplier = new SimpleTextTemplateApplier(cfg.suffixTemplate.value)
        formatter.getFooter.add(suffixApplier)

        for (applier <- formatter.getFooter.asScala) {
          applier.setParameter(cfg.TemplateSuffix, suffix)
        }

        event.getChannel.toOption.foreach { existing =>
          val playerChannel = handler.getChannelForReceiver(player)
          event.setChannel(new IntersectionMessageChannel(Set(existing, playerChannel.messageChannel)))

          if (cfg.mentionPling.value) {
            val message = event.getFormatter.getBody.format.toPlain
            playerChannel.members
              .collect { case player: Player if message.contains(player.getName) => player }
              .foreach(p => p.playSound(versionHelper.levelUpSound, p.getLocation.getPosition, 0.5D))
          }
        }
      case None =>
    }

    //Console knows all!!!
    event.getChannel.toOption.foreach(c => event.setChannel(new CombinedMessageChannel(c, MessageChannel.TO_CONSOLE)))
  }

  @Listener def onJoin(event:       ClientConnectionEvent.Join):       Unit = onConnectEvent(event, cfg.joinTemplate.value)
  @Listener def onDisconnect(event: ClientConnectionEvent.Disconnect): Unit = onConnectEvent(event, cfg.disconnectTemplate.value)

  def onConnectEvent(event: ClientConnectionEvent with MessageEvent with TargetPlayerEvent, template: TextTemplate): Unit = {
    val appliers = event.getFormatter.getBody.getAll.asScala
    val player   = event.getTargetEntity
    val name     = findNameConnect(appliers, "body").getOrElse(t"${player.getName}")

    for (applier <- appliers if applier.getParameters.containsKey("body")) {
      applier.setTemplate(template)
      applier.setParameter("body", name)
    }
  }

  private def findNameChat(applierList: Seq[SimpleTextTemplateApplier], parameterName: String): Option[TextElement] =
    findNameCommon(applierList, parameterName).headOption

  private def findNameConnect(applierList: Seq[SimpleTextTemplateApplier], parameterName: String): Option[Text] =
    findNameCommon(applierList, parameterName).collectFirst {
      case translatable: TranslatableText => translatable.getArguments.asScala.collectFirst { case t: Text => t }
    }.flatten

  private def findNameCommon(applierList: Seq[SimpleTextTemplateApplier], parameterName: String): Seq[TextElement] =
    applierList
      .withFilter(_.getParameters.containsKey(parameterName))
      .map(_.getParameter(parameterName))
}
