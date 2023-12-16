package net.katsstuff.chitchat.command

import java.util.Locale

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.serializer.TextSerializers

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.CCResource
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdModChangeChannel(cmdAdmin: CmdModChannel)(implicit handler: ChannelHandler, plugin: KatPlugin)
    extends LocalizedCommand(Some(cmdAdmin)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val channel = args.one(LibCommandKey.Channel).getOrElse(throw channelNotFound)
    if (!src.hasPermission(s"${LibPerm.ChangeChannelCmd}.${channel.name}")) throw missingPermissionChannel

    val serializer = TextSerializers.FORMATTING_CODE

    val name        = args.one(LibCommandKey.ChannelName)
    val prefix      = args.one(LibCommandKey.ChannelPrefix)
    val description = args.one(LibCommandKey.ChannelDescription)
    val extra       = args.one(LibCommandKey.ChannelExtra)

    val builder = Text.builder().color(GREEN)
    var counter = 0

    name.foreach { newName =>
      handler.renameChannel(channel, newName)
      builder.append(CCResource.getText("cmd.mod.changeChannel.setName", "name" -> newName))
      counter += 1
    }

    prefix.foreach { newPrefix =>
      val deseralized = serializer.deserialize(newPrefix)
      handler.setChannelPrefix(channel, deseralized)
      builder.append(CCResource.getText("cmd.mod.changeChannel.setPrefix", "prefix" -> deseralized))
      if (counter > 0) builder.append(Text.NEW_LINE)
      counter += 1
    }

    description.foreach { newDescription =>
      val deseralized = serializer.deserialize(newDescription)
      handler.setChannelDescription(channel, deseralized)
      builder.append(CCResource.getText("cmd.mod.changeChannel.setDescription", "description" -> deseralized))
      if (counter > 0) builder.append(Text.NEW_LINE)
      counter += 1
    }

    extra.foreach { extraData =>
      handler
        .setExtraData(channel, extraData)
        .fold {
          builder.append(CCResource.getText("cmd.mod.changeChannel.setExtra"))
          counter += 1
        }(src.sendMessage)

    }

    if (counter != 0) src.sendMessage(builder.trim().build())
    else throw new CommandException(CCResource.getText("cmd.mod.changeChannel.noChangesSet"))

    CommandResult.builder().successCount(counter).build()
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.mod.changeChannel.description"))

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
      .description(this)
      .permission(LibPerm.ChangeChannelCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("edit", "modify")
}
