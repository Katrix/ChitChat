package net.katsstuff.chitchat.command

import java.util.Optional

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.event.SpongeEventFactory
import org.spongepowered.api.event.cause.{Cause, NamedCause}
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdShout(implicit plugin: ChitChatPlugin, handler: ChannelHandler) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val data = for {
      channel <- args.one(LibCommandKey.Channel).toRight(channelNotFound)
      message <- args.one(LibCommandKey.Message).toRight(invalidParameterError)
      _       <- if (src.hasPermission(s"${LibPerm.ShoutCmd}.${channel.name}")) Right(()) else Left(missingPermissionChannel)
    } yield (channel, t"$message")

    data match {
      case Right((channel, message)) =>
        val cause         = Cause.builder().suggestNamed("Plugin", plugin).named(NamedCause.owner(src)).build()
        val headerApplier = new SimpleTextTemplateApplier(plugin.config.shoutTemplate.value)
        headerApplier.setParameter(plugin.config.TemplateHeader, src.getName.text)
        val headerText = headerApplier.toText

        val formatter = new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$message")
        formatter.getHeader.add(headerApplier)

        //TODO: Use chat event, and add ignore objects to listener
        val event =
          SpongeEventFactory.createMessageChannelEvent(cause, channel.messageChannel, Optional.of(channel.messageChannel), formatter, false)
        val cancelled = Sponge.getEventManager.post(event)

        if (!cancelled) {
          event.getChannel.toOption.foreach(_.send(src, event.getMessage))
          src.sendMessage(t"${GREEN}Sent message to ${channel.name}")
          CommandResult.success()
        } else {
          src.sendMessage(t"${RED}Failed to send message. Maybe some other plugin blocked it")
          CommandResult.empty()
        }
      case Left(e) => throw e
    }
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(new ChannelCommandArgument(LibCommandKey.Channel), GenericArguments.remainingJoinedStrings(LibCommandKey.Message))
      .description(t"Sends a message to another channel")
      .permission(LibPerm.ShoutCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("shout")
}
