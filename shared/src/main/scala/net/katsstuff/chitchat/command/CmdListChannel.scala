package net.katsstuff.chitchat.command

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.LibPerm

class CmdListChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val channels = handler.allChannels
      .filter { case (k, _) => src.hasPermission(s"${LibPerm.JoinChannelCmd}.$k") }
      .map {
        case (channelName, channel) =>
          val join = button(t"${YELLOW}Join")(s"/joinChannel $channelName")

          val typeName = if (channel.typeName == "Simple") Text.EMPTY else t" $YELLOW${channel.typeName} channel"

          t""""$channelName"$typeName - ${channel.description}: $join"""
      }
      .toSeq

    val pagination = Sponge.getServiceManager.provideUnchecked(classOf[PaginationService]).builder()

    pagination.title(t"${YELLOW}Channels")
    pagination.contents(channels: _*)
    pagination.sendTo(src)

    CommandResult.success()
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(t"Lists all the chat channels")
      .permission(LibPerm.ListChannelsCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("listChannels", "listRooms")
}
