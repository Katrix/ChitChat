package net.katsstuff.chitchat.chat.channel

import java.util
import java.util.Collections

import scala.collection.JavaConverters._
import scala.ref.WeakReference

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}

import net.katsstuff.chitchat.chat.channel.Channel.PositionedChannel

case class RootChannel(
		localName: String,
		prefix: Text,
		description: Text,
		members: Set[WeakReference[MessageReceiver]],
		children: Seq[PositionedChannel]) extends Channel {
	type Self = RootChannel

	lazy val messageChannel: MessageChannel = () => {
		val memberSet = Collections.newSetFromMap(new util.WeakHashMap[MessageReceiver, java.lang.Boolean])
		memberSet.addAll(members.flatMap(_.get).asJava)
		memberSet
	}

	override def localName_=(newName: String): Self = copy(localName = newName)
	override def prefix_=(newPrefix: Text): Self = copy(prefix = newPrefix)
	override def description_=(newDescription: Text): Self = copy(description = newDescription)
	override def members_+(receiver: MessageReceiver): Self = copy(members = members + WeakReference(receiver))
	override def members_-(receiver: MessageReceiver): Self = copy(members = members - WeakReference(receiver))
	override def children_+(channel: PositionedChannel): Self = copy(children = (children :+ channel).sortBy(_._2))
	override def children_-(channel: PositionedChannel): Self = copy(children = children.filter(_ != channel).sortBy(_._2))
	override def parent: Option[Channel] = None
}
