package net.katsstuff.chitchat.chat.channel

import scala.ref.WeakReference

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageReceiver

import net.katsstuff.chitchat.chat.LocalizedMessageChannel

case class ExtraData(displayName: String, data: Any)

/**
  * Base trait for all channels.
  * All channels are required to have a name, a prefix, and a description.
  * How to represent them is up to the implementation.
  *
  * A channel also has a set of members,
  * which should be the members of the [[LocalizedMessageChannel]].
  */
trait Channel {
  type Self <: Channel

  def messageChannel: LocalizedMessageChannel

  def name:                    String
  def name_=(newName: String): Self

  def prefix:                    Text
  def prefix_=(newPrefix: Text): Self

  def description:                         Text
  def description_=(newDescription: Text): Self

  def members:                                                    Set[WeakReference[MessageReceiver]]
  def members_=(newMembers: Set[WeakReference[MessageReceiver]]): Self

  def addMember(receiver: MessageReceiver):    Self = members = members + WeakReference(receiver)
  def removeMember(receiver: MessageReceiver): Self = members = members.filterNot(_.get.contains(receiver))

  /**
    * The display name of this type of channel.
    */
  def typeName: String

  /**
    * The extra data this channel holds.
    */
  def extraData: Map[String, ExtraData] = Map.empty

  /**
    * Handles changing custom data.
    * @param data The data to set. The implementation decides what is a
    *             correct format for the data.
    * @return Left with error message if setting the data failed, else Right.
    */
  def handleExtraData(data: String): Either[Text, Self]
}
