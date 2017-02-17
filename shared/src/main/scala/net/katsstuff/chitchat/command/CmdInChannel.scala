package net.katsstuff.chitchat.command

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdInChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val channel = args.one(LibCommandKey.Channel).getOrElse(handler.getChannelForReceiver(src))
    val members = channel.members
      .flatMap(_.get)
      .collect {
        case cmdSrc: CommandSource => cmdSrc.getName
      }
      .mkString(", ")

    src.sendMessage(t"$members")
    CommandResult.success()
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.optional(new ChannelCommandArgument(LibCommandKey.Channel)))
      .description(t"List the people in a channel")
      .permission(LibPerm.InChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("inChannel", "showMembers")
}
