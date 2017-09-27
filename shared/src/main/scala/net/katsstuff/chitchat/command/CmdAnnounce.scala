package net.katsstuff.chitchat.command

import java.util.Locale

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.helper.{ChatHelper, TextHelper}
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}
import net.katsstuff.chitchat.{CCResource, ChitChatPlugin}

class CmdAnnounce(implicit plugin: ChitChatPlugin) extends LocalizedCommand(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val message = args.one(LibCommandKey.Message).getOrElse(throw invalidParameterErrorLocalized)
    val console = args.hasAny("c")

    val headerApplier = new SimpleTextTemplateApplier(plugin.config.announceTemplate.value)
    headerApplier.setParameter(
      plugin.config.TemplateHeader,
      if (console) CCResource.getText("cmd.announce.consoleName") else src.getName.text
    )
    val headerText = headerApplier.toText

    val formatter = new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$message")
    formatter.getHeader.add(headerApplier)

    val cancelled = ChatHelper.sendMessagePostEvent(formatter, src, MessageChannel.TO_ALL)

    if (!cancelled) {
      CommandResult.success()
    } else throw sendMessageFailed
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.announce.description"))
  override def localizedExtendedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.announce.extendedDescription"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(
        GenericArguments
          .flags()
          .permissionFlag(LibPerm.AnnounceConsoleCmd, "c")
          .buildWith(GenericArguments.remainingJoinedStrings(LibCommandKey.Message))
      )
      .description(this)
      .extendedDescription(this)
      .permission(LibPerm.AnnounceCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("announce")
}
