package net.katsstuff.chitchat.command

import java.util.Locale

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.CCResource
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.LibPerm

class CmdListChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends LocalizedCommand(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val channels = handler.allChannels
      .filter { case (name, _) => src.hasPermission(s"${LibPerm.JoinChannelCmd}.$name") }
      .toSeq
      .sortBy(_._1)
      .map {
        case (channelName, channel) =>
          val join = button(t"$YELLOW${CCResource.get("cmd.list.join")}")(s"/joinChannel $channelName")

          val typeName =
            if (channel.typeName == "Simple") Text.EMPTY
            else t" $YELLOW${CCResource.get("cmd.list.typeName", "type" -> channel.typeName)}"

          t""""$channelName"$typeName - ${channel.description}: $join"""
      }

    val pagination = Sponge.getServiceManager.provideUnchecked(classOf[PaginationService]).builder()

    pagination.title(t"$YELLOW${CCResource.get("cmd.list.channels")}")
    pagination.contents(channels: _*)
    pagination.sendTo(src)

    CommandResult.success()
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] = Some(CCResource.getText("cmd.list.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
      .permission(LibPerm.ListChannelsCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("listChannels", "channels", "chlist")
}
