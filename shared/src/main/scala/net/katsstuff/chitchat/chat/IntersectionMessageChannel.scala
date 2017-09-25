package net.katsstuff.chitchat.chat

import java.util
import java.util.Optional

import scala.collection.JavaConverters._

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}
import org.spongepowered.api.text.chat.ChatType

/**
  * A [[MessageChannel]] that has the members that are contained in all the
  * channels passed in.
  */
class IntersectionMessageChannel(val channels: Set[MessageChannel]) extends MessageChannel {

  def this(channels: MessageChannel*) = this(channels.toSet)

  override def transformMessage(
      sender: scala.Any,
      recipient: MessageReceiver,
      original: Text,
      `type`: ChatType
  ): Optional[Text] = {
    val transformedText = channels.foldLeft(original)(
      (acc, channel) => channel.transformMessage(sender, recipient, acc, `type`).orElse(acc)
    )

    Optional.ofNullable(transformedText)
  }

  override def getMembers: util.Collection[MessageReceiver] = {
    val allMembers = channels.map(_.getMembers.asScala.toSet)

    allMembers
      .foldLeft(allMembers.flatten)((acc, members) => acc.intersect(members))
      .asJava
  }
}
