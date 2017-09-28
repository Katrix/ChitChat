package net.katsstuff.chitchat.chat

import java.util
import java.util.Optional

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}
import org.spongepowered.api.text.chat.ChatType

import scala.collection.JavaConverters._

/**
  * A [[MessageChannel]] that removes the members of all message channels
  * passed in except the first.
  */
class DifferenceMessageChannel(val first: MessageChannel, val rest: Set[MessageChannel]) extends MessageChannel {

  def this(first: MessageChannel, rest: MessageChannel*) = this(first, rest.toSet)
  def this(first: MessageChannel, rest: Iterable[MessageChannel]) = this(first, rest.toSet)

  override def transformMessage(
      sender: scala.Any,
      recipient: MessageReceiver,
      original: Text,
      `type`: ChatType
  ): Optional[Text] = first.transformMessage(sender, recipient, original, `type`)

  override def getMembers: util.Collection[MessageReceiver] = {
    val members = first.getMembers.asScala.toSet
    rest.foldLeft(members)((acc, rest) => acc.diff(rest.getMembers.asScala.toSet)).asJava
  }
}
