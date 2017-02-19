package net.katsstuff.chitchat.command

import java.util.Optional

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.event.SpongeEventFactory
import org.spongepowered.api.event.cause.{Cause, NamedCause}
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdAnnounce(implicit plugin: ChitChatPlugin) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val message = args.one(LibCommandKey.Message).getOrElse(throw invalidParameterError)
    val console = args.hasAny("c")

    val cause         = Cause.builder().suggestNamed("Plugin", plugin).named(NamedCause.owner(src)).build()
    val headerApplier = new SimpleTextTemplateApplier(plugin.config.announceTemplate.value)
    headerApplier.setParameter(plugin.config.TemplateHeader, if (console) t"Console" else src.getName.text)
    val headerText = headerApplier.toText

    val formatter = new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$message")
    formatter.getHeader.add(headerApplier)

    val event     = SpongeEventFactory.createMessageChannelEvent(cause, MessageChannel.TO_ALL, Optional.of(MessageChannel.TO_ALL), formatter, false)
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
      .arguments(
        GenericArguments
          .flags()
          .permissionFlag(LibPerm.AnnounceConsoleCmd, "c")
          .buildWith(GenericArguments.remainingJoinedStrings(LibCommandKey.Message))
      )
      .description(t"Announce something")
      .extendedDescription(t"If you supply the -c flag, it will do the announcement as console")
      .permission(LibPerm.AnnounceCmd)
      .executor(this)
      .build()
  override def aliases: Seq[String] = Seq("announce")
}
