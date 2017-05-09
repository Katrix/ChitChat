package net.katsstuff.chitchat.command

import java.util
import java.util.Optional

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.ref.WeakReference

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.event.SpongeEventFactory
import org.spongepowered.api.event.cause.{Cause, NamedCause}
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.format.TextFormat
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier

import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibCommonTCommandKey
import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.chat.SendToConsole
import net.katsstuff.chitchat.helper.TextHelper
import net.katsstuff.chitchat.lib.{LibCommandKey, LibPerm}

class CmdPm(implicit plugin: ChitChatPlugin) extends CommandBase(None) {

  protected[command] val conversations = new mutable.WeakHashMap[CommandSource, PmMessageChannel]

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val data = for {
      player  <- args.one(LibCommonTCommandKey.Player).orElse(conversations.get(src).flatMap(_.receiver.get)).toRight(playerNotFoundError)
      message <- args.one(LibCommandKey.Message).toRight(invalidParameterError)
    } yield (player, message)

    data match {
      case Right((player, message)) =>
        val cause         = Cause.builder().suggestNamed("Plugin", plugin).named(NamedCause.owner(src)).named("SendToConsole", SendToConsole).build()
        val headerApplier = new SimpleTextTemplateApplier(plugin.config.pmTemplate.value)
        headerApplier.setParameter(plugin.config.Sender, src.getName.text)
        headerApplier.setParameter(plugin.config.Receiver, player.getName.text)
        val headerText = headerApplier.toText

        val formatter = new MessageFormatter(t"${TextHelper.getFormatAtEnd(headerText).getOrElse(TextFormat.NONE)}$message")
        formatter.getHeader.add(headerApplier)

        val srcChannel = new PmMessageChannel(src, player)
        conversations.put(src, srcChannel)
        conversations.put(player, new PmMessageChannel(player, src))

        val event     = SpongeEventFactory.createMessageChannelEvent(cause, srcChannel, Optional.of(srcChannel), formatter, false)
        val cancelled = Sponge.getEventManager.post(event)

        if (!cancelled) {
          event.getChannel.toOption.foreach(_.send(src, event.getMessage))
          CommandResult.success()
        } else {
          src.sendMessage(t"${RED}Failed to send message. Maybe some other plugin blocked it")
          CommandResult.empty()
        }

      case Left(e) => throw e
    }
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(
        GenericArguments.optional(GenericArguments.player(LibCommonTCommandKey.Player)),
        GenericArguments.remainingJoinedStrings(LibCommandKey.Message)
      )
      .description(t"Send a message to another player")
      .extendedDescription(t"The player is optional if you they were the previous player you sent a message to")
      .permission(LibPerm.PMCmd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("pm", "msg", "whisper")
}

case class PmMessageChannel(src: WeakReference[CommandSource], receiver: WeakReference[CommandSource]) extends MessageChannel {

  def this(src: CommandSource, receiver: CommandSource) = this(WeakReference(src), WeakReference(receiver))

  override def getMembers: util.Collection[MessageReceiver] =
    Set[Option[MessageReceiver]](src.get, receiver.get).flatten.asJava
}
