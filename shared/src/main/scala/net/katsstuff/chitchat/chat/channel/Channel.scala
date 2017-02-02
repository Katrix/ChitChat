package net.katsstuff.chitchat.chat.channel

import scala.ref.WeakReference

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}

import net.katsstuff.chitchat.chat.channel.Channel.PositionedChannel

trait Channel {
  type Self <: Channel

  def messageChannel: MessageChannel

  def localName: String
  def localName_=(newName: String): Self
  def prefix: Text
  def prefix_=(newPrefix: Text): Self
  def description: Text
  def description_=(newDescription: Text): Self

  def name: Seq[String] = parent.map(_.name).getOrElse(Seq()) :+ localName
  def members: Set[WeakReference[MessageReceiver]]
  def members_=(newMembers: Set[WeakReference[MessageReceiver]]): Self

  def membersNested: Set[WeakReference[MessageReceiver]] = children.flatMap(_._1.membersNested).toSet ++ members

  def children: Seq[PositionedChannel]
  def children_=(newChildren: Seq[PositionedChannel]): Self
  def parent: Option[Channel]

  private lazy val childrenMap = children.map(t => (t._1.localName, t._1)).toMap

  def findChild(localName:  String): Option[Channel] = childrenMap.get(localName)
  def findChannel(thatName: Seq[String]): Option[Channel] = {
    val thisName = name

    if (thisName.length > thatName.length) None
    else if (thisName.length == thatName.length) {
      if (thisName == thatName) Some(this)
      else None
    } else {
      val isSubSeq = thatName.startsWith(thisName)
      if (!isSubSeq) None
      else {
        val rest = thatName.slice(0, thatName.lastIndexOfSlice(thisName) + 1)
        val head = rest.head //Safe as we know that thatName is bigger than thisName
        findChild(head).flatMap(_.findChannel(thatName))
      }
    }
  }
}
object Channel {
  type PositionedChannel = (Channel, Int)
}
