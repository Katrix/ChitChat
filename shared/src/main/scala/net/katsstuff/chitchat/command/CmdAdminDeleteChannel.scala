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

class CmdAdminDeleteChannel(cmdAdmin: CmdAdminChannel)(implicit handler: ChannelHandler, plugin: KatPlugin) extends CommandBase(Some(cmdAdmin)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val data = for {
      channel <- args.one(LibCommandKey.Channel).toRight(invalidParameterError)
    } yield channel

    data match {
      case Right(channel) =>
        if(handler.removeChannel(channel)) {
          src.sendMessage(t"${GREEN}Deleted ${channel.name}")
          CommandResult.success()
        }
        else {
          throw new CommandException(t"${RED}Can't delete global channel")
        }
      case Left(e)        => throw e
    }

  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(new ChannelCommandArgument(LibCommandKey.Channel))
      .description(t"Delete a channel")
      .permission(LibPerm.DeleteChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("delete", "remove")
}
