package net.katsstuff.chitchat.chat.channel

import java.util
import java.util.Optional

import scala.collection.JavaConverters._
import scala.ref.WeakReference

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}
import org.spongepowered.api.text.chat.ChatType

import io.github.katrix.katlib.helper.Implicits._

class PrefixedMessageChannel(prefix: Text, members: Set[WeakReference[MessageReceiver]]) extends MessageChannel {

  override def transformMessage(sender: scala.Any, recipient: MessageReceiver, original: Text, `type`: ChatType): Optional[Text] =
    Optional.of(t"[$prefix] ".concat(original))

  override def getMembers: util.Collection[MessageReceiver] = members.flatMap(_.get).asJava
}
