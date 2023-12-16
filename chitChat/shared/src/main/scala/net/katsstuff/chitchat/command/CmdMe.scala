package net.katsstuff.chitchat.command

import java.util.Locale

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.{CCResource, ChitChatPlugin}
import net.katsstuff.chitchat.helper.{ChatHelper, TextHelper}
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdMe(implicit plugin: ChitChatPlugin) extends LocalizedCommand(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val rawMessage = t"${args.one(LibCommandKey.Message).getOrElse(throw invalidParameterErrorLocalized)}"

    val headerApplier = new SimpleTextTemplateApplier(plugin.config.meTemplate.value)
    headerApplier.setParameter(plugin.config.TemplateHeader, src.getName.text)
    val headerText = headerApplier.toText

    val formatter =
      new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$rawMessage")
    formatter.getHeader.add(headerApplier)

    val cancelled = ChatHelper.sendMessagePostEvent(formatter, src, src.getMessageChannel)

    if (!cancelled) {
      CommandResult.success()
    } else throw sendMessageFailed
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.me.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.remainingJoinedStrings(LibCommandKey.Message))
      .description(this)
      .permission(LibPerm.MeCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("me")
}
