package net.katsstuff.chitchat.command

import java.util.{Locale, Optional}

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.event.SpongeEventFactory
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}
import net.katsstuff.chitchat.{CCResource, ChitChatPlugin}

class CmdShout(implicit plugin: ChitChatPlugin, handler: ChannelHandler) extends LocalizedCommand(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = for {
      channel <- args.one(LibCommandKey.Channel).toRight(channelNotFound)
      message <- args.one(LibCommandKey.Message).toRight(invalidParameterErrorLocalized)
      _       <- if (src.hasPermission(s"${LibPerm.ShoutCmd}.${channel.name}")) Right(()) else Left(missingPermissionChannel)
    } yield (channel, t"$message")

    data match {
      case Right((channel, message)) =>
        val cause         = plugin.versionHelper.createChatCause(src)
        val headerApplier = new SimpleTextTemplateApplier(plugin.config.shoutTemplate.value)
        headerApplier.setParameter(plugin.config.TemplateHeader, src.getName.text)
        val headerText = headerApplier.toText

        val formatter =
          new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$message")
        formatter.getHeader.add(headerApplier)

        val msgChannel = channel.messageChannel
        val event = SpongeEventFactory.createMessageChannelEvent(cause, msgChannel, Optional.of(msgChannel), formatter, false)
        val cancelled = Sponge.getEventManager.post(event)

        if (!cancelled) {
          event.getChannel.toOption.foreach(_.send(src, event.getMessage))
          src.sendMessage(CCResource.getText("cmd.shout.success", "channel" -> channel.name))
          CommandResult.success()
        } else throw sendMessageFailed
      case Left(e) => throw e
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.shout.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(
        new ChannelCommandArgument(LibCommandKey.Channel),
        GenericArguments.remainingJoinedStrings(LibCommandKey.Message)
      )
      .description(this)
      .permission(LibPerm.ShoutCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("shout")
}
