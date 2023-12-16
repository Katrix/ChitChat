package net.katsstuff.chitchat.chat

import scala.collection.JavaConverters._

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent
import org.spongepowered.api.event.message.{MessageChannelEvent, MessageEvent}
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.event.{Listener, Order}
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.text.channel.`type`.CombinedMessageChannel
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.serializer.TextSerializers
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier
import org.spongepowered.api.text.{transform, Text, TextElement, TranslatableText}

import net.katsstuff.chitchat.ChatConfig.{ChatFormatter, ChatOption, FormatterOptions}
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.LibPerm
import net.katsstuff.chitchat.{ChatConfig, ChitChatPlugin}
import net.katsstuff.katlib.helper.Implicits._

class ChatListener(implicit plugin: ChitChatPlugin, handler: ChannelHandler) {

  private val oldSerializer  = TextSerializers.FORMATTING_CODE
  private val jsonSerializer = TextSerializers.JSON

  private var cfg: ChatConfig.RootConfig = _

  /**
    * Reloads the config used by this listener
    */
  def reloadConfig(config: ChatConfig.RootConfig): Unit = cfg = config

  //Search for a option to use, trying both json and formatting codes
  def getTextOption(player: Player, variant: String): Option[Text] =
    player
      .getOption(s"json$variant")
      .toOption
      .map(jsonSerializer.deserialize)
      .orElse {
        player
          .getOption(variant)
          .toOption
          .map(oldSerializer.deserialize)
      }

  @Listener(order = Order.LAST)
  def onMessageChannel(event: MessageChannelEvent.Chat): Unit = {
    event.getCause.first(classOf[Player]).toOption match {
      case Some(player) =>
        val formatter = event.getFormatter
        val name      = findNameChat(formatter.getHeader.getAll.asScala, "header").getOrElse(t"${player.getName}")

        val stringFormat  = getTextOption(player, "namestyle")
        val textFormat    = stringFormat.flatMap(text => TextHelper.getFormatAtEnd(text))
        val formattedName = textFormat.fold(name)(format => t"$format$name")

        //Colors the body from formatting codes. This might destroy other data
        if (player.hasPermission(LibPerm.ChatColor)) {
          for (applier <- formatter.getBody.asScala if applier.getParameters.containsKey("body")) {
            applier.getParameter("body") match {
              case body: Text if body.toPlain.contains('&') =>
                applier.setParameter("body", oldSerializer.deserialize(oldSerializer.serialize(body)))
              case _ =>
            }
          }
        }

        formatEvent(event, player, cfg.formatting.chat, formattedName)

        event.getChannel.toOption.foreach { existing =>
          //Apply chat channel
          val playerChannel = handler.getChannelForReceiver(player)
          event.setChannel(new IntersectionMessageChannel(Set(existing, playerChannel.messageChannel)))

          //Pling mention
          if (cfg.mentionPling) {
            val message = event.getFormatter.getBody.format.toPlain
            playerChannel.members
              .flatMap(_.get)
              .collect { case player: Player if message.contains(player.getName) => player }
              .foreach(p => p.playSound(plugin.versionHelper.levelUpSound, p.getLocation.getPosition, 0.5D))
          }
        }
      case None =>
    }

    if (event.getCause.contains(SendToConsole)) {
      event.getChannel.toOption.foreach(c => event.setChannel(new CombinedMessageChannel(c, MessageChannel.TO_CONSOLE)))
    }
  }

  @Listener
  def onJoin(event: ClientConnectionEvent.Join): Unit = {
    val player = event.getTargetEntity
    handler.loginPlayer(player)
    formatConnectEvent(event, cfg.formatting.join)
    player.sendMessage(t"${YELLOW}You are chatting in ${handler.getChannelForReceiver(player).name}")
  }

  @Listener
  def onDisconnect(event: ClientConnectionEvent.Disconnect): Unit =
    formatConnectEvent(event, cfg.formatting.disconnect)

  def formatConnectEvent(
      event: ClientConnectionEvent with MessageEvent with TargetPlayerEvent,
      chatFormatter: ChatFormatter
  ): Unit = {
    val player = event.getTargetEntity
    val name   = findNameConnect(event.getFormatter.getBody.asScala.toSeq, "body").getOrElse(t"${player.getName}")

    val stringFormat  = getTextOption(player, "namestyle")
    val textFormat    = stringFormat.flatMap(text => TextHelper.getFormatAtEnd(text))
    val formattedName = textFormat.fold(name)(format => t"$format$name")

    formatEvent(event, player, chatFormatter, formattedName)
  }

  def formatEvent(
      event: MessageEvent,
      player: Player,
      chatFormatter: ChatFormatter,
      formattedName: TextElement
  ): Unit = {
    val formatter = event.getFormatter
    val sections = Map(
      formatter.getHeader -> chatFormatter.header,
      formatter.getBody   -> chatFormatter.body,
      formatter.getFooter -> chatFormatter.footer
    )

    for {
      (section, FormatterOptions(_, newTemplates, _)) <- sections
      newTemplate                                     <- newTemplates
    } {
      section.add(new transform.SimpleTextTemplateApplier(newTemplate))
    }

    for {
      (section, FormatterOptions(overrideTemplates, _, parameters)) <- sections
      applier                                                       <- section.asScala
    } {
      overrideTemplates.foreach {
        case (key, template) =>
          if (applier.getParameters.containsKey(key)) {
            applier.setTemplate(template)
          }
      }

      parameters.foreach {
        case (confName, ChatOption(optionName, isText, default)) =>
          if (optionName == "namestyle") {
            applier.setParameter("header", formattedName)
          }
          val content =
            if (isText) getTextOption(player, optionName).getOrElse(default)
            else player.getOption(optionName).toOption.fold(default)(Text.of)
          applier.setParameter(confName, content)
      }
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

object SendToConsole
