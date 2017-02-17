package net.katsstuff.chitchat.command

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.serializer.TextSerializers

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.chat.channel.SimpleChannel
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdAdminCreateChannel(cmdAdmin: CmdAdminChannel)(implicit handler: ChannelHandler, plugin: KatPlugin) extends CommandBase(Some(cmdAdmin)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val serializer = TextSerializers.FORMATTING_CODE

    val data = for {
      name <- args.one(LibCommandKey.ChannelName).toRight(invalidParameterError)
      _ <- if(src.hasPermission(s"${LibPerm.CreateChannelCmd}.$name")) Right(()) else Left(missingPermissionChannel)
    } yield
      (name,
       serializer.deserialize(args.one(LibCommandKey.ChannelPrefix).getOrElse("")),
       serializer.deserialize(args.one(LibCommandKey.ChannelDescription).getOrElse("")),
       args.one(LibCommandKey.ChannelType),
       args.one(LibCommandKey.ChannelExtra))

    val channel = data match {
      case Right((name, prefix, description, None, None)) =>
        new SimpleChannel(name, prefix, description, Set())
      case Right((name, prefix, description, Some(channelType), extra)) =>
        handler.registry
          .createChannel(channelType, name, prefix, description, extra.getOrElse(""))
          .fold(e => throw new CommandException(e), identity)
      case Left(e) => throw e
      case _       => throw invalidParameterError
    }

    if (handler.addChannel(channel)) {
      src.sendMessage(t"${GREEN}Created new channel")
      CommandResult.success()
    } else throw new CommandException(t"${RED}Channel name can't be global")
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(
        GenericArguments.string(LibCommandKey.ChannelName),
        GenericArguments.optional(GenericArguments.string(LibCommandKey.ChannelPrefix)),
        GenericArguments.optional(GenericArguments.string(LibCommandKey.ChannelDescription)),
        GenericArguments.optional(GenericArguments.string(LibCommandKey.ChannelType)),
        GenericArguments.optional(GenericArguments.remainingJoinedStrings(LibCommandKey.ChannelExtra))
      )
      .description(t"Create a new channel")
      .permission(LibPerm.CreateChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("create", "new")
}
