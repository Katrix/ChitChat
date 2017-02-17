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

class CmdAdminChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val channels = handler.allChannels
      .filter { case (k, _) => src.hasPermission(s"${LibPerm.AdminInterfaceCmd}.$k") }
      .map {
        case (channelName, channel) =>
          val setName        = button(t"${YELLOW}Set name")(s"/channelAdmin change $channelName --name <newName>")
          val setPrefix      = button(t"${YELLOW}Set prefix")(s"/channelAdmin change $channelName --prefix <newprefix>")
          val setDescription = button(t"${YELLOW}Set description")(s"/channelAdmin change $channelName --name <newDescription>")

          val extraSets = channel.extraData.values.map { extraData =>
            val extraDataName = extraData.displayName
            button(t"${YELLOW}Set $extraDataName")(s"/channelAdmin change $channelName --extra $extraDataName <new${extraDataName.capitalize}")
          }

          val combinedExtra = Text.of(extraSets.toSeq: _*)

          val delete   = button(t"${RED}Delete")(s"/channelAdmin delete $channelName")
          val typeName = if (channel.typeName == "Simple") Text.EMPTY else t" $YELLOW${channel.typeName} channel"

          t""""$channelName"$typeName: $setName$setPrefix$setDescription$combinedExtra$delete"""
      }
      .toSeq

    val create = button(t"${GREEN}Create")("/channelAdmin create [type] <name> <prefix> <description> <extraData...>")

    val pagination = Sponge.getServiceManager.provideUnchecked(classOf[PaginationService]).builder()

    pagination.title(t"${YELLOW}Edit channels")
    pagination.contents(create +: channels: _*)
    pagination.sendTo(src)

    CommandResult.success()
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(t"Interface for administrating different channels")
      .permission(LibPerm.AdminInterfaceCmd)
      .children(this)
      .build()

  // @formatter:off
  override def children: Seq[CommandBase] = Seq(
    new CmdAdminCreateChannel(this),
    new CmdAdminChangeChannel(this),
    new CmdAdminDeleteChannel(this)
  )
  // @formatter:on

  override def aliases: Seq[String] = Seq("channelAdmin", "roomAdmin")
}
