package net.katsstuff.chitchat.command

import java.util.Locale

import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.CCResource
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdJoinChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends LocalizedCommand(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = for {
      channel <- args.one(LibCommandKey.Channel).toRight(channelNotFound)
      _       <- Either.cond(src.hasPermission(s"${LibPerm.JoinChannelCmd}.${channel.name}"), (), missingPermissionChannel)
    } yield channel

    data match {
      case Right(channel) =>
        if (handler.setReceiverChannel(src, channel)) {
          src.sendMessage(t"$GREEN${CCResource.get("cmd.joinChannel.success", "channel" -> channel.name)}")
          CommandResult.success()
        } else throw new CommandException(CCResource.getText("cmd.joinChannel.failedJoin"))
      case Left(e) => throw e
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.joinChannel.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(new ChannelCommandArgument(LibCommandKey.Channel))
      .description(this)
      .permission(LibPerm.JoinChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("joinChannel", "chjoin")
}
