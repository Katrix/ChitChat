package net.katsstuff.chitchat.command

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.serializer.TextSerializers

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdAdminChangeChannel(cmdAdmin: CmdAdminChannel)(implicit handler: ChannelHandler, plugin: KatPlugin) extends CommandBase(Some(cmdAdmin)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val channel = args.one(LibCommandKey.Channel).getOrElse(throw channelNotFound)

    val serializer = TextSerializers.FORMATTING_CODE

    val name        = args.one(LibCommandKey.ChannelName)
    val prefix      = args.one(LibCommandKey.ChannelPrefix)
    val description = args.one(LibCommandKey.ChannelDescription)
    val extra       = args.one(LibCommandKey.ChannelExtra)

    val builder = Text.builder().color(GREEN)
    var counter = 0

    name.foreach { newName =>
      handler.renameChannel(channel, newName)
      builder.append(t"Set name to $newName")
      builder.append(Text.NEW_LINE)
      counter += 1
    }

    prefix.foreach { newPrefix =>
      val deseralized = serializer.deserialize(newPrefix)
      handler.setChannelPrefix(channel, deseralized)
      builder.append(t"Set prefix to $deseralized")
      builder.append(Text.NEW_LINE)
      counter += 1
    }

    description.foreach { newDescription =>
      val deseralized = serializer.deserialize(newDescription)
      handler.setChannelDescription(channel, deseralized)
      builder.append(t"Set description to $deseralized")
      builder.append(Text.NEW_LINE)
      counter += 1
    }

    extra.foreach { extraData =>
      handler
        .setExtraData(channel, extraData)
        .fold {
          builder.append(t"Set extra data")
          counter += 1
        }(src.sendMessage)

    }

    if (counter != 0) src.sendMessage(builder.trim().build())
    else src.sendMessage(t"No changes made, please specify flags for what to change")

    CommandResult.builder().successCount(counter).build()
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(
        GenericArguments
          .flags()
          .valueFlag(GenericArguments.string(LibCommandKey.ChannelName), "-name")
          .valueFlag(GenericArguments.string(LibCommandKey.ChannelPrefix), "-prefix")
          .valueFlag(GenericArguments.string(LibCommandKey.ChannelDescription), "-description")
          .valueFlag(GenericArguments.remainingJoinedStrings(LibCommandKey.ChannelDescription), "-extra")
          .buildWith(new ChannelCommandArgument(LibCommandKey.Channel))
      )
      .description(t"Change a channel's settings")
      .permission(LibPerm.ChangeChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("change", "edit", "modify")
}
