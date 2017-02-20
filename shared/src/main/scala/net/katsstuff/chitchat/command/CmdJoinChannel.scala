package net.katsstuff.chitchat.command

import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdJoinChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val data = for {
      channel <- args.one(LibCommandKey.Channel).toRight(channelNotFound)
      _       <- if (src.hasPermission(s"${LibPerm.JoinChannelCmd}.${channel.name}")) Right(()) else Left(missingPermissionChannel)
    } yield channel

    data match {
      case Right(channel) =>
        if (handler.setReceiverChannel(src, channel)) {
          src.sendMessage(t"""${GREEN}Joined "${channel.name}"""")
          CommandResult.success()
        } else throw new CommandException(t"${RED}Failed to set new channel")
      case Left(e) => throw e
    }
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(new ChannelCommandArgument(LibCommandKey.Channel))
      .description(t"Join a specific channel")
      .permission(LibPerm.JoinChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("joinChannel", "chjoin")
}
