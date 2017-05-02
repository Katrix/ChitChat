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

class CmdModChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends CommandBase(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val channels = handler.allChannels
      .filter { case (k, _) => src.hasPermission(s"${LibPerm.ModInterfaceCmd}.$k") }
      .toSeq
      .sortBy(_._1)
      .map {
        case (channelName, channel) =>
          val setName        = button(t"${YELLOW}Set name")(s"/channelmod edit $channelName --name <newName>")
          val setPrefix      = button(t"${YELLOW}Set prefix")(s"/channelmod edit $channelName --prefix <newprefix>")
          val setDescription = button(t"${YELLOW}Set description")(s"/channelmod edit $channelName --description <newDescription>")

          val extraSets = channel.extraData.values.map { extraData =>
            val extraDataName = extraData.displayName
            button(t"${YELLOW}Set $extraDataName")(s"/channelmod edit $channelName --extra $extraDataName <new${extraDataName.capitalize}")
          }

          val combinedExtra = Text.of(extraSets.toSeq: _*)

          val delete = button(t"${RED}Delete")(s"/channelAdmin delete $channelName")
          val typeName =
            if (channel.typeName == "Simple") Text.EMPTY
            else t" $YELLOW${channel.typeName} channel"

          t"""$AQUA$channelName$typeName:$RESET $setName $setPrefix $setDescription $combinedExtra $delete"""
      }

    val create = button(t"${GREEN}Create")("/channelmod create [type] <name> [prefix] [description] [extraData...]")

    val pagination = Sponge.getServiceManager
      .provideUnchecked(classOf[PaginationService])
      .builder()

    pagination.title(t"${YELLOW}Edit channels")
    pagination.contents(create +: channels: _*)
    pagination.sendTo(src)

    CommandResult.success()
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(t"Interface for editing different channels")
      .permission(LibPerm.ModInterfaceCmd)
      .children(this)
      .executor(this)
      .build()

  // @formatter:off
  override def children: Seq[CommandBase] = Seq(
    new CmdModCreateChannel(this),
    new CmdModChangeChannel(this),
    new CmdModDeleteChannel(this)
  )
  // @formatter:on

  override def aliases: Seq[String] = Seq("channelmod", "chmod")
}
