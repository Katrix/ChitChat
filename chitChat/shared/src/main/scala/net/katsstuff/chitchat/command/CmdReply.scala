package net.katsstuff.chitchat.command

import java.util.{Locale, Optional}

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.event.SpongeEventFactory
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}
import net.katsstuff.chitchat.{CCResource, ChitChatPlugin}

class CmdReply(pmCmd: CmdPm)(implicit plugin: ChitChatPlugin) extends LocalizedCommand(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = for {
      message  <- args.one(LibCommandKey.Message).toRight(invalidParameterErrorLocalized)
      channel  <- pmCmd.conversations.get(src).toRight(new CommandException(CCResource.getText("cmd.reply.noOneReply")))
      receiver <- channel.receiver.get.toRight(new CommandException(CCResource.getText("cmd.reply.noLongerOnline")))
    } yield (receiver, message, channel)

    data match {
      case Right((receiver, message, channel)) =>
        val cause         = plugin.versionHelper.createChatCause(src)
        val headerApplier = new SimpleTextTemplateApplier(plugin.config.pmTemplate.value)
        headerApplier.setParameter(plugin.config.Sender, src.getName.text)
        headerApplier.setParameter(plugin.config.Receiver, receiver.getName.text)
        val headerText = headerApplier.toText

        val formatter = new MessageFormatter(
          t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$message"
        )
        formatter.getHeader.add(headerApplier)

        val event     = SpongeEventFactory.createMessageChannelEvent(cause, channel, Optional.of(channel), formatter, false)
        val cancelled = Sponge.getEventManager.post(event)

        if (!cancelled) {
          event.getChannel.toOption.foreach(_.send(src, event.getMessage))
          CommandResult.success()
        } else throw sendMessageFailed

      case Left(e) => throw e
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.reply.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.remainingJoinedStrings(LibCommandKey.Message))
      .description(this)
      .permission(LibPerm.PMCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("r", "reply")
}
