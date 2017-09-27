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
import io.github.katrix.katlib.command.{CommandBase, LocalizedCommand}
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.chitchat.CCResource
import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.lib.LibPerm

class CmdModChannel(implicit handler: ChannelHandler, plugin: KatPlugin) extends LocalizedCommand(None) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val channels = handler.allChannels
      .filter { case (k, _) => src.hasPermission(s"${LibPerm.ModInterfaceCmd}.$k") }
      .toSeq
      .sortBy(_._1)
      .map {
        case (channelName, channel) =>
          val setName =
            button(t"$YELLOW${CCResource.get("cmd.mod.channel.setName")}")(
              s"/channelmod edit $channelName --name <newName>"
            )
          val setPrefix = button(t"$YELLOW${CCResource.get("cmd.mod.channel.setPrefix")}")(
            s"/channelmod edit $channelName --prefix <newprefix>"
          )
          val setDescription =
            button(t"$YELLOW${CCResource.get("cmd.mod.channel.setDescription")}")(
              s"/channelmod edit $channelName --description <newDescription>"
            )

          val extraSets = channel.extraData.values.map { extraData =>
            val extraDataName = extraData.displayName
            button(t"$YELLOW${CCResource.get("cmd.mod.channel.setExtra", "extra" -> extraDataName)}")(
              s"/channelmod edit $channelName --extra $extraDataName <new${extraDataName.capitalize}"
            )
          }

          val combinedExtra = Text.of(extraSets.toSeq: _*)

          val delete = button(t"$RED${CCResource.get("cmd.mod.channel.delete")}")(s"/channelAdmin delete $channelName")
          val typeName =
            if (channel.typeName == "Simple") Text.EMPTY
            else t" $YELLOW${CCResource.get("cmd.mod.channel.channelType", "channelType" -> channel.typeName)}"

          t"""$AQUA$channelName$typeName:$RESET $setName $setPrefix $setDescription $combinedExtra $delete"""
      }

    val create = button(t"$GREEN${CCResource.get("cmd.mod.channel.create")}")(
      "/channelmod create [type] <name> [prefix] [description] [extraData...]"
    )

    val pagination = Sponge.getServiceManager
      .provideUnchecked(classOf[PaginationService])
      .builder()

    pagination.title(t"$YELLOW${CCResource.get("cmd.mod.channel.title")}")
    pagination.contents(create +: channels: _*)
    pagination.sendTo(src)

    CommandResult.success()
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(CCResource.getText("cmd.mod.channel.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
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
