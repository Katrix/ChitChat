package net.katsstuff.chitchat.command

import java.util.Locale

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.CCResource
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdInChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends LocalizedCommand(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val channel = args.one(LibCommandKey.Channel).getOrElse(handler.getChannelForReceiver(src))
    val members = channel.members
      .flatMap(_.get)
      .collect {
        case cmdSrc: CommandSource => cmdSrc.getName
      }
      .mkString(", ")

    src.sendMessage(
      t"$GREEN${CCResource.get("cmd.inChannel.success", "channel" -> channel.name, "members" -> members)}"
    )
    CommandResult.success()
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.inChannel.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.optional(new ChannelCommandArgument(LibCommandKey.Channel)))
      .description(this)
      .permission(LibPerm.InChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("inChannel", "chmembers")
}
