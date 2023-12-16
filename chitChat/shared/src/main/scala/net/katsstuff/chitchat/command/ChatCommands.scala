package net.katsstuff.chitchat.command

import org.spongepowered.api.command.CommandMapping
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import net.katsstuff.chitchat.{CCResource, ChatConfig, ChitChatPlugin}
import net.katsstuff.chitchat.helper.{ChatHelper, TextHelper}
import net.katsstuff.chitchat.lib.LibPerm
import shapeless.{Witness => W}
import net.katsstuff.katlib.helper.Implicits._

object ChatCommands {

  def registerAnnounce(implicit plugin: ChitChatPlugin): Option[CommandMapping] =
    Command
      .simple[Flags[NeedPermission[LibPerm.AnnounceConsoleCmd, BooleanFlag[W.`"c"`.T]], String]] {
        case (src, _, Flags(NeedPermission(BooleanFlag(console)), message)) =>
          val headerApplier = new SimpleTextTemplateApplier(plugin.config.formatting.commands.announceTemplate)
          headerApplier.setParameter(
            ChatConfig.TemplateHeader,
            if (console) CCResource.getText("cmd.announce.consoleName") else t"${src.getName}"
          )
          val headerText = headerApplier.toText

          val formatter =
            new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$message")
          formatter.getHeader.add(headerApplier)

          val cancelled = ChatHelper.sendMessagePostEvent(formatter, src, MessageChannel.TO_ALL)

          if (!cancelled) Command.successStep() else Command.errorStep(sendMessageFailed)
      }
      .register(
        plugin,
        Alias("announce"),
        Permission(LibPerm.AnnounceCmd),
        shortDescription = LocalizedDescription("cmd.announce.description") //TODO: Set extended description
      )
}
