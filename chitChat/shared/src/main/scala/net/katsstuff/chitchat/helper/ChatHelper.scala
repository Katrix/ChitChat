package net.katsstuff.chitchat.helper

import java.util.Optional

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.event.SpongeEventFactory
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.channel.MessageChannel

import net.katsstuff.katlib.helper.Implicits._
import net.katsstuff.chitchat.ChitChatPlugin

object ChatHelper {

  def sendMessagePostEvent(formatter: MessageFormatter, src: CommandSource, channel: MessageChannel)(
      implicit plugin: ChitChatPlugin
  ): Boolean = {
    val cause     = plugin.versionHelper.createChatCause(src)
    val event     = SpongeEventFactory.createMessageChannelEvent(cause, channel, Optional.of(channel), formatter, false)
    val cancelled = Sponge.getEventManager.post(event)

    if (!cancelled) {
      event.getChannel.toOption.foreach(_.send(src, event.getMessage))
      true
    } else false
  }
}
