package net.katsstuff.chitchat.command

import java.util.Optional

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.event.SpongeEventFactory
import org.spongepowered.api.event.cause.{Cause, NamedCause}
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdReply(pmCmd: CmdPm)(implicit plugin: ChitChatPlugin) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val data = for {
      message  <- args.one(LibCommandKey.Message).toRight(invalidParameterError)
      channel  <- pmCmd.conversations.get(src).toRight(new CommandException(t"${RED}No one to reply to"))
      receiver <- channel.receiver.get.toRight(new CommandException(t"${RED}That player is no longer online"))
    } yield (receiver, message, channel)

    data match {
      case Right((receiver, message, channel)) =>
        val cause         = Cause.builder().suggestNamed("Plugin", plugin).named(NamedCause.owner(src)).build()
        val headerApplier = new SimpleTextTemplateApplier(plugin.config.pmTemplate.value)
        headerApplier.setParameter(plugin.config.Sender, src.getName.text)
        headerApplier.setParameter(plugin.config.Receiver, receiver.getName.text)
        val headerText = headerApplier.toText

        val formatter = new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$message")
        formatter.getHeader.add(headerApplier)

        val event     = SpongeEventFactory.createMessageChannelEvent(cause, channel, Optional.of(channel), formatter, false)
        val cancelled = Sponge.getEventManager.post(event)

        if (!cancelled) {
          event.getChannel.toOption.foreach(_.send(src, event.getMessage))
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
      .arguments(GenericArguments.remainingJoinedStrings(LibCommandKey.Message))
      .description(t"Send a message to another player")
      .permission(LibPerm.PMCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("r", "reply")
}
