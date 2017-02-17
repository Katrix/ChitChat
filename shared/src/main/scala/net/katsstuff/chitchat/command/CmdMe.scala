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
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdMe(implicit plugin: ChitChatPlugin) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val rawMessage = t"${args.one(LibCommandKey.Message).getOrElse(throw invalidParameterError)}"

    val cause         = Cause.builder().suggestNamed("Plugin", plugin).named(NamedCause.owner(src)).build()
    val headerApplier = new SimpleTextTemplateApplier(plugin.config.meTemplate.value)
    headerApplier.setParameter(plugin.config.TemplateHeader, src.getName.text)
    val headerText = headerApplier.toText

    val formatter = new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$rawMessage")
    formatter.getHeader.add(headerApplier)

    val event = SpongeEventFactory
      .createMessageChannelEventChat(cause, src.getMessageChannel, Optional.of(src.getMessageChannel), formatter, rawMessage, false)
    val cancelled = Sponge.getEventManager.post(event)

    if (!cancelled) {
      event.getChannel.toOption.foreach(_.send(src, event.getMessage))
      CommandResult.success()
    } else {
      src.sendMessage(t"${RED}Failed to send message. Maybe some other plugin blocked it")
      CommandResult.empty()
    }
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.remainingJoinedStrings(LibCommandKey.Message))
      .description(t"Act out an action")
      .permission(LibPerm.MeCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("me")
}
