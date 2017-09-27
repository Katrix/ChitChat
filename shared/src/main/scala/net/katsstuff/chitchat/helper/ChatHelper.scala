package net.katsstuff.chitchat.helper

import java.util.Optional

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.event.SpongeEventFactory
import org.spongepowered.api.event.cause.{Cause, NamedCause}
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter
import org.spongepowered.api.text.channel.MessageChannel

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits._

object ChatHelper {

  def sendMessagePostEvent(formatter: MessageFormatter, src: CommandSource, channel: MessageChannel)(
      implicit plugin: KatPlugin
  ): Boolean = {
    val cause     = Cause.builder().suggestNamed("Plugin", plugin).named(NamedCause.owner(src)).build()
    val event     = SpongeEventFactory.createMessageChannelEvent(cause, channel, Optional.of(channel), formatter, false)
    val cancelled = Sponge.getEventManager.post(event)

    if (!cancelled) {
      event.getChannel.toOption.foreach(_.send(src, event.getMessage))
      true
    } else false
  }
}
