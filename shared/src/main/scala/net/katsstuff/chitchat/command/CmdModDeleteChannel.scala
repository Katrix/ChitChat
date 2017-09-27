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

class CmdModDeleteChannel(cmdAdmin: CmdModChannel)(implicit handler: ChannelHandler, plugin: KatPlugin)
    extends LocalizedCommand(Some(cmdAdmin)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = for {
      channel <- args.one(LibCommandKey.Channel).toRight(invalidParameterErrorLocalized)
      _ <- if (src.hasPermission(s"${LibPerm.DeleteChannelCmd}.${channel.name}")) Right(())
      else Left(missingPermissionChannel)
    } yield channel

    data match {
      case Right(channel) =>
        if (handler.removeChannel(channel)) {
          src.sendMessage(t"$GREEN${CCResource.get("cmd.mod.deleteChannel.success", "channel" -> channel.name)}")
          CommandResult.success()
        } else {
          throw new CommandException(CCResource.getText("cmd.mod.deleteChannel.cantDeleteGlobal"))
        }
      case Left(e) => throw e
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.mod.deleteChannel.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(new ChannelCommandArgument(LibCommandKey.Channel))
      .description(this)
      .permission(LibPerm.DeleteChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("delete", "remove")
}
