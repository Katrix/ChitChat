package net.katsstuff.chitchat.chat.channel

import java.util
import java.util.Collections

import scala.collection.JavaConverters._
import scala.ref.WeakReference

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}

import net.katsstuff.chitchat.chat.channel.Channel.PositionedChannel

case class SimpleChannel(
		localName: String,
		prefix: Text,
		description: Text,
		members: Set[WeakReference[MessageReceiver]],
		children: Seq[PositionedChannel],
		_parent: Channel) extends Channel {
	type Self = SimpleChannel

	lazy val messageChannel: MessageChannel = () => {
		val memberSet = Collections.newSetFromMap(new util.WeakHashMap[MessageReceiver, java.lang.Boolean])
		memberSet.addAll(members.flatMap(_.get).asJava)
		memberSet
	}

	override def localName_=(newName: String): Self = copy(localName = newName)
	override def prefix_=(newPrefix: Text): Self = copy(prefix = newPrefix)
	override def description_=(newDescription: Text): Self = copy(description = newDescription)
	override def members_=(newMembers: Set[WeakReference[MessageReceiver]]): Self = copy(members = newMembers)
	override def children_=(newChildren: Seq[PositionedChannel]): Self = copy(children = newChildren)
	override def parent: Option[Channel] = Some(_parent)
}
